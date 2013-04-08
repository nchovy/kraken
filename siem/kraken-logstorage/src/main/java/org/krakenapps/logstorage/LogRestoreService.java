package org.krakenapps.logstorage;

public interface LogRestoreService {

	void restoreByDelete();

	void restoreByDelete(String tableName);

	void restoreByFixHeader();

	void restoreByFixHeader(String tableName);

}
