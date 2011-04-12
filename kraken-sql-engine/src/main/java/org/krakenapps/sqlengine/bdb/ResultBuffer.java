package org.krakenapps.sqlengine.bdb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ResultBuffer {
	private List<Column> columns = new ArrayList<Column>();
	private List<Row> rows = new LinkedList<Row>();

	public void addColumn(Column col) {
		columns.add(col);
	}

	public void add(Row row) {
		rows.add(row);
	}

}
