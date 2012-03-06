package org.krakenapps.sqlparser.ast;

public class DropColumnDefinition implements AlterTableAction {
	private String columnName;

	public DropColumnDefinition(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	@Override
	public String toString() {
		return "DROP COLUMN " + columnName;
	}
}
