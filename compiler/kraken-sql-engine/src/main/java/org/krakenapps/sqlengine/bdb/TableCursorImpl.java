package org.krakenapps.sqlengine.bdb;

import java.nio.ByteBuffer;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.sqlengine.RowKey;
import org.krakenapps.sqlengine.RowValue;
import org.krakenapps.sqlengine.TableCursor;
import org.krakenapps.sqlengine.Status;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class TableCursorImpl implements TableCursor {
	private Cursor cursor;

	public TableCursorImpl(Cursor cursor) {
		this.cursor = cursor;
	}

	@Override
	public Status getNext(RowKey rowKey, RowValue rowValue) {
		DatabaseEntry k = new DatabaseEntry();
		DatabaseEntry v = new DatabaseEntry();

		OperationStatus status = cursor.getNext(k, v, LockMode.DEFAULT);
		if (status == OperationStatus.SUCCESS) {
			ByteBuffer kbuf = ByteBuffer.wrap(k.getData());
			ByteBuffer vbuf = ByteBuffer.wrap(v.getData());
			Object key = EncodingRule.decode(kbuf);
			Object[] value = EncodingRule.decodeArray(vbuf);

			rowKey.set(key);
			rowValue.set(value);
		}

		return StatusConverter.convert(status);
	}

	@Override
	public void close() {
		cursor.close();
	}
}
