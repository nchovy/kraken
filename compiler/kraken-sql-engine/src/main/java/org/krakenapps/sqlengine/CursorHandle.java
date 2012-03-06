package org.krakenapps.sqlengine;

import java.sql.ResultSetMetaData;
import java.util.Iterator;

import org.krakenapps.sqlengine.bdb.Row;

public interface CursorHandle {
	ResultSetMetaData getMetadata();

	Iterator<Row> getRowIterator();
}