package org.krakenapps.msgbus;

import java.net.InetAddress;
import java.util.Locale;

public interface Session {
	int getId();

	@Deprecated
	Integer getOrgId();

	String getOrgDomain();

	@Deprecated
	Integer getAdminId();

	String getAdminLoginName();

	InetAddress getLocalAddress();

	InetAddress getRemoteAddress();

	Locale getLocale();

	boolean has(String key);

	Object get(String key);

	String getString(String key);

	Integer getInt(String key);

	void setProperty(String key, Object value);

	void unsetProperty(String key);

	void send(Message msg);

	void close();
}
