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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
	private FileOutputStream logFileOutput;
	private BufferedOutputStream logOutput;
	private long logFileLength;
	private int logHeaderLength;

	/**
	 * doc file handle
	 */
	private FileOutputStream datFileOutput;
	private BufferedOutputStream datOutput;
	private long datFileLength;
	private final int datHeaderLength;

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

		RandomAccessFile logRaf = new RandomAccessFile(logFile, "rw");
		try {
			if (!logExists) {
				byte[] b = Arrays.copyOf(MAGIC_STRING, 16);
				b[13] = 0x2; // version 1, log
				b[14] = (byte) ((logOption.length >> 8) & 0xFF);
				b[15] = (byte) (logOption.length & 0xFF);
				logRaf.write(b);
				logRaf.write(logOption);
				this.logHeaderLength = 16 + logOption.length;
			} else {
				logRaf.seek(14);
				byte b1 = logRaf.readByte();
				byte b2 = logRaf.readByte();
				this.logHeaderLength = 16 + ((b1 & 0xFF) << 8) + (b2 & 0xFF);
			}
		} finally {
			logRaf.close();
		}

		RandomAccessFile datRaf = new RandomAccessFile(datFile, "rw");
		try {
			if (!datExists) {
				byte[] b = Arrays.copyOf(MAGIC_STRING, 16);
				b[13] = 0x3; // version 1, dat
				b[14] = (byte) ((datOption.length >> 8) & 0xFF);
				b[15] = (byte) (datOption.length & 0xFF);
				datRaf.write(b);
				datRaf.write(datOption);
				this.datHeaderLength = 16 + datOption.length;
			} else {
				datRaf.seek(14);
				byte b1 = datRaf.readByte();
				byte b2 = datRaf.readByte();
				this.datHeaderLength = 16 + ((b1 & 0xFF) << 8) + (b2 & 0xFF);
			}
		} finally {
			datRaf.close();
		}

		this.buffer = new byte[COL_LOG_SIZE];
		this.datFileLength = datFile.length();
		this.logFileLength = logFile.length();

		this.logFileOutput = new FileOutputStream(logFile, true);
		this.datFileOutput = new FileOutputStream(datFile, true);
		this.logOutput = new BufferedOutputStream(logFileOutput);
		this.datOutput = new BufferedOutputStream(datFileOutput);

		// TODO: check signature and collection metadata (e.g. version, name)
	}

	public int write(RevLog log) throws IOException {
		byte[] doc = log.getDoc();

		// append doc binary data
		ByteBuffer hbb = ByteBuffer.allocate(8);
		hbb.putInt(doc == null ? 0 : doc.length);
		hbb.putInt(0); // option

		datOutput.write(hbb.array());
		if (doc != null)
			datOutput.write(doc);

		// append collection log
		log.setDocOffset(datFileLength - datHeaderLength);
		log.setDocLength(doc == null ? 0 : doc.length);

		if (doc != null)
			datFileLength += 8 + doc.length;

		if (log.getOperation() == CommitOp.CreateDoc)
			log.setDocId((int) ((logFileLength - logHeaderLength) / COL_LOG_SIZE) + 1);

		ByteBuffer bb = ByteBuffer.wrap(buffer);
		log.serialize(bb);
		logOutput.write(bb.array());

		logFileLength += COL_LOG_SIZE;

		return log.getDocId();
	}

	/**
	 * @return the number of revision log items including duplicated updates
	 */
	public int count() {
		return (int) ((logFileLength - logHeaderLength) / COL_LOG_SIZE);
	}

	public void sync() throws IOException {
		datOutput.flush();
		logOutput.flush();

		datFileOutput.getFD().sync();
		logFileOutput.getFD().sync();
	}

	public void close() {
		closeOutput(datOutput, "doc file");
		closeOutput(logOutput, "log file");
	}

	private void closeOutput(OutputStream os, String target) {
		try {
			os.close();
		} catch (IOException e) {
			logger.error("kraken confdb: cannot close " + target, e);
		}
	}

}
