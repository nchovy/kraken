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
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT thread-safe
 * 
 * @author xeraph
 * 
 */
public class LogFileWriter {
	private final Logger logger = LoggerFactory.getLogger(LogFileWriter.class.getName());

	private static final int INDEX_ITEM_SIZE = 16;
	private static final int DEFAULT_MAX_LOG_BUFFERING = 10000;

	private final int maxLogBuffering;
	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	private LogFileHeaderV1 indexFileHeader;
	private LogFileHeaderV1 dataFileHeader;
	private int count;
	private byte[] keybuf;
	private byte[] intbuf;
	private byte[] datebuf;
	private int lastKey;
	private long lastTime;

	private List<LogRecord> bufferedLogs;

	public LogFileWriter(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this(indexPath, dataPath, DEFAULT_MAX_LOG_BUFFERING);
	}

	public LogFileWriter(File indexPath, File dataPath, int maxLogBuffering) throws IOException,
			InvalidLogFileHeaderException {
		this.bufferedLogs = new ArrayList<LogRecord>(maxLogBuffering * 2);
		this.maxLogBuffering = maxLogBuffering;

		boolean indexExists = indexPath.exists();
		boolean dataExists = dataPath.exists();
		this.indexFile = new RandomAccessFile(indexPath, "rw");
		this.dataFile = new RandomAccessFile(dataPath, "rw");

		this.keybuf = new byte[4];
		this.intbuf = new byte[4];
		this.datebuf = new byte[8];

		if (indexExists) {
			indexFileHeader = LogFileHeaderV1.extractHeader(indexFile);
		} else {
			indexFileHeader = new LogFileHeaderV1(LogFileHeaderV1.MAGIC_STRING_INDEX);
			indexFileHeader.updateHeaderSize();
		}

		if (dataExists) {
			dataFileHeader = LogFileHeaderV1.extractHeader(dataFile);
		} else {
			dataFileHeader = new LogFileHeaderV1(LogFileHeaderV1.MAGIC_STRING_DATA);
			dataFileHeader.updateHeaderSize();
		}

		// read last id
		count = (int) ((indexFile.length() - indexFileHeader.size()) / INDEX_ITEM_SIZE);
		if (count > 0) {
			indexFile.seek(indexFileHeader.size() + (count - 1) * INDEX_ITEM_SIZE);
			lastKey = indexFile.readInt();
			lastTime = indexFile.readLong() >> 16;

		}

		// move to end
		indexFile.seek(indexFile.length());
		dataFile.seek(dataFile.length());
	}

	public int getLastKey() {
		return lastKey;
	}

	public Date getLastDate() {
		return new Date(lastTime);
	}

	public int getCount() {
		return count;
	}

	public void write(LogRecord data) throws IOException {
		// check validity
		int newKey = data.getId();
		if (newKey <= lastKey)
			throw new IllegalArgumentException("invalid key: " + newKey + ", last key was " + lastKey);

		long time = data.getDate().getTime();
		time = time - time % 1000; // discards milliseconds
		if (time < lastTime) {
			if (lastTime - time > 60000) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ssZ");
				String now = dateFormat.format(new Date(time));
				throw new IllegalArgumentException("invalid time: " + now + ", last date was "
						+ dateFormat.format(getLastDate()));
			} else {
				time = lastTime;
			}
		}

		// add to buffer
		bufferedLogs.add(data);

		// update last key
		lastKey = newKey;
		lastTime = time;

		// flush if condition met
		if (bufferedLogs.size() > maxLogBuffering)
			flush();
	}

	public void write(Collection<LogRecord> data) throws IOException {
		for (LogRecord log : data) {
			try {
				write(log);
			} catch (IllegalArgumentException e) {
				logger.error("log storage: write failed", e.getMessage());
			}
		}
	}

	private void prepareInt(int l, byte[] b) {
		for (int i = 0; i < 4; i++)
			b[i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
	}

	private void prepareLong(long l, byte[] b) {
		for (int i = 0; i < 8; i++)
			b[i] = (byte) ((l >> ((7 - i) * 8)) & 0xff);
	}

	private void rawWrite(LogRecord data) throws IOException {
		int dataLength = data.getData().remaining();

		// write key, date, and current position to index file
		prepareInt(data.getId(), keybuf);
		indexFile.write(keybuf);

		long time = data.getDate().getTime();
		time = time - time % 1000; // discards milliseconds
		prepareLong(time, datebuf);
		indexFile.write(datebuf, 2, 6);

		prepareLong((long) dataFile.getFilePointer(), datebuf);
		indexFile.write(datebuf, 2, 6);

		// write key to data file
		dataFile.write(keybuf);

		// write date to data file
		dataFile.write(datebuf);

		// write data length to data file
		prepareInt(dataLength, intbuf);
		dataFile.write(intbuf);

		// write data to data file
		ByteBuffer b = data.getData();
		if (b.remaining() == b.array().length) {
			dataFile.write(b.array());
		} else {
			byte[] array = new byte[b.remaining()];
			b.get(array);
			dataFile.write(array);
		}

		count++;
	}

	public void flush() throws IOException {
		List<LogRecord> b = bufferedLogs;
		bufferedLogs = new ArrayList<LogRecord>(maxLogBuffering * 2);

		Iterator<LogRecord> it = b.iterator();
		int rawWriteCount = 0;
		if (indexFile.length() == 0) {
			indexFile.write(indexFileHeader.serialize());
		}
		if (dataFile.length() == 0) {
			dataFile.write(dataFileHeader.serialize());
		}

		while (it.hasNext()) {
			rawWrite(it.next());
			rawWriteCount++;
		}

		indexFile.getFD().sync();
		dataFile.getFD().sync();
	}

	public void close() throws IOException {
		flush();

		if (indexFile != null) {
			indexFile.close();
			indexFile = null;
		}
		if (dataFile != null) {
			dataFile.close();
			dataFile = null;
		}
	}
}
