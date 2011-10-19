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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RevLogReader {
	private static final int REV_LOG_SIZE = 34;

	private final Logger logger = LoggerFactory.getLogger(RevLogReader.class.getName());

	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;

	/**
	 * doc file handle
	 */
	private RandomAccessFile docRaf;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public RevLogReader(File logFile, File docFile) throws IOException {
		this.logRaf = new RandomAccessFile(logFile, "r");
		this.docRaf = new RandomAccessFile(docFile, "r");
		this.buffer = new byte[REV_LOG_SIZE];
	}

	public long count() throws IOException {
		// TODO: consider file header size
		return logRaf.length() / REV_LOG_SIZE;
	}

	public RevLog findDoc(int docId) throws IOException {
		for (long i = count() - 1; i >= 0; i--) {
			RevLog log = read(i);
			if (log.getDocId() == docId)
				return log;
		}

		return null;
	}

	public RevLog findRev(long rev) throws IOException {
		for (long i = count() - 1; i >= 0; i--) {
			RevLog log = read(i);
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
		// TODO: consider file header size

		logRaf.seek(index * REV_LOG_SIZE);
		logRaf.read(buffer);
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		return RevLog.deserialize(bb);
	}

	public byte[] readDoc(long offset, int length) throws IOException {
		byte[] buf = new byte[length];

		docRaf.seek(offset);
		docRaf.readInt(); // len
		docRaf.readInt(); // opt
		docRaf.read(buf);

		return buf;
	}

	public void close() {
		try {
			logRaf.close();
			docRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close index file", e);
		}
	}
}
