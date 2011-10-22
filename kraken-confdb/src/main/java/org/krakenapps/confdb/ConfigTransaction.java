package org.krakenapps.confdb;

public interface ConfigTransaction {
	Manifest getManifest();

	void begin();

	void log(CommitOp operation, String colName, int docId, long rev);

	void commit(String committer, String log);

	void rollback();
}
