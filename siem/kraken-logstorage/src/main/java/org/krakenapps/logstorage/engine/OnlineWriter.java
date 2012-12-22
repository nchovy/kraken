/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.krakenapps.logstorage.file.LogFileWriter;
import org.krakenapps.logstorage.file.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnlineWriter {
	private final Logger logger = LoggerFactory.getLogger(OnlineWriter.class.getName());

	/**
	 * table id
	 */
	private int tableId;

	/**
	 * is in closing state?
	 */
	private boolean closing;

	/**
	 * only yyyy-MM-dd (excluding hour, min, sec, milli)
	 */
	private Date day;

	/**
	 * maintain last write access time. idle writer should be evicted
	 */
	private Date lastAccess = new Date();
	private AtomicLong nextId;

	/**
	 * binary log file writer
	 */
	private LogFileWriter writer;

	public OnlineWriter(int tableId, Date day, int blockSize) throws IOException {
		this(tableId, day, blockSize, null);
	}

	public OnlineWriter(int tableId, Date day, int blockSize, String defaultLogVersion) throws IOException {
		this.tableId = tableId;
		this.day = day;
		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		File dataPath = DatapathUtil.getDataFile(tableId, day);

		indexPath.getParentFile().mkdirs();
		dataPath.getParentFile().mkdirs();

		writer = LogFileWriter.getLogFileWriter(indexPath, dataPath, defaultLogVersion);
		nextId = new AtomicLong(writer.getLastKey());
	}

	public boolean isOpen() {
		return writer != null && closing == false;
	}

	public boolean isClosed() {
		return closing == true && writer == null;
	}

	public int getTableId() {
		return tableId;
	}

	public Date getDay() {
		return day;
	}

	private long nextId() {
		// do NOT update last access here
		return nextId.incrementAndGet();
	}

	public Date getLastAccess() {
		return lastAccess;
	}
	
	public Date getLastFlush() {
		return writer.getLastFlush();
	}

	public void write(LogRecord record) throws IOException {
		synchronized (this) {
			if (writer == null)
				throw new IOException("file closed");

			long nid = nextId();
			record.setId(nid);
			writer.write(record);
			lastAccess = new Date();
		}
	}

	public void write(Collection<LogRecord> logs) throws IOException {
		if (writer == null)
			throw new IllegalStateException("file closed");

		for (LogRecord record : logs) {
			record.setId(nextId());
		}

		synchronized (this) {
			writer.write(logs);
			lastAccess = new Date();
		}
	}

	public List<LogRecord> getBuffer() {
		synchronized (this) {
			return new ArrayList<LogRecord>(writer.getBuffer());
		}
	}

	public void flush() throws IOException {
		if (logger.isTraceEnabled()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			logger.trace("kraken logstorage: flushing log table [{}], day [{}]", tableId, dateFormat.format(day));
		}

		synchronized (this) {
			writer.flush();
			notifyAll();
		}
	}

	public void close() {
		if (closing)
			return;

		try {
			synchronized (this) {
				closing = true;
				flush();
				writer.close();
				notifyAll();
				writer = null;
			}

		} catch (IOException e) {
			logger.error("cannot close online log writer", e);
		}
	}
}