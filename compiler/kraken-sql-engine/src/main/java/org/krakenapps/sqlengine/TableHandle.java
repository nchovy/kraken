package org.krakenapps.sqlengine;

public interface TableHandle {
	TableCursor openCursor();
	
	Status get(RowKey rowKey, RowValue rowValue);

	void insert(RowKey rowKey, RowValue rowValue);

	void update(RowKey rowKey, RowValue rowValue);

	void delete(RowKey rowKey);

	void addListener(TableHandleEventListener callback);

	void removeListener(TableHandleEventListener callback);

	void close();

}
