package org.krakenapps.eventstorage.engine;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.confdb.CollectionName;

@CollectionName("table")
public class EventTableSchema {
	private int id;
	private String name;
	private Map<String, Object> metadata;

	public EventTableSchema() {
	}

	public EventTableSchema(int id, String name) {
		this.id = id;
		this.name = name;
		this.metadata = new HashMap<String, Object>();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}
}
