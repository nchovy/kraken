/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RevLogReader {
	private static final byte[] MAGIC_STRING = "KRAKEN_CONFDB".getBytes();
	private static final int REV_LOG_SIZE = 34;

	private final Logger logger = LoggerFactory.getLogger(RevLogReader.class.getName());

	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;
	private int logHeaderLength;

	/**
	 * doc file handle
	 */
	private RandomAccessFile docRaf;
	private int docHeaderLength;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public RevLogReader(File logFile, File docFile) throws IOException {
		this.logRaf = new RandomAccessFile(logFile, "r");
		byte[] logHeader = new byte[16];
		this.logRaf.read(logHeader);
		if (!Arrays.equals(Arrays.copyOf(logHeader, 13), MAGIC_STRING))
			throw new IOException("invalid log file");
		if (logHeader[13] != 0x2)
			throw new IOException("invalid log file version");
		this.logHeaderLength = 16 + ((logHeader[14] & 0xFF) << 8 + (logHeader[15] & 0xFF));

		this.docRaf = new RandomAccessFile(docFile, "r");
		byte[] docHeader = new byte[16];
		this.docRaf.read(docHeader);
		if (!Arrays.equals(Arrays.copyOf(docHeader, 13), MAGIC_STRING))
			throw new IOException("invalid doc file");
		if (docHeader[13] != 0x3)
			throw new IOException("invalid doc file version");
		this.docHeaderLength = 16 + ((docHeader[14] & 0xFF) << 8 + (docHeader[15] & 0xFF));

		this.buffer = new byte[REV_LOG_SIZE];
	}

	public long count() throws IOException {
		return (logRaf.length() - logHeaderLength) / REV_LOG_SIZE;
	}

	public RevLog findDoc(int docId) throws IOException {
		Iterator<RevLog> it = iterator();
		while (it.hasNext()) {
			RevLog log = it.next();
			if (log.getDocId() == docId)
				return log;
		}

		return null;
	}

	public RevLog findRev(long rev) throws IOException {
		Iterator<RevLog> it = iterator();
		while (it.hasNext()) {
			RevLog log = it.next();
			if (log.getRev() == rev)
				return log;
		}

		return null;
	}

	/**
	 * read() does not return doc binary data. You should explicitly read doc
	 * binary using readDoc()
	 * 
	 * @param index
	 *            the index of items
	 * @return the revision log
	 * @throws IOException
	 */
	public RevLog read(long index) throws IOException {
		logRaf.seek(logHeaderLength + index * REV_LOG_SIZE);
		logRaf.read(buffer);
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		return RevLog.deserialize(bb);
	}

	public byte[] readDoc(long offset, int length) throws IOException {
		byte[] buf = new byte[length];

		// skip also len(4) and option(4) field
		docRaf.seek(docHeaderLength + offset + 8);
		docRaf.read(buf);

		return buf;
	}

	public ListIterator<RevLog> iterator() throws IOException {
		return new RevLogIterator();
	}

	public ListIterator<RevLog> iterator(long index) throws IOException {
		return new RevLogIterator(index);
	}

	public void close() {
		try {
			logRaf.close();
			docRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close index file", e);
		}
	}

	public class RevLogIterator implements ListIterator<RevLog> {
		private long count;
		private long index;

		public RevLogIterator() throws IOException {
			this(0L);
		}

		public RevLogIterator(long index) throws IOException {
			this.count = count();
			this.index = this.count - index;
		}

		@Override
		public boolean hasNext() {
			return (index > 0);
		}

		@Override
		public RevLog next() {
			try {
				return read(--index);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasPrevious() {
			try {
				if (index < count)
					this.count = count();
			} catch (IOException e) {
			}
			return (index < count);
		}

		@Override
		public RevLog previous() {
			try {
				return read(index++);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int nextIndex() {
			return (int) (index - 1);
		}

		@Override
		public int previousIndex() {
			return (int) index;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(RevLog e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(RevLog e) {
			throw new UnsupportedOperationException();
		}
	}
}
