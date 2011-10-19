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

import org.krakenapps.confdb.CommitOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevLogWriter {
	private final Logger logger = LoggerFactory.getLogger(RevLogReader.class.getName());
	private static final int COL_LOG_SIZE = 34;

	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;

	/**
	 * doc file handle
	 */
	private RandomAccessFile datRaf;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public RevLogWriter(File logFile, File datFile) throws IOException {
		logFile.getParentFile().mkdirs();
		datFile.getParentFile().mkdirs();
		
		this.logRaf = new RandomAccessFile(logFile, "rw");
		this.datRaf = new RandomAccessFile(datFile, "rw");
		this.buffer = new byte[COL_LOG_SIZE];

		// TODO: check signature and collection metadata (e.g. version, name)
	}

	public int write(RevLog log) throws IOException {
		byte[] doc = log.getDoc();

		// append doc binary data
		long datSize = datRaf.length();
		datRaf.seek(datSize);
		datRaf.writeInt(doc == null ? 0 : doc.length);
		datRaf.writeInt(0); // option
		if (doc != null)
			datRaf.write(doc);

		// append collection log
		log.setDocOffset(datSize);
		log.setDocLength(doc == null ? 0 : doc.length);

		long logSize = logRaf.length();
		if (log.getOperation() == CommitOp.CreateDoc)
			log.setDocId((int) (logSize / COL_LOG_SIZE) + 1);

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		log.serialize(bb);
		logRaf.seek(logSize);
		logRaf.write(bb.array());

		sync();

		return log.getDocId();
	}

	private void sync() throws IOException {
		datRaf.getFD().sync();
		logRaf.getFD().sync();
	}

	public void close() {
		try {
			logRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close collection log file", e);
		}

		try {
			datRaf.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close doc file", e);
		}
	}

}
