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
package org.krakenapps.msgbus;

import java.util.Collection;

public interface MessageBus {

	Collection<String> getPluginNames();

	Collection<String> getMethodNames(String pluginName);

	Collection<Session> getSessions();

	@Deprecated
	Session getSession(int id);

	Session getSession(String guid);

	boolean checkPermission(Session session, String group, String code);

	Message execute(Session session, Message message);

	void dispatch(Session session, Message msg);

	void send(Message msg);

	void openSession(Session session);

	void closeSession(Session session);

	void register(MessageHandler handler);

	void unregister(MessageHandler handler);

	void register(SessionEventHandler callback);

	void unregister(SessionEventHandler callback);

	void addMessageListener(MessageListener listener);

	void removeMessageListener(MessageListener listener);

	void setSessionTimeout(int minutes);

	int getSessionTimeout();
}
