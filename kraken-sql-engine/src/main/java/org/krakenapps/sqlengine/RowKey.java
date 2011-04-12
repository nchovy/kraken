package org.krakenapps.sqlengine;

public class RowKey {
	private Object value;
	
	public RowKey() {
	}

	public RowKey(Object value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}
	
	public void set(Object value) {
		this.value = value;
	}
}
