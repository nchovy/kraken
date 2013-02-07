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
package org.krakenapps.msgbus.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.PushCondition;
import org.krakenapps.msgbus.PushInterceptor;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "msgbus-push-api")
@Provides
public class PushApiImpl implements PushApi {
	private final Logger logger = LoggerFactory.getLogger(PushApiImpl.class.getName());

	@Requires
	private MessageBus msgbus;

	// organization to sessions (id set)
	private ConcurrentMap<String, Set<String>> orgSessionMap = new ConcurrentHashMap<String, Set<String>>();

	// session to processes (id set)
	private ConcurrentMap<String, Set<Binding>> pushBindingsMap = new ConcurrentHashMap<String, Set<Binding>>();

	private ConcurrentMap<String, PushInterceptor> pushInterceptorsMap = new ConcurrentHashMap<String, PushInterceptor>();
	private ConcurrentMap<PushCondition.Key, PushCondition> pushConditions = new ConcurrentHashMap<PushCondition.Key, PushCondition>();

	@Override
	public void subscribe(String orgDomain, int sessionId, int processId, String callback) {
		Map<String, Object> m = new HashMap<String, Object>();
		subscribe(orgDomain, Integer.toString(sessionId), processId, callback, m);
	}

	@Override
	public void subscribe(String orgDomain, int sessionId, int processId, String callback, Map<String, Object> options) {
		subscribe(orgDomain, Integer.toString(sessionId), processId, callback, options);
	}

	@Override
	public void subscribe(String orgDomain, String sessionId, int processId, String callback,
			Map<String, Object> options) {
		Set<String> sessions = orgSessionMap.get(orgDomain);
		if (sessions == null)
			orgSessionMap.putIfAbsent(orgDomain, Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()));

		Set<Binding> bindings = pushBindingsMap.get(sessionId);
		if (bindings == null)
			pushBindingsMap.putIfAbsent(callback, Collections.newSetFromMap(new ConcurrentHashMap<Binding, Boolean>()));

		sessions = orgSessionMap.get(orgDomain);
		sessions.add(sessionId);

		bindings = pushBindingsMap.get(callback);
		Binding binding = new Binding(sessionId, processId);
		bindings.add(binding);

		PushCondition condition = new PushCondition(orgDomain, binding.sessionId, binding.processId, callback, options);
		pushConditions.put(condition.getKey(), condition);

		logger.trace("kraken msgbus: subscribe push, org [{}], session [{}], callback [{}]", new Object[] { orgDomain,
				sessionId, callback });
	}

	@Override
	public void unsubscribe(String orgDomain, int sessionId, int processId, String callback) {
		unsubscribe(orgDomain, Integer.toString(sessionId), processId, callback);

	}

	@Override
	public void unsubscribe(String orgDomain, String sessionId, int processId, String callback) {
		logger.trace("kraken msgbus: unsubscribe push, org [{}], session [{}], callback [{}]", new Object[] {
				orgDomain, sessionId, callback });

		Set<Binding> bindings = pushBindingsMap.get(callback);
		if (bindings != null) {
			bindings.remove(new Binding(sessionId, processId));
			if (bindings.isEmpty()) {
				pushBindingsMap.remove(callback);
			}
		}

		// clear condition
		pushConditions.remove(new PushCondition.Key(orgDomain, sessionId, processId, callback));
	}

	@Override
	public void addInterceptor(String callback, PushInterceptor interceptor) {
		if (pushInterceptorsMap.putIfAbsent(callback, interceptor) != null)
			throw new IllegalStateException("already added");
		logger.trace("dom push api: add interceptor, callback [{}]", new Object[] { callback });
	}

	@Override
	public void removeInterceptor(String callback) {
		pushInterceptorsMap.remove(callback);
		logger.trace("dom push api: remove interceptor, callback [{}]", new Object[] { callback });
	}

	@Override
	public void push(String orgDomain, String callback, Map<String, Object> m) {
		Set<String> sessions = orgSessionMap.get(orgDomain);
		if (sessions == null)
			return;

		Set<Binding> bindings = pushBindingsMap.get(callback);
		if (bindings == null)
			return;

		for (Binding binding : bindings) {
			if (sessions.contains(binding.sessionId)) {
				Message msg = createMessage(orgDomain, binding, callback, m);
				if (logger.isTraceEnabled())
					tracePush(orgDomain, callback, m, binding.sessionId, binding);
				msgbus.send(msg);
			}
		}
	}

	@Override
	public void push(Session session, String callback, Map<String, Object> m) {
		Set<Binding> bindings = pushBindingsMap.get(callback);
		if (bindings == null)
			return;
		for (Binding binding : bindings) {
			if (binding.sessionId.equals(session.getGuid())) {
				String orgDomain = session.getOrgDomain();

				Message msg = createMessage(orgDomain, binding, callback, m);
				if (logger.isTraceEnabled())
					tracePush(orgDomain, callback, m, binding.sessionId, binding);
				msgbus.send(msg);
			}
		}
	}

	private Message createMessage(String orgDomain, Binding binding, String callback, Map<String, Object> m) {
		Message msg = new Message();
		msg.setSession(binding.sessionId);
		msg.setType(Message.Type.Trap);
		msg.setMethod(callback);
		msg.setTarget(Integer.toString(binding.processId));

		Map<String, Object> params = null;
		PushInterceptor interceptor = pushInterceptorsMap.get(callback);
		if (interceptor != null) {
			PushCondition.Key key = new PushCondition.Key(orgDomain, binding.sessionId, binding.processId, callback);
			PushCondition condition = pushConditions.get(key);
			params = interceptor.onPush(condition, m);
		} else {
			params = m;
		}

		for (String key : params.keySet()) {
			msg.getParameters().put(key, params.get(key));
		}

		return msg;
	}

	private void tracePush(String orgDomain, String method, Map<String, Object> m, String sessionId, Binding binding) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : m.keySet()) {
			if (i != 0)
				sb.append(", ");
			sb.append(key);
			sb.append('=');
			sb.append(m.get(key));
			i++;
		}

		final String template = "kraken msgbus: push org [{}], session [{}], process [{}], callback [{}], msg [{}]";
		logger.trace(template, new Object[] { orgDomain, sessionId, binding.processId, method, sb.toString() });
	}

	@Override
	public void sessionClosed(String orgDomain, int sessionId) {
		sessionClosed(orgDomain, Integer.toString(sessionId));

	}

	@Override
	public void sessionClosed(String orgDoamin, String sessionId) {
		logger.trace("kraken msgbus: push session closed, org [{}], session [{}]", orgDoamin, sessionId);
		cleanSession(orgDoamin, sessionId);
	}

	private void cleanSession(String orgDomain, String sessionId) {
		Set<String> bindingsToRemove = Collections.newSetFromMap(new HashMap<String, Boolean>());
		for (Entry<String, Set<Binding>> entry : pushBindingsMap.entrySet()) {
			Set<Binding> bindings = entry.getValue();
			Set<Binding> toRemove = Collections.newSetFromMap(new HashMap<Binding, Boolean>());
			for (Binding binding : bindings) {
				if (binding.sessionId.equals(sessionId))
					toRemove.add(binding);
			}
			bindings.removeAll(toRemove);
			if (bindings.isEmpty())
				bindingsToRemove.add(entry.getKey());
		}
		for (String callback : bindingsToRemove)
			pushBindingsMap.remove(callback);

		Set<String> sessions = orgSessionMap.get(orgDomain);
		if (sessions != null)
			sessions.remove(sessionId);

		// clear all related conditions
		for (PushCondition.Key key : pushConditions.keySet())
			if (key.getOrgDomain() == orgDomain && key.getSessionId() == sessionId)
				pushConditions.remove(key);
	}

	private static class Binding {
		private String sessionId;
		private int processId;

		public Binding(String sessionId, int processId) {
			this.processId = processId;
			this.sessionId = sessionId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + processId;
			result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
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
			Binding other = (Binding) obj;
			if (processId != other.processId)
				return false;
			if (sessionId == null) {
				if (other.sessionId != null)
					return false;
			} else if (!sessionId.equals(other.sessionId))
				return false;
			return true;
		}
	}
}
