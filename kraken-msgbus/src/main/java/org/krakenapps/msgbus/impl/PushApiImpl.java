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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "msgbus-push-api")
@Provides
public class PushApiImpl implements PushApi {
	private final Logger logger = LoggerFactory.getLogger(PushApiImpl.class.getName());

	@Requires
	private MessageBus msgbus;

	// organization to sessions (id set)
	private ConcurrentMap<Integer, Set<Integer>> orgSessionMap;

	// session to processes (id set)
	private ConcurrentMap<String, Set<Binding>> pushBindingsMap;

	private ConcurrentMap<String, PushInterceptor> pushInterceptorsMap;

	private ConcurrentMap<PushCondition.Key, PushCondition> pushConditions;

	public PushApiImpl() {
		orgSessionMap = new ConcurrentHashMap<Integer, Set<Integer>>();
		pushBindingsMap = new ConcurrentHashMap<String, Set<Binding>>();
		pushInterceptorsMap = new ConcurrentHashMap<String, PushInterceptor>();
		pushConditions = new ConcurrentHashMap<PushCondition.Key, PushCondition>();
	}

	@Override
	public void push(int orgId, String callback, Map<String, Object> m) {
		Set<Integer> sessions = orgSessionMap.get(orgId);
		if (sessions == null)
			return;

		Set<Binding> bindings = pushBindingsMap.get(callback);
		if (bindings == null)
			return;

		PushInterceptor interceptor = pushInterceptorsMap.get(callback);

		for (Binding binding : bindings) {
			if (sessions.contains(binding.sessionId)) {
				Message msg = new Message();
				msg.setSession(binding.sessionId);
				msg.setType(Message.Type.Trap);
				msg.setMethod(callback);
				msg.setTarget(Integer.toString(binding.processId));

				Map<String, Object> params = null;
				if (interceptor != null) {
					PushCondition.Key key = new PushCondition.Key(orgId, binding.sessionId, binding.processId, callback);
					PushCondition condition = pushConditions.get(key);
					params = interceptor.onPush(condition, m);
				} else {
					params = m;
				}

				for (String key : params.keySet()) {
					msg.getParameters().put(key, params.get(key));
				}

				if (logger.isTraceEnabled()) {
					tracePush(orgId, callback, m, binding.sessionId, binding);
				}

				msgbus.send(msg);
			}
		}
	}

	private void tracePush(int orgId, String method, Map<String, Object> m, Integer sessionId, Binding binding) {
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

		final String template = "dom push api: dispatching org [{}], session [{}], process [{}], callback [{}], msg [{}]";
		logger.trace(template, new Object[] { orgId, sessionId, binding.processId, method, sb.toString() });
	}

	@Override
	public void subscribe(int orgId, int sessionId, int processId, String callback) {
		Map<String, Object> m = new HashMap<String, Object>();
		subscribe(orgId, sessionId, processId, callback, m);
	}

	@Override
	public void subscribe(int orgId, int sessionId, int processId, String callback, Map<String, Object> options) {
		Set<Integer> sessions = orgSessionMap.get(orgId);
		if (sessions == null)
			orgSessionMap.putIfAbsent(orgId, Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>()));

		Set<Binding> bindings = pushBindingsMap.get(sessionId);
		if (bindings == null)
			pushBindingsMap.putIfAbsent(callback, Collections.newSetFromMap(new ConcurrentHashMap<Binding, Boolean>()));

		sessions = orgSessionMap.get(orgId);
		sessions.add(sessionId);

		bindings = pushBindingsMap.get(callback);
		Binding binding = new Binding(sessionId, processId);
		bindings.add(binding);

		PushCondition condition = new PushCondition(orgId, binding.sessionId, binding.processId, callback, options);
		pushConditions.put(condition.getKey(), condition);

		logger.trace("dom push api: subscribe, org [{}], session [{}], callback [{}]", new Object[] { orgId, sessionId,
				callback });
	}

	@Override
	public void unsubscribe(int orgId, int sessionId, int processId, String callback) {
		logger.trace("dom push api: unsubscribe, org [{}], session [{}], callback [{}]", new Object[] { orgId,
				sessionId, callback });

		Set<Binding> bindings = pushBindingsMap.get(callback);
		if (bindings != null) {
			bindings.remove(new Binding(sessionId, processId));
			if (bindings.isEmpty()) {
				pushBindingsMap.remove(callback);
			}
		}

		// clear condition
		pushConditions.remove(new PushCondition.Key(orgId, sessionId, processId, callback));
	}

	@Override
	public void sessionClosed(int orgId, int sessionId) {
		logger.trace("dom push api: session closed, org [{}], session [{}]", orgId, sessionId);
		cleanSession(orgId, sessionId);
	}

	private void cleanSession(int orgId, int sessionId) {
		Set<String> bindingsToRemove = Collections.newSetFromMap(new HashMap<String, Boolean>());
		for (Entry<String, Set<Binding>> entry : pushBindingsMap.entrySet()) {
			Set<Binding> bindings = entry.getValue();
			Set<Binding> toRemove = Collections.newSetFromMap(new HashMap<Binding, Boolean>());
			for (Binding binding : bindings) {
				if (binding.sessionId == sessionId)
					toRemove.add(binding);
			}
			bindings.removeAll(toRemove);
			if (bindings.isEmpty())
				bindingsToRemove.add(entry.getKey());
		}
		for (String callback : bindingsToRemove)
			pushBindingsMap.remove(callback);

		Set<Integer> sessions = orgSessionMap.get(orgId);
		if (sessions != null)
			sessions.remove(sessionId);

		// clear all related conditions
		for (PushCondition.Key key : pushConditions.keySet())
			if (key.getOrgId() == orgId && key.getSessionId() == sessionId)
				pushConditions.remove(key);
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

	private static class Binding {
		private int sessionId;
		private int processId;

		public Binding(int sessionId, int processId) {
			this.processId = processId;
			this.sessionId = sessionId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + processId;
			result = prime * result + sessionId;
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
			if (sessionId != other.sessionId)
				return false;
			return true;
		}
	}
}
