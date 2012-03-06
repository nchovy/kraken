package org.krakenapps.sqlengine.bdb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.sqlengine.DatabaseHandle;
import org.krakenapps.sqlengine.RowKey;
import org.krakenapps.sqlengine.RowValue;
import org.krakenapps.sqlengine.TableCursor;
import org.krakenapps.sqlengine.Status;
import org.krakenapps.sqlengine.TableHandle;
import org.krakenapps.sqlengine.TableSchemaManager;
import org.krakenapps.sqlparser.ast.AddColumnDefinition;
import org.krakenapps.sqlparser.ast.AlterTableAction;
import org.krakenapps.sqlparser.ast.AlterTableStatement;
import org.krakenapps.sqlparser.ast.ColumnConstraintDefinition;
import org.krakenapps.sqlparser.ast.ColumnDefinition;
import org.krakenapps.sqlparser.ast.DataType;
import org.krakenapps.sqlparser.ast.DropColumnDefinition;
import org.krakenapps.sqlparser.ast.NotNullConstraint;
import org.krakenapps.sqlparser.ast.ReferencesSpecification;
import org.krakenapps.sqlparser.ast.TableDefinition;

public class TableSchemaManagerImpl implements TableSchemaManager {

	private TableHandle tableHandle;

	public TableSchemaManagerImpl(DatabaseHandle databaseHandle) {
		tableHandle = databaseHandle.openTable("_schema", true);
	}

	@Override
	public TableDefinition getTableSchema(String tableName) throws SQLException {
		TableCursor cursor = tableHandle.openCursor();
		try {
			RowKey rowKey = new RowKey(tableName);
			RowValue rowValue = new RowValue();
			Status status = tableHandle.get(rowKey, rowValue);
			if (status != Status.Success)
				return null;

			List<ColumnDefinition> columnDefinitions = deserialize(rowValue.getData());
			TableDefinition def = new TableDefinition(tableName, columnDefinitions);
			return def;
		} finally {
			cursor.close();
		}
	}

	@Override
	public void createTable(TableDefinition definition) throws SQLException {
		RowValue rowValue = getRowValue(definition);
		tableHandle.insert(new RowKey(definition.getTableName()), rowValue);
	}

	private RowValue getRowValue(TableDefinition definition) {
		Object[] columns = serialize(definition.getColumnDefinitions());
		RowValue rowValue = new RowValue(columns);
		return rowValue;
	}

	@Override
	public void alterTable(AlterTableStatement stmt) throws SQLException {
		TableDefinition tableDefinition = getTableSchema(stmt.getTableName());
		if (tableDefinition == null)
			throw new SQLException("table [" + stmt.getTableName() + "] not found");

		AlterTableAction action = stmt.getAction();
		if (action instanceof AddColumnDefinition) {
			String tableName = tableDefinition.getTableName();
			ColumnDefinition newColumnDefinition = ((AddColumnDefinition) action).getColumnDefinition();

			// not null without default value is not allowed
			for (ColumnConstraintDefinition ccd : newColumnDefinition.getConstraints()) {
				if (ccd.getColumnConstraint() instanceof NotNullConstraint)
					// TODO: check also default value
					throw new SQLException("cannot add not null column without default value.");
			}

			// update table schema
			tableDefinition.getColumnDefinitions().add(newColumnDefinition);
			tableHandle.update(new RowKey(tableName), getRowValue(tableDefinition));
		} else if (action instanceof DropColumnDefinition) {
			String tableName = tableDefinition.getTableName();

			DropColumnDefinition dropAction = (DropColumnDefinition) action;
			String dropColumnName = dropAction.getColumnName();

			// check if target column exists
			ColumnDefinition dropColumnDefinition = tableDefinition.findColumnDefinition(dropColumnName);
			if (dropColumnDefinition == null)
				throw new SQLException("column [" + dropColumnName + "] does not exist");

			// check if it is last column
			if (tableDefinition.getColumnDefinitions().size() == 1)
				throw new SQLException("cannot drop all columns of table [" + tableName + "]");

			// check if it is referenced column
			checkIfReferencedTable(tableName, dropColumnName);

			// update table definition
			tableDefinition.getColumnDefinitions().remove(dropColumnDefinition);
			RowValue rowValue = getRowValue(tableDefinition);
			tableHandle.update(new RowKey(tableName), rowValue);
		}
	}

	private void checkIfReferencedTable(String refTableName) throws SQLException {
		TableDefinition refSchema = getTableSchema(refTableName);
		for (ColumnDefinition cd : refSchema.getColumnDefinitions()) {
			checkIfReferencedTable(refTableName, cd.getColumnName());
		}
	}

	private void checkIfReferencedTable(String refTableName, String refColumnName) throws SQLException {
		for (String tableName : getTableNames()) {
			TableDefinition tableDefinition = getTableSchema(tableName);
			if (isReferencedColumn(tableDefinition, refTableName, refColumnName))
				throw new SQLException(String.format("\"%s\" column of \"%s\" table is referenced by \"%s\" table.",
						refColumnName, refTableName, tableDefinition.getTableName()));
		}
	}

	private boolean isReferencedColumn(TableDefinition tableDefinition, String refTableName, String refColumnName) {
		for (ColumnDefinition cd : tableDefinition.getColumnDefinitions()) {
			for (ColumnConstraintDefinition ccd : cd.getConstraints()) {
				if (!(ccd.getColumnConstraint() instanceof ReferencesSpecification))
					continue;

				ReferencesSpecification rs = (ReferencesSpecification) ccd.getColumnConstraint();
				if (rs.getTableName().equals(refTableName) && rs.getColumns().contains(refColumnName))
					return true;
			}
		}

		return false;
	}

	@Override
	public void dropTable(String tableName) throws SQLException {
		// check if it is referenced table
		checkIfReferencedTable(tableName);

		tableHandle.delete(new RowKey(tableName));
	}

	@Override
	public Collection<String> getTableNames() {
		List<String> s = new ArrayList<String>();
		TableCursor cursor = tableHandle.openCursor();
		try {
			RowKey rowKey = new RowKey();
			RowValue rowValue = new RowValue();

			while (true) {
				Status status = cursor.getNext(rowKey, rowValue);
				if (status != Status.Success)
					break;

				s.add((String) rowKey.get());
			}

			return s;
		} finally {
			cursor.close();
		}
	}

	private Object[] serialize(List<ColumnDefinition> defs) {
		Object[] columns = new Object[defs.size()];

		int i = 0;
		for (ColumnDefinition def : defs) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", def.getColumnName());
			m.put("type", def.getDataType().toString());
			m.put("constraints", ConstraintSerializer.serialize(def.getConstraints()));
			columns[i++] = m;
		}

		return columns;
	}

	@SuppressWarnings("unchecked")
	private List<ColumnDefinition> deserialize(Object[] columns) {
		List<ColumnDefinition> defs = new ArrayList<ColumnDefinition>();

		for (Object column : columns) {
			Map<String, Object> m = (Map<String, Object>) column;
			String columnName = (String) m.get("name");
			DataType dataType = DataTypeSerializer.deserialize((String) m.get("type"));
			List<ColumnConstraintDefinition> constraints = ConstraintSerializer.deserialize((Object[]) m.get("constraints"));
			defs.add(new ColumnDefinition(columnName, dataType, constraints));
		}

		return defs;
	}
}
