package org.krakenapps.confdb;

public interface ConfigDatabase {
	String getName();

	ConfigCollection ensureCollection(String name);

	void dropCollection(String name);
}
