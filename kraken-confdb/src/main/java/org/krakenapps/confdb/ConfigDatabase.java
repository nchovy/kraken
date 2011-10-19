package org.krakenapps.confdb;

import java.util.List;
import java.util.Set;

public interface ConfigDatabase {
	String getName();

	Set<String> getCollectionNames();

	ConfigCollection ensureCollection(String name);

	void dropCollection(String name);

	List<CommitLog> getCommitLogs();
}
