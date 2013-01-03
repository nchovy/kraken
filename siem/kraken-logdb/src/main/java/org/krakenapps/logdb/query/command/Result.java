/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.logdb.query.command;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.codec.FastEncodingRule;
import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.LogResultSet;
import org.krakenapps.logstorage.file.LogFileReaderV2;
import org.krakenapps.logstorage.file.LogFileWriterV2;
import org.krakenapps.logstorage.file.LogRecord;
import org.krakenapps.logstorage.file.LogRecordCursor;

public class Result extends LogQueryCommand {
	private static File BASE_DIR = new File(System.getProperty("kraken.data.dir"), "kraken-logdb/query/");
	private LogFileWriterV2 writer;
	private File indexPath;
	private File dataPath;
	private long count;

	private Set<LogQueryCallback> callbacks;
	private Queue<LogQueryCallbackInfo> callbackQueue;
	private Integer nextCallback;
	private long nextStatusChangeCallback;

	/**
	 * index and data file is deleted by user request
	 */
	private volatile boolean purged;

	public Result() throws IOException {
		callbacks = new CopyOnWriteArraySet<LogQueryCallback>();
		callbackQueue = new PriorityQueue<Result.LogQueryCallbackInfo>(11, new CallbackInfoComparator());

		indexPath = File.createTempFile("result", ".idx", BASE_DIR);
		dataPath = File.createTempFile("result", ".dat", BASE_DIR);
		writer = new LogFileWriterV2(indexPath, dataPath, 1024 * 1024, 1);
		BASE_DIR.mkdirs();
	}

	private class LogQueryCallbackInfo {
		private int size;
		private LogQueryCallback callback;

		public LogQueryCallbackInfo(LogQueryCallback callback) {
			this.size = callback.offset() + callback.limit();
			this.callback = callback;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((callback == null) ? 0 : callback.hashCode());
			result = prime * result + size;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LogQueryCallbackInfo other = (LogQueryCallbackInfo) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (callback == null) {
				if (other.callback != null)
					return false;
			} else if (!callback.equals(other.callback))
				return false;
			if (size != other.size)
				return false;
			return true;
		}

		private Result getOuterType() {
			return Result.this;
		}
	}

	private class CallbackInfoComparator implements Comparator<LogQueryCallbackInfo> {
		@Override
		public int compare(LogQueryCallbackInfo o1, LogQueryCallbackInfo o2) {
			return o1.size - o2.size;
		}
	}

	@Override
	public void push(LogMap m) {
		try {
			synchronized (writer) {
				writer.write(convert(m.map()));
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		count++;

		while (nextCallback != null && count >= nextCallback) {
			LogQueryCallback callback = callbackQueue.poll().callback;
			callback.onPageLoaded();
			if (callbackQueue.isEmpty())
				nextCallback = null;
			else
				nextCallback = callbackQueue.peek().size;
		}

		if (nextStatusChangeCallback < System.currentTimeMillis()) {
			for (LogQueryCallback callback : logQuery.getLogQueryCallback())
				callback.onQueryStatusChange();
			nextStatusChangeCallback = System.currentTimeMillis() + 2000;
		}
	}

	private LogRecord convert(Map<String, Object> m) {
		FastEncodingRule enc = new FastEncodingRule();
		ByteBuffer bb = enc.encode(m);
		LogRecord logdata = new LogRecord(new Date(), count + 1, bb);
		return logdata;
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	public void registerCallback(LogQueryCallback callback) {
		callbacks.add(callback);
		callbackQueue.add(new LogQueryCallbackInfo(callback));
		nextCallback = this.callbackQueue.peek().size;
	}

	public void unregisterCallback(LogQueryCallback callback) {
		callbacks.remove(callback);
		callbackQueue.remove(new LogQueryCallbackInfo(callback));
		nextCallback = null;
		if (!this.callbackQueue.isEmpty())
			nextCallback = this.callbackQueue.peek().size;
	}

	public LogResultSet getResult() throws IOException {
		if (purged) {
			String msg = "query result file is already purged, index=" + indexPath.getAbsolutePath() + ", data="
					+ dataPath.getAbsolutePath();
			throw new IOException(msg);
		}

		synchronized (writer) {
			writer.flush();
			writer.sync();
		}

		LogFileReaderV2 reader = new LogFileReaderV2(indexPath, dataPath);
		return new LogResultSetImpl(reader);
	}

	@Override
	public void eof() {
		this.status = Status.Finalizing;

		try {
			synchronized (writer) {
				writer.close();
			}
		} catch (IOException e) {
		}

		super.eof();
		for (LogQueryCallback callback : callbacks)
			callback.onEof();
	}

	public void purge() {
		purged = true;

		// clear query callbacks
		callbacks.clear();
		callbackQueue.clear();
		nextCallback = null;

		// delete files
		indexPath.delete();
		dataPath.delete();
	}

	private static class LogResultSetImpl implements LogResultSet {
		private LogFileReaderV2 reader;
		private LogRecordCursor cursor;

		public LogResultSetImpl(LogFileReaderV2 reader) {
			this.reader = reader;
			this.cursor = reader.getCursor(true);
		}

		@Override
		public long size() {
			return reader.count();
		}

		@Override
		public boolean hasNext() {
			return cursor.hasNext();
		}

		@Override
		public Map<String, Object> next() {
			LogRecord next = cursor.next();
			return EncodingRule.decodeMap(next.getData());
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void skip(long n) {
			cursor.skip(n);
		}

		@Override
		public void close() {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
	}
}
