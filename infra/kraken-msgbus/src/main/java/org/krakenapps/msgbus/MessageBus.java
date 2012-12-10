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
