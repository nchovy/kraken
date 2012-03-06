package org.krakenapps.sqlengine.bdb;

public class Column {
	private ColumnType type;
	private String name;

	public Column(ColumnType type, String name) {

		this.type = type;
		this.name = name;
	}

	public ColumnType getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
