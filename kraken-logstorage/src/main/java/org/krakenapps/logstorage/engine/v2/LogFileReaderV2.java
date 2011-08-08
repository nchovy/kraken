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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.krakenapps.logstorage.engine.InvalidLogFileHeaderException;
import org.krakenapps.logstorage.engine.LogFileHeader;
import org.krakenapps.logstorage.engine.LogFileReader;
import org.krakenapps.logstorage.engine.LogRecord;
import org.krakenapps.logstorage.engine.LogRecordCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileReaderV2 extends LogFileReader {
	private Logger logger = LoggerFactory.getLogger(LogFileReaderV2.class);
	private static final int INDEX_ITEM_SIZE = 16;

	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;

	private List<IndexBlockHeader> indexBlockHeaders = new ArrayList<IndexBlockHeader>();
	private List<DataBlockHeader> dataBlockHeaders = new ArrayList<DataBlockHeader>();

	private byte[] buf;
	private IndexBlockHeader nowIndexBlock;
	private ByteBuffer indexBuffer;
	private DataBlockHeader nowDataBlock;
	private ByteBuffer dataBuffer;

	private Inflater decompresser = new Inflater();

	public LogFileReaderV2(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this.indexFile = new RandomAccessFile(indexPath, "r");
		LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexFile);
		if (indexFileHeader.version() != 2)
			throw new InvalidLogFileHeaderException("version not match");
		int indexBlockSize = getInt(indexFileHeader.getExtraData());
		indexBuffer = ByteBuffer.allocate(indexBlockSize);

		long length = indexFile.length() - 4;
		long pos = indexFileHeader.size();
		while (pos < length) {
			indexFile.seek(pos);
			IndexBlockHeader header = new IndexBlockHeader(indexFile);
			header.fp = pos;
			indexBlockHeaders.add(header);
			pos += 20 + header.compressedLength;
		}
		logger.trace("kraken logstorage: {} has {} blocks.", indexPath.getName(), indexBlockHeaders.size());

		this.dataFile = new RandomAccessFile(dataPath, "r");
		LogFileHeader dataFileHeader = LogFileHeader.extractHeader(dataFile);
		if (dataFileHeader.version() != 2)
			throw new InvalidLogFileHeaderException("version not match");
		int dataBlockSize = getInt(dataFileHeader.getExtraData());
		dataBuffer = ByteBuffer.allocate(dataBlockSize);

		length = dataFile.length();
		pos = dataFileHeader.size();
		long offset = 0;
		while (pos < length) {
			dataFile.seek(pos);
			DataBlockHeader header = new DataBlockHeader(dataFile);
			header.fp = pos;
			header.offset = offset;
			offset += header.origLength;
			dataBlockHeaders.add(header);
			pos += 8 + header.compressedLength;
		}

		buf = new byte[(indexBlockSize > dataBlockSize) ? indexBlockSize : dataBlockSize];
	}

	private int getInt(byte[] extraData) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			value <<= 8;
			value |= extraData[i] & 0xFF;
		}
		return value;
	}

	@Override
	public LogRecord find(int id) throws IOException {
		Long pos = null;
		for (IndexBlockHeader header : indexBlockHeaders) {
			if (id < header.firstId + header.origLength / INDEX_ITEM_SIZE) {
				pos = getLogIndex(header, id - header.firstId).fp;
				break;
			}
		}
		if (pos == null)
			return null;

		return getLogRecord(pos);
	}

	@Override
	public void traverse(int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(null, null, limit, callback);
	}

	@Override
	public void traverse(Date from, Date to, int limit, LogRecordCallback callback) throws IOException,
			InterruptedException {
		int matched = 0;

		int block = indexBlockHeaders.size() - 1;
		IndexBlockHeader header = indexBlockHeaders.get(block);
		long blockLogNum = header.origLength / INDEX_ITEM_SIZE;

		if (header.endTime == 0)
			blockLogNum = (indexFile.length() - (header.fp + 20)) / INDEX_ITEM_SIZE;

		// block validate
		while ((from != null && header.endTime != 0L && header.endTime < from.getTime())
				|| (to != null && header.startTime > to.getTime())) {
			if (--block < 0)
				return;
			header = indexBlockHeaders.get(block);
			blockLogNum = header.origLength / INDEX_ITEM_SIZE;
		}

		while (true) {
			if (--blockLogNum < 0) {
				do {
					if (--block < 0)
						return;
					header = indexBlockHeaders.get(block);
					blockLogNum = header.origLength / INDEX_ITEM_SIZE - 1;
				} while ((from != null && header.endTime < from.getTime())
						|| (to != null && header.startTime > to.getTime()));
			}

			LogIndex index = getLogIndex(header, (int) blockLogNum);

			if (from != null && index.date.before(from))
				continue;
			if (to != null && index.date.after(to))
				continue;

			// read data file fp
			long pos = index.fp;

			if (callback.onLog(getLogRecord(pos))) {
				if (++matched == limit)
					break;
			}
		}
	}

	private class LogIndex {
		@SuppressWarnings("unused")
		private int id;
		private Date date;
		private long fp;

		private LogIndex(int id, Date date, long fp) {
			this.id = id;
			this.date = date;
			this.fp = fp;
		}
	}

	private LogIndex getLogIndex(IndexBlockHeader header, int offset) throws IOException {
		if (!header.equals(nowIndexBlock)) {
			nowIndexBlock = header;

			indexBuffer.clear();
			indexFile.seek(header.fp + 20);
			indexFile.read(buf, 0, header.compressedLength);
			decompresser.setInput(buf, 0, header.compressedLength);
			try {
				indexBuffer.limit(header.origLength);
				decompresser.inflate(indexBuffer.array());
				decompresser.reset();
			} catch (DataFormatException e) {
				throw new IOException(e);
			}
		}

		indexBuffer.position(offset * INDEX_ITEM_SIZE);
		int id = indexBuffer.getInt();
		Date date = new Date(read6Bytes(indexBuffer));
		long fp = read6Bytes(indexBuffer);

		return new LogIndex(id, date, fp);
	}

	private LogRecord getLogRecord(long pos) throws IOException {
		DataBlockHeader header = null;
		for (DataBlockHeader h : dataBlockHeaders) {
			if (pos < h.offset + h.origLength) {
				header = h;
				break;
			}
		}
		if (header == null)
			throw new IOException("invalid position");

		if (!header.equals(nowDataBlock)) {
			nowDataBlock = header;

			dataBuffer.clear();
			dataFile.seek(header.fp + 8);
			dataFile.read(buf, 0, header.compressedLength);
			decompresser.setInput(buf, 0, header.compressedLength);
			try {
				dataBuffer.limit(header.origLength);
				decompresser.inflate(dataBuffer.array());
				decompresser.reset();
			} catch (DataFormatException e) {
				throw new IOException(e);
			}
		}

		dataBuffer.position((int) (pos - header.offset));
		int id = dataBuffer.getInt();
		Date date = new Date(dataBuffer.getLong());
		byte[] data = new byte[dataBuffer.getInt()];
		dataBuffer.get(data);

		return new LogRecord(date, id, ByteBuffer.wrap(data));
	}

	private long read6Bytes(ByteBuffer b) throws IOException {
		return ((long) b.getInt() << 16) | (b.getShort() & 0xFFFF);
	}

	@Override
	public void close() throws IOException {
		decompresser.end();
		indexFile.close();
		dataFile.close();
	}

	private static class IndexBlockHeader {
		private static Integer NEXT_ID = 1;
		private long fp;
		private long startTime;
		private long endTime;
		private int origLength;
		private int compressedLength;
		private int firstId;

		private IndexBlockHeader(RandomAccessFile f) throws IOException {
			this.startTime = read6Bytes(f);
			this.endTime = read6Bytes(f);
			this.origLength = f.readInt();
			this.compressedLength = f.readInt();
			this.firstId = NEXT_ID;
			NEXT_ID += (int) this.origLength / INDEX_ITEM_SIZE;
		}

		private long read6Bytes(RandomAccessFile f) throws IOException {
			return ((long) f.readInt() << 16) | (f.readShort() & 0xFFFF);
		}
	}

	private class DataBlockHeader {
		private long fp;
		private long offset;
		private int origLength;
		private int compressedLength;

		private DataBlockHeader(RandomAccessFile f) throws IOException {
			this.origLength = f.readInt();
			this.compressedLength = f.readInt();
		}
	}
}
