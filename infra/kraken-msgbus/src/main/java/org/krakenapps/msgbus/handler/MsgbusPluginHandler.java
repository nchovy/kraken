/*
 * Copyright 2011 Future Systems, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.msgbus.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.PrimitiveHandler;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.MessageHandler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.SessionEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgbusPluginHandler extends PrimitiveHandler implements MessageHandler, SessionEventHandler {
	final Logger logger = LoggerFactory.getLogger(MsgbusPluginHandler.class.getName());

	private MessageBus msgbus;
	private Map<String, Handler> messageHandlerMap = new ConcurrentHashMap<String, Handler>();
	private Set<Handler> sessionHandlers = Collections.newSetFromMap(new ConcurrentHashMap<Handler, Boolean>());

	/*
	 * PrimitiveHandler implementations
	 */

	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Element metadata, Dictionary config) throws ConfigurationException {
		logger.trace("msgbus handler: configuring component instance [{}]", config.get("instance.name"));
		injectMessageBus();
		registerHandlerMethods();
	}

	@Override
	public void stateChanged(int state) {
		if (state == InstanceManager.VALID) {
			validate();
		} else if (state == InstanceManager.INVALID) {
			invalidate();
		}
	}

	private void injectMessageBus() {
		PojoMetadata pojoMetadata = getPojoMetadata();
		for (FieldMetadata fieldMetadata : pojoMetadata.getFields()) {
			if (fieldMetadata.getFieldType().equals("org.krakenapps.msgbus.MessageBus")) {
				getInstanceManager().register(fieldMetadata, this);
			}
		}
	}

	private void registerHandlerMethods() {
		Object o = getInstanceManager().getPojoObject();
		if (o == null) {
			logger.error("msgbus handler: failed to register handler methods (pojo not found)");
			return;
		}

		Class<?> clazz = o.getClass();
		for (Method m : clazz.getDeclaredMethods()) {
			MsgbusMethod method = m.getAnnotation(MsgbusMethod.class);
			if (method == null)
				continue;

			Set<MsgbusPermission> perms = new HashSet<MsgbusPermission>();
			for (Annotation a : m.getAnnotations())
				if (a.annotationType() == MsgbusPermission.class)
					perms.add((MsgbusPermission) a);

			AllowGuestAccess allowGuestAccess = m.getAnnotation(AllowGuestAccess.class);

			logger.trace("msgbus handler: msgbus method [{}] detected.", m.getName());
			MethodMetadata mm = getPojoMetadata().getMethod(m.getName());

			if (mm == null)
				continue;

			logger.trace("msgbus handler: annotated method [{}] injected.", m.getName());

			Handler handler = new Handler();
			handler.plugin = o;
			handler.method = m;
			handler.type = method.type();
			handler.perms = perms;
			handler.allowGuestAccess = allowGuestAccess != null;

			String methodName = clazz.getName() + "." + mm.getMethodName();

			logger.trace("msgbus handler: adding [{}] to message handler map.", methodName);

			if (method.type() == CallbackType.MessageReceived) {
				messageHandlerMap.put(methodName, handler);
				logger.trace("msgbus handler: MessageReceived callback [{}] detected.", mm.getMethodName());
			} else {
				sessionHandlers.add(handler);
				logger.trace("msgbus handler: Session callback [{}] detected.", mm.getMethodName());
			}
		}
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	/*
	 * MessageHandler implementations
	 */

	@Override
	public void handleMessage(Request req, Response resp) throws Exception {
		Session session = req.getSession();
		if (session == null)
			throw new IllegalStateException("session not found for request [" + req.getMethod() + "]");

		Handler handler = messageHandlerMap.get(req.getMethod());
		if (!handler.allowGuestAccess && isGuest(session))
			throw new SecurityException("guest cannot request [" + req.getMethod() + "]");

		for (MsgbusPermission perm : handler.perms) {
			if (!msgbus.checkPermission(session, perm.group(), perm.code())) {
				String subject = req.getAdminLoginName() == null ? "guest" : "[" + req.getAdminLoginName() + "]";
				throw new SecurityException(subject + " has no [" + perm.group() + "/" + perm.code() + "] permission");
			}
		}

		logger.trace("msgbus hanlder: dispatching message [{}] to handler [{}]", req.getMethod(), handler);

		handler.method.invoke(handler.plugin, new Object[] { req, resp });
	}

	private boolean isGuest(Session session) {
		if (session.getOrgDomain() != null && session.getAdminLoginName() != null)
			return false;

		return true;
	}

	@Override
	public String getClassName() {
		return getInstanceManager().getPojoObject().getClass().getName();
	}

	@Override
	public Collection<String> getMethodNames() {
		Set<String> messageTypes = new HashSet<String>();
		for (String messageType : messageHandlerMap.keySet()) {
			messageTypes.add(messageType);
		}
		return messageTypes;
	}

	/*
	 * SessionEventHandler implementations
	 */

	@Override
	public void sessionOpened(Session session) {
		for (Handler handler : sessionHandlers) {
			if (handler.type == CallbackType.SessionOpened)
				fireSessionEvent(handler.plugin, handler.method, session);
		}
	}

	@Override
	public void sessionClosed(Session session) {
		for (Handler handler : sessionHandlers) {
			if (handler.type == CallbackType.SessionClosed)
				fireSessionEvent(handler.plugin, handler.method, session);
		}
	}

	private void fireSessionEvent(Object plugin, Method method, Session session) {
		try {
			method.invoke(plugin, new Object[] { session });
		} catch (Exception e) {
			logger.warn("kraken msgbus: session event handler should not throw any exception", e);
		}
	}

	/*
	 * Start and stop
	 */

	private void validate() {
		if (msgbus == null) {
			logger.warn("msgbus handler: null msgbus, restart bundle required.");
			return;
		}

		msgbus.register((MessageHandler) this);

		if (sessionHandlers.size() != 0)
			msgbus.register((SessionEventHandler) this);
	}

	private void invalidate() {
		if (msgbus == null)
			return;

		msgbus.unregister((MessageHandler) this);

		if (sessionHandlers.size() != 0)
			msgbus.unregister((SessionEventHandler) this);
	}

	class Handler {
		public Object plugin;
		public Method method;
		public CallbackType type;
		public Set<MsgbusPermission> perms = null;
		public boolean allowGuestAccess = false;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (allowGuestAccess ? 1231 : 1237);
			result = prime * result + ((method == null) ? 0 : method.hashCode());
			result = prime * result + ((perms == null) ? 0 : perms.hashCode());
			result = prime * result + ((plugin == null) ? 0 : plugin.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Handler other = (Handler) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (allowGuestAccess != other.allowGuestAccess)
				return false;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			if (perms == null) {
				if (other.perms != null)
					return false;
			} else if (!perms.equals(other.perms))
				return false;
			if (plugin == null) {
				if (other.plugin != null)
					return false;
			} else if (!plugin.equals(other.plugin))
				return false;
			if (type != other.type)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return plugin + ":" + method;
		}

		private MsgbusPluginHandler getOuterType() {
			return MsgbusPluginHandler.this;
		}
	}
}
