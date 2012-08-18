package org.krakenapps.eventstorage;

public class EventTableNotFoundException extends IllegalStateException {
	private static final long serialVersionUID = 1L;
	private String tableName;

	public EventTableNotFoundException(String tableName) {
		super("table [" + tableName + "] not found");
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}
}
