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
package org.krakenapps.webconsole;

import org.krakenapps.msgbus.Session;

/**
 * Comet channel is short-living object. A comet channel connect and wait for
 * one trap message. If comet channel get an push event, server closes comet
 * channel. Web browser will initiate another comet channel using
 * XMLHttpRequest. Since comet channel is not connect-oriented, it's session
 * data should be persisted in separated manner.
 * 
 * @author xeraph
 * 
 */
public interface CometSessionStore {
	Session find(String sessionKey);

	/**
	 * Replace associated comet channel. Once session key and comet session is
	 * registered, it's session data will persist until explicitly unregistered.
	 * In other words, session data can be survive among short-living channels.
	 * 
	 * @param sessionKey
	 *            the session key
	 * @param session
	 *            the new comet http session
	 */
	void register(String sessionKey, Session session);

	/**
	 * Set comet session parameter.
	 * 
	 * @param sessionKey
	 *            the session key
	 * @param key
	 *            the parameter key
	 * @param value
	 *            the value
	 */
	void set(String sessionKey, String key, Object value);

	/**
	 * Unset comet session parameter.
	 * 
	 * @param sessionKey
	 *            the session key
	 * @param key
	 *            the parameter key
	 */
	void unset(String sessionKey, String key);

	/**
	 * Check if contains specific comet session parameter
	 * 
	 * @param sessionKey
	 *            the session key
	 * @param key
	 *            the parameter key
	 * @return true if exists
	 */
	boolean containsKey(String sessionKey, String key);

	/**
	 * Get comet session parameter
	 * 
	 * @param sessionKey
	 *            the session key
	 * @param key
	 *            the parameter key
	 * @return the value
	 */
	Object get(String sessionKey, String key);

	/**
	 * Unregister and clear all session key related items
	 * 
	 * @param sessionKey
	 *            the session key
	 */
	void unregister(String sessionKey);

}
