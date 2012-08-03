package org.krakenapps.eventstorage;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface EventTableRegistry {
	boolean exists(String tableName);

	Collection<String> getTableNames();

	int getTableId(String tableName);

	String getTableName(int tableId);

	int createTable(String tableName, Map<String, String> tableMetadata);

	void renameTable(String currentName, String newName);

	void dropTable(String tableName);

	Set<String> getTableMetadataKeys(String tableName);

	String getTableMetadata(String tableName, String key);

	void setTableMetadata(String tableName, String key, String value);

	void unsetTableMetadata(String tableName, String key);
}
