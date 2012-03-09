package org.krakenapps.msgbus.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.MessageHandler;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.PackageMetadataProvider;
import org.krakenapps.msgbus.PermissionChecker;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.ResourceApi;
import org.krakenapps.msgbus.ResourceHandler;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.SessionEventHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "msgbus")
@Provides
public class MessageBusImpl extends ServiceTracker implements MessageBus {
	private final Logger logger = LoggerFactory.getLogger(MessageBusImpl.class.getName());

	private ConcurrentMap<String, Set<String>> pluginMessageMap;
	private ConcurrentMap<Integer, Session> sessionMap;
	private Set<SessionEventHandler> sessionEventListeners;
	private ConcurrentMap<String, Set<MessageHandler>> messageHandlerMap;
	private ConcurrentMap<String, PackageMetadataProvider> packageProviderMap;
	private Set<PermissionChecker> permissionCheckers = Collections
			.newSetFromMap(new ConcurrentHashMap<PermissionChecker, Boolean>());
	private final ExecutorService threadPool;

	@Requires
	private ResourceApi resourceApi;

	public MessageBusImpl(BundleContext bc) {
		super(bc, PermissionChecker.class.getName(), null);

		pluginMessageMap = new ConcurrentHashMap<String, Set<String>>();
		sessionMap = new ConcurrentHashMap<Integer, Session>();
		sessionEventListeners = Collections.newSetFromMap(new ConcurrentHashMap<SessionEventHandler, Boolean>());
		messageHandlerMap = new ConcurrentHashMap<String, Set<MessageHandler>>();
		packageProviderMap = new ConcurrentHashMap<String, PackageMetadataProvider>();

		threadPool = Executors.newCachedThreadPool();
	}

	@Validate
	public void start() {
		super.open();
	}

	@Invalidate
	public void stop() {
		super.close();
	}

	@Override
	public boolean checkPermission(Session session, String group, String code) {
		for (PermissionChecker checker : permissionCheckers)
			if (!checker.check(session, group, code))
				return false;

		return true;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		PermissionChecker checker = (PermissionChecker) super.addingService(reference);
		permissionCheckers.add(checker);

		logger.trace("kraken msgbus: new permission checker installed, " + checker);
		return checker;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		PermissionChecker checker = (PermissionChecker) service;
		permissionCheckers.remove(checker);

		logger.trace("kraken msgbus: permission checker uninstalled, " + checker);
		super.removedService(reference, service);
	}

	@Override
	public Collection<String> getPackageKeys() {
		return new ArrayList<String>(packageProviderMap.keySet());
	}

	public Collection<String> getPluginNames() {
		Set<String> set = new HashSet<String>();
		for (String pluginName : pluginMessageMap.keySet())
			set.add(pluginName);
		return set;
	}

	public Collection<String> getMethodNames(String pluginName) {
		Set<String> methodTypes = pluginMessageMap.get(pluginName);
		if (methodTypes == null)
			return null;

		Set<String> copySet = new HashSet<String>();
		for (String methodType : methodTypes) {
			copySet.add(methodType);
		}

		return copySet;
	}

	public Collection<Session> getSessions() {
		Set<Session> sessions = new HashSet<Session>();

		for (Integer key : sessionMap.keySet()) {
			Session session = sessionMap.get(key);
			if (session != null)
				sessions.add(session);
		}

		return sessions;
	}

	@Override
	public Session getSession(int id) {
		return sessionMap.get(id);
	}

	@Override
	public String getPackageName(String key) {
		return getPackageName(key, null);
	}

	@Override
	public String getPackageName(String key, Locale locale) {
		PackageMetadataProvider provider = packageProviderMap.get(key);
		if (provider == null)
			return null;

		if (locale == null)
			return provider.getName();

		return provider.getName(locale.getLanguage());
	}

	@Override
	public Message execute(Session session, Message message) {
		if (session == null)
			throw new IllegalArgumentException("session should be not null");
		if (message == null)
			throw new IllegalArgumentException("message should be not null");

		logger.trace("kraken msgbus: executing method [{}]", message.getMethod());

		Set<MessageHandler> handlers = messageHandlerMap.get(message.getMethod());
		if (handlers == null || handlers.size() == 0) {
			session.send(Message.createError(session, message, "msgbus-handler-not-found", message.getMethod()));
			logger.warn("kraken msgbus: handler not found. discarded [{}]", message.getMethod());
			throw new IllegalStateException("msgbus-handler-not-found");
		}

		TaskRunner runner = new TaskRunner(session, message, handlers.iterator().next());
		runner.run();
		return runner.respondMessage;
	}

	@Override
	public void dispatch(Session session, Message message) {
		logger.trace("kraken msgbus: dispatching message [{}]", message.getMethod());

		Set<MessageHandler> handlers = messageHandlerMap.get(message.getMethod());
		if (handlers == null || handlers.size() == 0) {
			session.send(Message.createError(session, message, "msgbus-handler-not-found", message.getMethod()));
			logger.warn("kraken msgbus: handler not found. discarded [{}]", message.getMethod());
			return;
		}

		for (MessageHandler handler : handlers) {
			threadPool.execute(new TaskRunner(session, message, handler));
		}

	}

	@SuppressWarnings("unchecked")
	public void send(Message message) {
		Session session = sessionMap.get(message.getSession());
		if (session == null) {
			logger.warn("kraken msgbus: session not found. [{}] message will be discarded.", message.getMethod());
			return;
		}

		logger.debug("kraken msgbus: sending message [{}] to session [{}]", message.getMethod(), message.getSession());
		Map<String, Object> m = message.getParameters();
		message.setParameters((Map<String, Object>) convert(m));
		session.send(message);
		message.setParameters(m);
	}

	private Object convert(Object value) {
		if (value == null)
			return null;

		if (value instanceof Date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			return sdf.format((Date) value);
		} else if (value instanceof Map) {
			Map<?, ?> m = (Map<?, ?>) value;
			Map<Object, Object> mm = new HashMap<Object, Object>();
			for (Object key : m.keySet())
				mm.put(convert(key), convert(m.get(key)));
			return mm;
		} else if (value instanceof Collection) {
			return convertList((Collection<?>) value);
		} else if (value.getClass().isArray()) {
			try {
				return convertList(Arrays.asList((Object[]) value));
			} catch (ClassCastException e) {
				return value;
			}
		}

		return value;
	}

	private Object convertList(Collection<?> value) {
		List<Object> list = new ArrayList<Object>();
		for (Object obj : value)
			list.add(convert(obj));
		return list;
	}

	public void openSession(Session session) {
		sessionMap.put(session.getId(), session);

		for (SessionEventHandler handler : sessionEventListeners) {
			handler.sessionOpened(session);
		}
	}

	public void closeSession(Session session) {
		if (session == null)
			return;

		sessionMap.remove(session.getId());

		logger.trace("kraken msgbus: session={}, org domain={}, admin login name={} closed", new Object[] { session,
				session.getOrgDomain(), session.getAdminLoginName() });

		for (SessionEventHandler handler : sessionEventListeners) {
			handler.sessionClosed(session);
		}
	}

	public void register(MessageHandler handler) {
		pluginMessageMap.put(handler.getClassName(), new HashSet<String>(handler.getMethodNames()));

		for (String methodName : handler.getMethodNames()) {
			logger.trace("kraken msgbus: {} subscribed.", methodName);
			Set<MessageHandler> handlers = messageHandlerMap.get(methodName);
			if (handlers == null) {
				handlers = Collections.newSetFromMap(new ConcurrentHashMap<MessageHandler, Boolean>());
				messageHandlerMap.put(methodName, handlers);
			}

			handlers.add(handler);
		}
	}

	public void unregister(MessageHandler handler) {
		pluginMessageMap.remove(handler.getClassName());

		for (String messageType : messageHandlerMap.keySet()) {
			Set<MessageHandler> handlers = messageHandlerMap.get(messageType);
			handlers.remove(handler);
		}
	}

	public void register(SessionEventHandler hanlder) {
		sessionEventListeners.add(hanlder);
	}

	public void unregister(SessionEventHandler handler) {
		sessionEventListeners.remove(handler);
	}

	@Override
	public void register(PackageMetadataProvider provider) {
		packageProviderMap.putIfAbsent(provider.getKey(), provider);
	}

	@Override
	public void unregister(PackageMetadataProvider provider) {
		packageProviderMap.remove(provider.getKey());
	}

	private void reduceStackTrace(InvocationTargetException e) {
		Throwable t = e.getTargetException();
		int limit = 0;
		boolean found = false;
		for (StackTraceElement el : t.getStackTrace()) {
			if (el.getClassName().equals("org.krakenapps.msgbus.handler.MsgbusPluginHandler")) {
				found = true;
				break;
			}

			limit++;
		}

		if (found) {
			// remove reflection related call stacks
			limit -= 4;

			t.setStackTrace(Arrays.copyOf(t.getStackTrace(), limit));
		}
	}

	class TaskRunner implements Runnable {
		private Session session;
		private Message message;
		private MessageHandler handler;
		private Message respondMessage;

		public TaskRunner(Session session, Message message, MessageHandler handler) {
			this.session = session;
			this.message = message;
			this.handler = handler;
		}

		@Override
		public void run() {
			invokeMessageHandler(session, message, handler);
		}

		@SuppressWarnings("unchecked")
		private void invokeMessageHandler(Session session, Message message, MessageHandler handler) {
			try {
				respondMessage = Message.createResponse(session, message);
				Request request = new Request(session, message);
				Response response = new Response();
				handler.handleMessage(request, response);
				respondMessage.setParameters(response);
			} catch (SecurityException e) {
				logger.warn("kraken msgbus: security violation [domain={}, admin_login_name={}, method={}]",
						new Object[] { session.getOrgDomain(), session.getAdminLoginName(), message.getMethod() });
				logger.debug("kraken msgbus: security violation stacktrace", e);
				respondMessage = Message.createError(session, message, "security", "Security Violation");
			} catch (IllegalArgumentException e) {
				respondMessage = Message.createError(session, message, "invalid-method-signature",
						"invalid msgbus method signature");
				logger.error("kraken msgbus: illegal msgbus method signature", e);
			} catch (IllegalAccessException e) {
				respondMessage = Message.createError(session, message, "invalid-access", "invalid msgbus access");
				logger.error("kraken msgbus: invalid msgbus access", e);
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof MsgbusException) {
					MsgbusException wce = (MsgbusException) e.getCause();
					String groupId = wce.getGroupId();
					String key = wce.getErrorCode();
					String lang = session.getString("lang");
					if (lang == null)
						lang = "en";

					String errorCode = wce.getErrorCode();

					ResourceHandler resourceHandler = resourceApi.getResourceHandler(groupId);
					String errorMessage = null;
					if (resourceHandler != null)
						errorMessage = resourceHandler.formatText(errorCode, new Locale(lang), wce.getParameters());

					if (errorMessage == null) {
						final String templateErrorMessage = "kraken msgbus: error message template [group_id={}, message_id={}, lang={}] not found";
						logger.warn(templateErrorMessage, new Object[] { groupId, key, lang });
					}

					respondMessage = Message.createError(session, message, errorCode, errorMessage);
					respondMessage.setParameters(wce.getParameters());
				} else {
					respondMessage = Message.createError(session, message, "general-error",
							"invocation target exception");
				}

				reduceStackTrace(e);

				logger.error("kraken msgbus: message handler failed", e);
			} catch (Exception e) {
				respondMessage = Message.createError(session, message, "unknown", "unknown exception");
				logger.error("kraken msgbus: message handler failed", e);
			} finally {
				Map<String, Object> m = respondMessage.getParameters();
				respondMessage.setParameters((Map<String, Object>) convert(m));
				session.send(respondMessage);
				respondMessage.setParameters(m);
			}
		}
	}
}
