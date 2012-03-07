package org.krakenapps.sqlengine.bdb;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.sqlengine.RowKey;
import org.krakenapps.sqlengine.RowValue;
import org.krakenapps.sqlengine.Status;
import org.krakenapps.sqlengine.TableCursor;
import org.krakenapps.sqlengine.TableHandle;
import org.krakenapps.sqlengine.TableHandleEventListener;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Table handle is not thread-safe.
 * 
 * @author xeraph
 * 
 */
public class TableHandleImpl implements TableHandle {
	private Database db;

	private Set<TableHandleEventListener> callbacks;

	public TableHandleImpl(Database db) {
		this.db = db;
		this.callbacks = new HashSet<TableHandleEventListener>();
	}

	@Override
	public TableCursor openCursor() {
		Cursor cursor = db.openCursor(null, new CursorConfig());
		return new TableCursorImpl(cursor);
	}

	@Override
	public Status get(RowKey rowKey, RowValue rowValue) {
		DatabaseEntry k = encodeKey(rowKey.get());
		DatabaseEntry v = new DatabaseEntry();
		OperationStatus ret = db.get(null, k, v, LockMode.DEFAULT);
		if (ret == OperationStatus.SUCCESS) {
			ByteBuffer bb = ByteBuffer.wrap(v.getData());
			Object[] data = EncodingRule.decodeArray(bb);
			rowValue.set(data);
		}

		return StatusConverter.convert(ret);
	}

	@Override
	public void insert(RowKey rowKey, RowValue rowValue) {
		DatabaseEntry k = encodeKey(rowKey.get());
		DatabaseEntry v = encodeData(rowValue.getData());

		db.put(null, k, v);
	}

	@Override
	public void update(RowKey rowKey, RowValue rowValue) {
		DatabaseEntry k = encodeKey(rowKey.get());
		DatabaseEntry v = encodeData(rowValue.getData());
		DatabaseEntry old = new DatabaseEntry();

		OperationStatus ret = db.get(null, k, old, LockMode.DEFAULT);
		if (ret == OperationStatus.SUCCESS)
			db.delete(null, k);

		db.put(null, k, v);
	}

	@Override
	public void delete(RowKey rowKey) {
		DatabaseEntry k = encodeKey(rowKey.get());
		db.delete(null, k);
	}

	private DatabaseEntry encodeData(Object[] data) {
		int len = EncodingRule.length(data);
		ByteBuffer bb = ByteBuffer.allocate(len);
		EncodingRule.encode(bb, data);
		DatabaseEntry v = new DatabaseEntry(bb.array());
		return v;
	}

	private DatabaseEntry encodeKey(Object key) {
		ByteBuffer keybuf = ByteBuffer.allocate(EncodingRule.length(key));
		EncodingRule.encode(keybuf, key);
		DatabaseEntry k = new DatabaseEntry(keybuf.array());
		return k;
	}

	@Override
	public void addListener(TableHandleEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(TableHandleEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public void close() {
		db.close();

		// invoke close callbacks
		for (TableHandleEventListener callback : callbacks)
			callback.onClose(this);
	}
}
