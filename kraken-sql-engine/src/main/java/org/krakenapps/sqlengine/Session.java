package org.krakenapps.sqlengine;

import java.sql.SQLException;

public interface Session {
	String getDatabaseName();

	CursorHandle openFor(String sql) throws SQLException;
	
	int executeUpdate(String sql) throws SQLException;
}