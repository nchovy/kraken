package org.krakenapps.confdb;

import java.util.List;

public interface ConfigDatabase {
	String getName();

	ConfigCollection ensureCollection(String name);

	void dropCollection(String name);

	List<CommitLog> getCommitLogs();
}
