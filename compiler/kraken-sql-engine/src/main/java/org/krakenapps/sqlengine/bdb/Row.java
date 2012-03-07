package org.krakenapps.sqlengine.bdb;

import java.util.ArrayList;
import java.util.List;

public class Row {
	private List<Object> elements = new ArrayList<Object>();

	public void add(Object o) {
		elements.add(o);
	}

	public Object get(int i) {
		return elements.get(i);
	}
}
