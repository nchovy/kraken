package org.krakenapps.mail;

import java.util.Map;
import java.util.Properties;

import javax.mail.Store;

public interface PostboxApi {
	Map<String, Properties> getConfigs();

	Properties getConfig(String name);

	void register(String name, Properties props);

	void unregister(String name);

	Store connect(String name);
}
