/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.krakenapps.eventstorage.EventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileWriter {
	private Logger logger = LoggerFactory.getLogger(FileWriter.class);
	private int tableId;
	private File file;

	private volatile boolean modified = false;
	private Date lastWrite = new Date();
	private Date lastFlush = new Date();
	private volatile boolean closed = false;

	protected FileWriter(int tableId, File file) {
		this.tableId = tableId;
		this.file = file;
	}

	protected EventFileHeader getHeader(String magicString) throws IOException {
		EventFileHeader hdr = null;
		if (file.length() == 0) {
			hdr = new EventFileHeader((short) 1, magicString);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(hdr.serialize());
			} finally {
				if (fos != null)
					fos.close();
			}
		} else {
			hdr = EventFileHeader.extractHeader(file);
			if (!hdr.magicString().equals(magicString))
				throw new IllegalStateException("invalid magic string " + file.getAbsolutePath());
			if (hdr.version() != 1)
				throw new IllegalStateException("invalid version " + file.getAbsolutePath());
		}
		return hdr;
	}

	public int getTableId() {
		return tableId;
	}

	public File getFile() {
		return file;
	}

	public final void write(EventRecord record) throws IOException {
		logger.debug("kraken eventstorage: write record table [{}] file [{}]", tableId, file.getName());
		if (closed)
			throw new IllegalStateException("closed");
		modified = true;
		lastWrite = new Date();
		doWrite(record);
	}

	protected abstract void doWrite(EventRecord record) throws IOException;

	public final synchronized void flush(boolean sync) throws IOException {
		logger.debug("kraken eventstorage: flush writer table [{}] file [{}]", tableId, file.getName());
		if (closed)
			throw new IllegalStateException("closed");
		lastFlush = new Date();
		if (modified)
			doFlush(sync);
		modified = false;
	}

	protected abstract void doFlush(boolean sync) throws IOException;

	protected final void touch() {
		lastWrite = new Date();
	}

	protected final void modify() {
		modified = true;
	}

	public Date getLastWriteTime() {
		return lastWrite;
	}

	public Date getLastFlushTime() {
		return lastFlush;
	}

	public boolean isClosed() {
		return closed;
	}

	public final void close() {
		if (isClosed())
			return;

		logger.debug("kraken eventstorage: close writer table [{}] file [{}]", tableId, file.getName());
		if (modified) {
			try {
				flush(true);
			} catch (IOException e) {
				logger.warn("kraken eventstorage: flush failed [{}] file [{}]", tableId, file.getName());
			}
		}
		closed = true;
		doClose();
	}

	protected abstract void doClose();
}
