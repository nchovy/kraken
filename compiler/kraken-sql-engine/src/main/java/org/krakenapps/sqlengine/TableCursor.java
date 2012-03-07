package org.krakenapps.sqlengine;

public interface TableCursor {
	Status getNext(RowKey rowKey, RowValue rowValue);

	void close();
}
