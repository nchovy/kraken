package org.krakenapps.confdb;

public interface Config {
	ConfigDatabase getDatabase();

	ConfigCollection getCollection();

	int getId();

	long getRevision();
}
