package org.krakenapps.sqlengine;

import java.sql.SQLException;
import java.util.Collection;

import org.krakenapps.sqlparser.ast.AlterTableStatement;
import org.krakenapps.sqlparser.ast.TableDefinition;

public interface TableSchemaManager {
	Collection<String> getTableNames();

	TableDefinition getTableSchema(String tableName) throws SQLException;

	void createTable(TableDefinition definition) throws SQLException;

	void alterTable(AlterTableStatement stmt) throws SQLException;

	void dropTable(String tableName) throws SQLException;
}
