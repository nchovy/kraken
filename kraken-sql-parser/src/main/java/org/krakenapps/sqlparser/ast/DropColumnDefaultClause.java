package org.krakenapps.sqlparser.ast;

public class DropColumnDefaultClause implements AlterColumnAction {
	@Override
	public String toString() {
		return "DROP DEFAULT";
	}
}
