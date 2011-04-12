package org.krakenapps.sqlparser.ast;

public class AlterColumnDefinition implements AlterTableAction {
	private AlterColumnAction action;

	public AlterColumnDefinition(AlterColumnAction action) {
		this.action = action;
	}

	public AlterColumnAction getAction() {
		return action;
	}
}
