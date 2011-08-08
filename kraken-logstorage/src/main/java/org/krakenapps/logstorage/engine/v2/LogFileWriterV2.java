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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.zip.Deflater;

import org.krakenapps.logstorage.engine.InvalidLogFileHeaderException;
import org.krakenapps.logstorage.engine.LogFileHeader;
import org.krakenapps.logstorage.engine.LogFileWriter;
import org.krakenapps.logstorage.engine.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOT thread-safe
 * 
 * @author xeraph
 * 
 */
public class LogFileWriterV2 extends LogFileWriter {
	private final Logger logger = LoggerFactory.getLogger(LogFileWriterV2.class.getName());

	private static final int INDEX_ITEM_SIZE = 16;
	public static final int DEFAULT_BLOCK_SIZE = 640 * 1024; // 640KB
	public static final int DEFAULT_LEVEL = 3;

	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;
	private int count;
	private byte[] intbuf = new byte[4];
	private byte[] longbuf = new byte[8];
	private int lastKey;
	private long lastTime;

	private Long blockStartLogTime;
	private Long blockEndLogTime;
	private long blockLogCount;
	private long dataFileOffset;

	private ByteBuffer indexBuffer;
	private ByteBuffer dataBuffer;
	private ByteBuffer compressed;
	private Deflater compresser;

	public LogFileWriterV2(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this(indexPath, dataPath, DEFAULT_BLOCK_SIZE);
	}

	public LogFileWriterV2(File indexPath, File dataPath, int blockSize) throws IOException,
			InvalidLogFileHeaderException {
		this(indexPath, dataPath, blockSize, DEFAULT_LEVEL);
	}

	public LogFileWriterV2(File indexPath, File dataPath, int blockSize, int level) throws IOException,
			InvalidLogFileHeaderException {
		boolean indexExists = indexPath.exists();
		boolean dataExists = dataPath.exists();
		this.indexFile = new RandomAccessFile(indexPath, "rw");
		this.dataFile = new RandomAccessFile(dataPath, "rw");
		this.indexBuffer = ByteBuffer.allocate(blockSize);
		this.dataBuffer = ByteBuffer.allocate(blockSize);
		this.compressed = ByteBuffer.allocate(blockSize);
		this.compresser = new Deflater(level);

		LogFileHeader indexFileHeader = null;
		if (indexExists && indexFile.length() > 0) {
			indexFileHeader = LogFileHeader.extractHeader(indexFile);
		} else {
			indexFileHeader = new LogFileHeader((short) 2, LogFileHeader.MAGIC_STRING_INDEX);
			prepareInt(blockSize, intbuf);
			indexFileHeader.setExtraData(intbuf);
			indexFile.write(indexFileHeader.serialize());
		}

		LogFileHeader dataFileHeader = null;
		if (dataExists && dataFile.length() > 0) {
			dataFileHeader = LogFileHeader.extractHeader(dataFile);
		} else {
			dataFileHeader = new LogFileHeader((short) 2, LogFileHeader.MAGIC_STRING_DATA);
			prepareInt(blockSize, intbuf);
			dataFileHeader.setExtraData(intbuf);
			dataFile.write(dataFileHeader.serialize());
		}

		// read last key, last time
		long length = indexFile.length() - 4;
		long pos = indexFileHeader.size();
		while (pos < length) {
			indexFile.seek(pos);
			// ignore start date
			read6Byte(indexFile);
			long endTime = read6Byte(indexFile);
			long blockLength = read6Byte(indexFile);
			long compressedLength = read6Byte(indexFile);
			count += blockLength / INDEX_ITEM_SIZE;
			lastTime = (lastTime < endTime) ? endTime : lastTime;
			pos += 24 + compressedLength;
		}

		if (count > 0) {
			indexFile.seek(length);
			lastKey = indexFile.readInt();
			indexFile.seek(length);

			// read data file offset
			length = dataFile.length();
			pos = dataFileHeader.size();
			while (pos < length) {
				dataFile.seek(pos);
				dataFileOffset += dataFile.readInt();
				pos += 8 + dataFile.readInt();
			}
		} else {
			indexFile.seek(length + 4);
		}

		// move to end
		dataFile.seek(dataFile.length());
	}

	private long read6Byte(RandomAccessFile f) throws IOException {
		return ((long) f.readInt() << 16) | (f.readShort() & 0xFFFF);
	}

	@Override
	public int getLastKey() {
		return lastKey;
	}

	@Override
	public Date getLastDate() {
		return new Date(lastTime);
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public void write(LogRecord data) throws IOException {
		logger.trace("kraken logstorage: write new log: id {}, time {}", data.getId(), data.getDate().toString());

		// check validity
		int newKey = data.getId();
		if (newKey <= lastKey)
			throw new IllegalArgumentException("invalid key: " + newKey + ", last key was " + lastKey);

		if ((blockEndLogTime != null && blockEndLogTime > data.getDate().getTime()) || indexBuffer.remaining() < 16)
			flushIndex();

		// add to buffer
		prepareInt(data.getId(), intbuf);
		indexBuffer.put(intbuf);
		prepareLong(data.getDate().getTime(), longbuf);
		indexBuffer.put(longbuf, 2, 6);
		prepareLong(dataFileOffset + dataBuffer.position(), longbuf);
		indexBuffer.put(longbuf, 2, 6);
		if (blockStartLogTime == null)
			blockStartLogTime = data.getDate().getTime();
		blockEndLogTime = data.getDate().getTime();
		blockLogCount++;

		if (dataBuffer.remaining() < 16 + data.getData().remaining())
			flushData();

		prepareInt(data.getId(), intbuf);
		dataBuffer.put(intbuf);
		prepareLong(data.getDate().getTime(), longbuf);
		dataBuffer.put(longbuf);
		prepareInt(data.getData().remaining(), intbuf);
		dataBuffer.put(intbuf);
		ByteBuffer b = data.getData();
		if (b.remaining() == b.array().length) {
			dataBuffer.put(b.array());
		} else {
			byte[] array = new byte[b.remaining()];
			b.get(array);
			dataBuffer.put(array);
		}

		// update last key
		lastKey = newKey;
		long time = data.getDate().getTime();
		lastTime = (lastTime < time) ? time : lastTime;

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
	public void flush() throws IOException {
		flushIndex();
		flushData();
	}

	private void flushIndex() throws IOException {
		if (indexBuffer.position() == 0)
			return;

		indexBuffer.flip();

		// write start time
		prepareLong(blockStartLogTime, longbuf);
		indexFile.write(longbuf, 2, 6);

		// write end time
		prepareLong(blockEndLogTime, longbuf);
		indexFile.write(longbuf, 2, 6);

		// write original size
		prepareInt(indexBuffer.limit(), intbuf);
		indexFile.write(intbuf);

		compresser.setInput(indexBuffer.array(), 0, indexBuffer.limit());
		compresser.finish();
		int compressedSize = compresser.deflate(compressed.array());

		// write compressed size
		prepareInt(compressedSize, intbuf);
		indexFile.write(intbuf);

		// write compressed indexes
		indexFile.write(compressed.array(), 0, compressedSize);

		// caching last key
		prepareInt(lastKey, intbuf);
		indexFile.write(intbuf);

		indexBuffer.clear();
		compresser.reset();
		indexFile.getFD().sync();
		indexFile.seek(indexFile.length() - 4);

		blockStartLogTime = null;
		blockEndLogTime = null;
		blockLogCount = 0;
	}

	private void flushData() throws IOException {
		if (dataBuffer.position() == 0)
			return;

		dataBuffer.flip();
		dataFileOffset += dataBuffer.limit();

		// write original size
		prepareInt(dataBuffer.limit(), intbuf);
		dataFile.write(intbuf);

		compresser.setInput(dataBuffer.array(), 0, dataBuffer.limit());
		compresser.finish();
		int compressedSize = compresser.deflate(compressed.array());

		// write compressed size
		prepareInt(compressedSize, intbuf);
		dataFile.write(intbuf);

		// write compressed logs
		dataFile.write(compressed.array(), 0, compressedSize);

		dataBuffer.clear();
		compresser.reset();
		dataFile.getFD().sync();
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
