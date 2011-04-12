package org.krakenapps.sqlparser.ast;

public class AddColumnDefinition implements AlterTableAction {
	private ColumnDefinition columnDefinition;

	public AddColumnDefinition(ColumnDefinition columnDefinition) {
		this.columnDefinition = columnDefinition;
	}

	public ColumnDefinition getColumnDefinition() {
		return columnDefinition;
	}

	@Override
	public String toString() {
		return "ADD COLUMN " + columnDefinition;
	}
}
