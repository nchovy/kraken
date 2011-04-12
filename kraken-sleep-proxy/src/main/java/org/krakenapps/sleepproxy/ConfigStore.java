package org.krakenapps.sleepproxy;

public interface ConfigStore {
	String get(String key);
	
	String get(String key, String def);

	void set(String key, String value);

	void delete(String key);
}
