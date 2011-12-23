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

import org.krakenapps.confdb.CommitOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RevLogWriter {
	private static final byte[] MAGIC_STRING = "KRAKEN_CONFDB".getBytes();
	private static final int COL_LOG_SIZE = 34;

	private final Logger logger = LoggerFactory.getLogger(RevLogReader.class.getName());

	/**
	 * collection log file handle
	 */
	private RandomAccessFile logRaf;
	private int logHeaderLength;

	/**
	 * doc file handle
	 */
	private RandomAccessFile datRaf;
	private int datHeaderLength;

	/**
	 * collection log buffer
	 */
	private byte[] buffer;

	public RevLogWriter(File logFile, File datFile) throws IOException {
		this(logFile, datFile, null, null);
	}

	public RevLogWriter(File logFile, File datFile, byte[] logOption, byte[] datOption) throws IOException {
		logFile.getParentFile().mkdirs();
		datFile.getParentFile().mkdirs();
		boolean logExists = logFile.exists();
		boolean datExists = datFile.exists();

		if (logExists && logOption != null)
			logger.info("kraken confdb: log file already exist. ignore log option.");
		if (datExists && datOption != null)
			logger.info("kraken confdb: dat file already exist. ignore dat option.");

		if (logOption == null)
			logOption = new byte[0];
		if (datOption == null)
			datOption = new byte[0];

		this.logRaf = new RandomAccessFile(logFile, "rw");
		if (!logExists) {
			byte[] b = Arrays.copyOf(MAGIC_STRING, 16);
			b[13] = 0x2; // version 1, log
			b[14] = (byte) ((logOption.length >> 8) & 0xFF);
			b[15] = (byte) (logOption.length & 0xFF);
			this.logRaf.write(b);
			this.logRaf.write(logOption);
			this.logHeaderLength = 16 + logOption.length;
		} else {
			this.logRaf.seek(14);
			byte b1 = this.logRaf.readByte();
			byte b2 = this.logRaf.readByte();
			this.logHeaderLength = 16 + ((b1 & 0xFF) << 8) + (b2 & 0xFF);
		}

		this.datRaf = new RandomAccessFile(datFile, "rw");
		if (!datExists) {
			byte[] b = Arrays.copyOf(MAGIC_STRING, 16);
			b[13] = 0x3; // version 1, dat
			b[14] = (byte) ((datOption.length >> 8) & 0xFF);
			b[15] = (byte) (datOption.length & 0xFF);
			this.datRaf.write(b);
			this.datRaf.write(datOption);
			this.datHeaderLength = 16 + datOption.length;
		} else {
			this.datRaf.seek(14);
			byte b1 = this.datRaf.readByte();
			byte b2 = this.datRaf.readByte();
			this.datHeaderLength = 16 + ((b1 & 0xFF) << 8) + (b2 & 0xFF);
		}

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
		log.setDocOffset(datSize - datHeaderLength);
		log.setDocLength(doc == null ? 0 : doc.length);

		long logSize = logRaf.length();
		if (log.getOperation() == CommitOp.CreateDoc)
			log.setDocId((int) ((logSize - logHeaderLength) / COL_LOG_SIZE) + 1);

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
