package org.krakenapps.sqlengine.bdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.krakenapps.sqlengine.DatabaseHandle;
import org.krakenapps.sqlengine.TableSchemaManager;
import org.krakenapps.sqlparser.ast.AlterTableStatement;
import org.krakenapps.sqlparser.ast.ColumnConstraint;
import org.krakenapps.sqlparser.ast.ColumnConstraintDefinition;
import org.krakenapps.sqlparser.ast.ColumnDefinition;
import org.krakenapps.sqlparser.ast.DescTableStatement;
import org.krakenapps.sqlparser.ast.DropTableStatement;
import org.krakenapps.sqlparser.ast.NotNullConstraint;
import org.krakenapps.sqlparser.ast.PrimaryKeyConstraint;
import org.krakenapps.sqlparser.ast.ReferencesSpecification;
import org.krakenapps.sqlparser.ast.ShowTablesStatement;
import org.krakenapps.sqlparser.ast.TableDefinition;
import org.krakenapps.sqlparser.ast.UniqueConstraint;

public class DdlHandler {

	public static void handleCreateTable(DatabaseHandle handle, TableDefinition stmt) throws SQLException {
		validateCreateTable(handle, stmt);

		TableSchemaManager tsm = handle.getTableSchemaManager();
		tsm.createTable(stmt);
	}

	private static void validateCreateTable(DatabaseHandle handle, TableDefinition stmt) throws SQLException {
		// check duplicated table name
		TableSchemaManager tsm = handle.getTableSchemaManager();
		if (tsm.getTableNames().contains(stmt.getTableName()))
			throw new SQLException("table [" + stmt.getTableName() + "] already exists");

		// check duplicated column name
		Set<String> names = new HashSet<String>();
		for (ColumnDefinition def : stmt.getColumnDefinitions()) {
			if (names.contains(def.getColumnName()))
				throw new SQLException("duplicated column name [" + def.getColumnName() + "]");

			names.add(def.getColumnName());
		}

		// check FK validity (should be same type and pri key)
		for (ColumnDefinition def : stmt.getColumnDefinitions()) {
			checkForeignKeyValidity(tsm, def);
		}

		// force not null to primary key
		for (ColumnDefinition def : stmt.getColumnDefinitions()) {
			forcePrimaryKeyNotNull(def);
		}
	}

	private static void checkForeignKeyValidity(TableSchemaManager tsm, ColumnDefinition columnDefinition)
			throws SQLException {
		for (ColumnConstraintDefinition cdef : columnDefinition.getConstraints()) {
			ColumnConstraint c = cdef.getColumnConstraint();
			if (!(c instanceof ReferencesSpecification))
				continue;

			String columnName = columnDefinition.getColumnName();
			ReferencesSpecification ref = (ReferencesSpecification) c;

			TableDefinition refTableDef = tsm.getTableSchema(ref.getTableName());
			if (refTableDef == null) {
				throw new SQLException(columnName + " references " + ref.getTableName()
						+ " table, but table does not exist");
			}

			for (String refColumnName : ref.getColumns()) {
				ColumnDefinition refCol = findColumn(refColumnName, refTableDef.getColumnDefinitions());
				if (refCol == null)
					throw new SQLException(columnName + " references " + ref.getTableName() + "(" + refColumnName
							+ "), but column does not exist");

				// check if refCol is primary key

				if (!refCol.getDataType().equals(columnDefinition.getDataType()))
					throw new SQLException(columnName + " references " + ref.getTableName() + "(" + refColumnName
							+ "), but data type does not match");
			}
		}
	}

	private static ColumnDefinition findColumn(String name, List<ColumnDefinition> columns) {
		for (ColumnDefinition col : columns)
			if (col.getColumnName().equals(name))
				return col;

		return null;
	}

	private static void forcePrimaryKeyNotNull(ColumnDefinition def) {
		boolean hasNotNull = false;
		boolean hasPrimaryKey = false;
		for (ColumnConstraintDefinition cdef : def.getConstraints()) {
			ColumnConstraint c = cdef.getColumnConstraint();
			if (c instanceof NotNullConstraint)
				hasNotNull = true;

			if (c instanceof PrimaryKeyConstraint)
				hasPrimaryKey = true;
		}

		// force not null
		if (hasPrimaryKey && !hasNotNull)
			def.getConstraints().add(new ColumnConstraintDefinition(new NotNullConstraint()));
	}

	public static void handleAlterTable(DatabaseHandle handle, AlterTableStatement stmt) throws SQLException {
		TableSchemaManager tsm = handle.getTableSchemaManager();

		// check if table exists
		if (!tsm.getTableNames().contains(stmt.getTableName()))
			throw new SQLException("table [" + stmt.getTableName() + "] does not exist");

		tsm.alterTable(stmt);
	}

	public static void handleDropTable(DatabaseHandle handle, DropTableStatement stmt) throws SQLException {
		TableSchemaManager tsm = handle.getTableSchemaManager();

		// check if table exists
		if (!tsm.getTableNames().contains(stmt.getTableName()))
			throw new SQLException("table [" + stmt.getTableName() + "] does not exist");

		// drop
		tsm.dropTable(stmt.getTableName());
	}

	public static Iterator<Row> handleDescTable(DatabaseHandle handle, DescTableStatement stmt, ResultMetadata metadata)
			throws SQLException {
		TableSchemaManager tsm = handle.getTableSchemaManager();
		List<Row> rows = new ArrayList<Row>();

		metadata.addColumn("Column Name");
		metadata.addColumn("Data Type");
		metadata.addColumn("Nullable");
		metadata.addColumn("Key");

		TableDefinition def = tsm.getTableSchema(stmt.getTableName());
		if (def == null)
			return null;

		for (ColumnDefinition cd : def.getColumnDefinitions()) {
			Row row = new Row();
			row.add(cd.getColumnName());
			row.add(cd.getDataType());
			row.add(getNullable(cd.getConstraints()));
			row.add(getKey(cd.getConstraints()));
			rows.add(row);
		}

		return rows.iterator();
	}

	private static String getNullable(List<ColumnConstraintDefinition> defs) {
		for (ColumnConstraintDefinition def : defs)
			if (def.getColumnConstraint() instanceof NotNullConstraint)
				return "N";

		return "Y";
	}

	private static String getKey(List<ColumnConstraintDefinition> defs) {
		for (ColumnConstraintDefinition def : defs) {
			ColumnConstraint c = def.getColumnConstraint();
			if (c instanceof PrimaryKeyConstraint)
				return "PRI";
			if (c instanceof ReferencesSpecification)
				return "FOR";
			if (c instanceof UniqueConstraint)
				return "UNI";
		}
		return "";
	}

	public static Iterator<Row> handleShowTable(DatabaseHandle handle, ShowTablesStatement stmt, ResultMetadata metadata) {
		TableSchemaManager tsm = handle.getTableSchemaManager();
		List<Row> rows = new ArrayList<Row>();

		metadata.addColumn("Table Name");
		int i = 0;
		for (String tableName : tsm.getTableNames()) {
			Row row = new Row();
			row.add(tableName);
			rows.add(row);

			metadata.updateDisplaySize(i, tableName.length());
			i++;
		}
		return rows.iterator();
	}
}
