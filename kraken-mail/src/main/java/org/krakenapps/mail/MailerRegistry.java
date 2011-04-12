package org.krakenapps.mail;

import java.util.Map;
import java.util.Properties;

import javax.mail.Session;

public interface MailerRegistry {
	Map<String, Properties> getConfigs();

	Properties getConfig(String name);

	void register(String name, Properties props);

	void unregister(String name);

	Session getSession(String name);
}
