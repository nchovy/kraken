/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.msgbus.Session;
import org.krakenapps.webconsole.CometSessionStore;

@Component(name = "comet-session-store")
@Provides
public class CometSessionStoreImpl implements CometSessionStore {

	private ConcurrentMap<String, SessionState> sessions;

	public CometSessionStoreImpl() {
		sessions = new ConcurrentHashMap<String, SessionState>();
	}

	@Override
	public Session find(String sessionKey) {
		SessionState state = sessions.get(sessionKey);
		if (state == null)
			return null;

		return state.session;
	}

	@Override
	public void register(String sessionKey, Session session) {
		sessions.put(sessionKey, new SessionState(session));
	}

	@Override
	public void set(String sessionKey, String key, Object value) {
		SessionState state = sessions.get(sessionKey);
		if (state == null)
			return;

		state.params.put(key, value);
	}

	@Override
	public boolean containsKey(String sessionKey, String key) {
		SessionState state = sessions.get(sessionKey);
		if (state == null)
			return false;

		return state.params.containsKey(key);
	}

	@Override
	public void unset(String sessionKey, String key) {
		SessionState state = sessions.get(sessionKey);
		if (state == null)
			return;

		state.params.remove(key);
	}

	@Override
	public Object get(String sessionKey, String key) {
		SessionState state = sessions.get(sessionKey);
		if (state == null)
			return null;

		return state.params.get(key);
	}

	@Override
	public void unregister(String sessionKey) {
		sessions.remove(sessionKey);

	}

	private static class SessionState {
		private Session session;
		private ConcurrentMap<String, Object> params;

		public SessionState(Session session) {
			this.session = session;
			this.params = new ConcurrentHashMap<String, Object>();
		}
	}
}
