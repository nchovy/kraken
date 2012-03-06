package org.krakenapps.sqlengine.bdb;

import java.sql.ResultSetMetaData;
import java.util.Iterator;

import org.krakenapps.sqlengine.CursorHandle;

public class CursorImpl implements CursorHandle {
	private ResultSetMetaData metadata;
	private Iterator<Row> it;

	public CursorImpl(ResultSetMetaData metadata, Iterator<Row> it) {
		this.metadata = metadata;
		this.it = it;
	}

	public ResultSetMetaData getMetadata() {
		return metadata;
	}

	public Iterator<Row> getRowIterator() {
		return it;
	}
}
