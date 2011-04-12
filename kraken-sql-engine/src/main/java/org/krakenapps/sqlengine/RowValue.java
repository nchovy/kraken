package org.krakenapps.sqlengine;

public class RowValue {
	private Object[] data;

	public RowValue() {
	}
	
	public RowValue(Object[] data) {
		this.data = data;
	}
	
	public Object[] getData() {
		return data;
	}

	public Object get(int i) {
		return data[i];
	}

	public void set(Object[] data) {
		this.data = data;
	}
}
