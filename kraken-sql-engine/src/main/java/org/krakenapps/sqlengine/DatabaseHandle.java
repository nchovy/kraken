package org.krakenapps.sqlengine;

import java.sql.SQLException;

public interface DatabaseHandle {
	String getName();

	int execute(Session session, String sql) throws SQLException;

	CursorHandle openFor(Session session, String sql) throws SQLException;

	TableSchemaManager getTableSchemaManager();

	TableHandle openTable(String tableName);

	TableHandle openTable(String tableName, boolean allowCreate);

	TableHandle createTable(String tableName);

	void close();

}
