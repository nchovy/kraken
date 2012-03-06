package org.krakenapps.servlet.csv;

import java.util.ArrayList;
import java.util.List;

public class CsvRow {
	private List<String> columns = new ArrayList<String>();
	
	public void add(String value) {
		columns.add(value);
	}
	
	public String get(int index) {
		return columns.get(index);
	}
	
	public int size() {
		return columns.size();
	}
}
