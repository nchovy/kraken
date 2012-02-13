package org.krakenapps.msgbus;

import java.util.Collection;
import java.util.Locale;

public interface MessageBus {
	Collection<String> getPackageKeys();

	Collection<String> getPluginNames();

	Collection<String> getMethodNames(String pluginName);

	Collection<Session> getSessions();

	String getPackageName(String key);

	String getPackageName(String key, Locale locale);

	Session getSession(int id);

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

	void register(PackageMetadataProvider provider);

	void unregister(PackageMetadataProvider provider);
}
