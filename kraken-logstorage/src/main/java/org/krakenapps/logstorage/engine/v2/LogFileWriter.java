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
package org.krakenapps.logstorage.engine.v2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.krakenapps.logstorage.engine.InvalidLogFileHeaderException;
import org.krakenapps.logstorage.engine.LogFileHeader;
import org.krakenapps.logstorage.engine.LogRecord;
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

	private static final int BLOCK_SIZE = 640 * 1024; // 640KB
	private static final int INDEX_ITEM_SIZE = 16;
	private static final int DEFAULT_MAX_LOG_BUFFERING = 10000;

	private final int maxLogBuffering;
	private ZipFileWriter indexWriter;
	private ZipFileWriter dataWriter;
	private LogFileHeader indexFileHeader;
	private LogFileHeader dataFileHeader;
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

		this.keybuf = new byte[4];
		this.intbuf = new byte[4];
		this.datebuf = new byte[8];

		// write index header
		if (!indexPath.exists() || indexPath.length() == 0L) {
			this.indexFileHeader = new LogFileHeader((short) 2, LogFileHeader.MAGIC_STRING_INDEX);
			prepareInt(BLOCK_SIZE, intbuf);
			indexFileHeader.setExtraData(intbuf);

			FileOutputStream fos = new FileOutputStream(indexPath);
			fos.write(indexFileHeader.serialize());
			fos.close();
		} else {
			RandomAccessFile index = new RandomAccessFile(indexPath, "r");
			this.indexFileHeader = LogFileHeader.extractHeader(index);
			index.close();
		}

		// write data header
		if (!dataPath.exists() || dataPath.length() == 0L) {
			this.dataFileHeader = new LogFileHeader((short) 2, LogFileHeader.MAGIC_STRING_DATA);
			prepareInt(BLOCK_SIZE, intbuf);
			dataFileHeader.setExtraData(intbuf);

			FileOutputStream fos = new FileOutputStream(dataPath);
			fos.write(dataFileHeader.serialize());
			fos.close();
		} else {
			RandomAccessFile data = new RandomAccessFile(dataPath, "r");
			this.dataFileHeader = LogFileHeader.extractHeader(data);
			data.close();
		}

		this.indexWriter = new ZipFileWriter(indexPath, BLOCK_SIZE);
		this.dataWriter = new ZipFileWriter(dataPath, BLOCK_SIZE);

		ZipFileReader indexReader = new ZipFileReader(indexPath, BLOCK_SIZE);
		ZipFileReader dataReader = new ZipFileReader(dataPath, BLOCK_SIZE);

		// read last id
		count = (int) ((indexReader.length() - indexFileHeader.size()) / INDEX_ITEM_SIZE);
		if (count > 0) {
			indexReader.seek(indexFileHeader.size() + (count - 1) * INDEX_ITEM_SIZE);
			lastKey = indexReader.readInt();
			lastTime = indexReader.readLong() >> 16;

		}

		indexReader.close();
		dataReader.close();
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
		indexWriter.append(keybuf);

		long time = data.getDate().getTime();
		time = time - time % 1000; // discards milliseconds
		prepareLong(time, datebuf);
		indexWriter.append(datebuf, 2, 6);

		prepareLong(dataWriter.getFilePointer(), datebuf);
		indexWriter.append(datebuf, 2, 6);

		// write key to data file
		dataWriter.append(keybuf);

		// write date to data file
		dataWriter.append(datebuf);

		// write data length to data file
		prepareInt(dataLength, intbuf);
		dataWriter.append(intbuf);

		// write data to data file
		ByteBuffer b = data.getData();
		if (b.remaining() == b.array().length) {
			dataWriter.append(b.array());
		} else {
			byte[] array = new byte[b.remaining()];
			b.get(array);
			dataWriter.append(array);
		}

		count++;
	}

	public void flush() throws IOException {
		List<LogRecord> b = bufferedLogs;
		bufferedLogs = new ArrayList<LogRecord>(maxLogBuffering * 2);

		Iterator<LogRecord> it = b.iterator();
		int rawWriteCount = 0;

		while (it.hasNext()) {
			rawWrite(it.next());
			rawWriteCount++;
		}

		indexWriter.flush();
		dataWriter.flush();
	}

	public void close() throws IOException {
		flush();

		if (indexWriter != null) {
			indexWriter.close();
			indexWriter = null;
		}
		if (dataWriter != null) {
			dataWriter.close();
			dataWriter = null;
		}
	}
}
