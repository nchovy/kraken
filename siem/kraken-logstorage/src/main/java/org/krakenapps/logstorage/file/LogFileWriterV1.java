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
package org.krakenapps.logstorage.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
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
public class LogFileWriterV1 extends LogFileWriter {
	private final Logger logger = LoggerFactory.getLogger(LogFileWriterV1.class.getName());

	private static final int INDEX_ITEM_SIZE = 16;
	private static final int DEFAULT_MAX_LOG_BUFFERING = 10000;

	private final int maxLogBuffering;
	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	private long count;
	private byte[] intbuf = new byte[4];
	private byte[] longbuf = new byte[8];
	private long lastKey;
	private long lastTime;

	private Long lastBlockHeaderFp;
	private Long latestLogTime;
	private long blockLogCount;

	private List<LogRecord> bufferedLogs;
	private volatile Date lastFlush = new Date();

	public LogFileWriterV1(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this(indexPath, dataPath, DEFAULT_MAX_LOG_BUFFERING);
	}

	public LogFileWriterV1(File indexPath, File dataPath, int maxLogBuffering) throws IOException,
			InvalidLogFileHeaderException {
		this.bufferedLogs = new ArrayList<LogRecord>(maxLogBuffering * 2);
		this.maxLogBuffering = maxLogBuffering;

		boolean indexExists = indexPath.exists();
		boolean dataExists = dataPath.exists();
		this.indexFile = new RandomAccessFile(indexPath, "rw");
		this.dataFile = new RandomAccessFile(dataPath, "rw");

		LogFileHeader indexFileHeader = null;
		if (indexExists && indexFile.length() > 0) {
			indexFileHeader = LogFileHeader.extractHeader(indexFile, indexPath);
		} else {
			indexFileHeader = new LogFileHeader((short) 1, LogFileHeader.MAGIC_STRING_INDEX);
			indexFile.write(indexFileHeader.serialize());
		}

		LogFileHeader dataFileHeader = null;
		if (dataExists && dataFile.length() > 0) {
			dataFileHeader = LogFileHeader.extractHeader(dataFile, dataPath);
		} else {
			dataFileHeader = new LogFileHeader((short) 1, LogFileHeader.MAGIC_STRING_DATA);
			dataFile.write(dataFileHeader.serialize());
		}

		// read last key, last time
		long length = indexFile.length();
		long pos = indexFileHeader.size();
		while (pos < length) {
			indexFile.seek(pos);
			lastBlockHeaderFp = pos;

			// ignore start date
			read6Byte(indexFile);
			long endTime = read6Byte(indexFile);
			long blockLength = read6Byte(indexFile);

			if (endTime == 0) {
				long remain = length - (pos + 18);
				this.blockLogCount = remain / INDEX_ITEM_SIZE;
				count += this.blockLogCount;
				indexFile.seek(length - 12);
				endTime = read6Byte(indexFile);
				lastTime = (lastTime < endTime) ? endTime : lastTime;
				break;
			} else {
				count += blockLength / INDEX_ITEM_SIZE;
				lastTime = (lastTime < endTime) ? endTime : lastTime;
				pos += blockLength;
			}
		}

		if (count > 0) {
			indexFile.seek(length - INDEX_ITEM_SIZE);
			lastKey = indexFile.readInt();
			latestLogTime = read6Byte(indexFile);
		}

		// move to end
		indexFile.seek(indexFile.length());
		dataFile.seek(dataFile.length());
	}

	private long read6Byte(RandomAccessFile f) throws IOException {
		return ((long) f.readInt() << 16) | (f.readShort() & 0xFFFF);
	}

	@Override
	public long getLastKey() {
		return lastKey;
	}

	@Override
	public Date getLastDate() {
		return new Date(lastTime);
	}

	@Override
	public long getCount() {
		return count;
	}

	@Override
	public void write(LogRecord data) throws IOException {
		// check validity
		long newKey = data.getId();
		if (newKey <= lastKey)
			throw new IllegalArgumentException("invalid key: " + newKey + ", last key was " + lastKey);

		// add to buffer
		bufferedLogs.add(data);

		// update last key
		lastKey = newKey;
		long time = data.getDate().getTime();
		lastTime = (lastTime < time) ? time : lastTime;

		// flush if condition met
		if (bufferedLogs.size() > maxLogBuffering)
			flush();

		count++;
	}

	@Override
	public void write(Collection<LogRecord> data) throws IOException {
		for (LogRecord log : data) {
			try {
				write(log);
			} catch (IllegalArgumentException e) {
				logger.error("log storage: write failed", e.getMessage());
			}
		}
	}

	@Override
	public List<LogRecord> getBuffer() {
		return bufferedLogs;
	}

	@Override
	public void flush() throws IOException {
		lastFlush = new Date();

		List<LogRecord> b = bufferedLogs;
		bufferedLogs = new ArrayList<LogRecord>(maxLogBuffering * 2);

		Iterator<LogRecord> it = b.iterator();

		while (it.hasNext()) {
			rawWrite(it.next());
		}

		indexFile.getFD().sync();
		dataFile.getFD().sync();
	}

	private void rawWrite(LogRecord data) throws IOException {
		if (latestLogTime == null || latestLogTime > data.getDate().getTime()) {
			// renew block header
			if (lastBlockHeaderFp != null) {
				indexFile.seek(lastBlockHeaderFp + 6);
				// update end date
				prepareLong(latestLogTime, longbuf);
				indexFile.write(longbuf, 2, 6);
				// update block length
				prepareLong(blockLogCount * INDEX_ITEM_SIZE, longbuf);
				indexFile.write(longbuf, 2, 6);
			}

			// write new block header
			blockLogCount = 0L;
			lastBlockHeaderFp = indexFile.length();
			indexFile.seek(lastBlockHeaderFp);
			// write start date
			prepareLong(data.getDate().getTime(), longbuf);
			indexFile.write(longbuf, 2, 6);
			// initialize end date, block length
			prepareLong(0L, longbuf);
			indexFile.write(longbuf, 2, 6);
			indexFile.write(longbuf, 2, 6);
		}
		latestLogTime = data.getDate().getTime();

		long dataFileFp = dataFile.getFilePointer();

		// write key
		prepareInt((int) data.getId(), intbuf);
		indexFile.write(intbuf);
		dataFile.write(intbuf);

		// write date
		long time = data.getDate().getTime();
		prepareLong(time, longbuf);
		indexFile.write(longbuf, 2, 6);
		dataFile.write(longbuf);

		// write fp
		prepareLong(dataFileFp, longbuf);
		indexFile.write(longbuf, 2, 6);

		// write data length to data file
		prepareInt(data.getData().remaining(), intbuf);
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

		blockLogCount++;
	}

	private void prepareInt(int l, byte[] b) {
		for (int i = 0; i < 4; i++)
			b[i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
	}

	private void prepareLong(long l, byte[] b) {
		for (int i = 0; i < 8; i++)
			b[i] = (byte) ((l >> ((7 - i) * 8)) & 0xff);
	}

	@Override
	public Date getLastFlush() {
		return lastFlush;
	}

	@Override
	public void close() throws IOException {
		flush();

		// renew block header
		if (lastBlockHeaderFp != null) {
			indexFile.seek(lastBlockHeaderFp + 6);
			// update end date
			prepareLong(latestLogTime, longbuf);
			indexFile.write(longbuf, 2, 6);
			// update log count
			prepareLong(blockLogCount * INDEX_ITEM_SIZE, longbuf);
			indexFile.write(longbuf, 2, 6);
		}

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
