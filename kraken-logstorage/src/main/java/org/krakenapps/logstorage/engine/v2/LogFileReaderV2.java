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
	private static final int INDEX_ITEM_SIZE = 4;

	private RandomAccessFile indexFile;
	private RandomAccessFile dataFile;

	private List<IndexBlockHeader> indexBlockHeaders = new ArrayList<IndexBlockHeader>();
	private List<DataBlockHeader> dataBlockHeaders = new ArrayList<DataBlockHeader>();

	private byte[] buf;
	private DataBlockHeader nowDataBlock;
	private ByteBuffer dataBuffer;

	private Inflater decompresser = new Inflater();

	public LogFileReaderV2(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this.indexFile = new RandomAccessFile(indexPath, "r");
		LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexFile);
		if (indexFileHeader.version() != 2)
			throw new InvalidLogFileHeaderException("version not match");

		long length = indexFile.length() - 4;
		long pos = indexFileHeader.size();
		long logCount = 0;
		while (pos < length) {
			indexFile.seek(pos);
			IndexBlockHeader header = new IndexBlockHeader(indexFile);
			header.fp = pos;
			logCount += header.logCount;
			indexBlockHeaders.add(header);
			pos += 4 + header.logCount * INDEX_ITEM_SIZE;
		}
		logger.trace("kraken logstorage: {} has {} blocks, {} logs.", new Object[] { indexPath.getName(), indexBlockHeaders.size(),
				logCount });

		this.dataFile = new RandomAccessFile(dataPath, "r");
		LogFileHeader dataFileHeader = LogFileHeader.extractHeader(dataFile);
		if (dataFileHeader.version() != 2)
			throw new InvalidLogFileHeaderException("version not match");
		int dataBlockSize = getInt(dataFileHeader.getExtraData());
		dataBuffer = ByteBuffer.allocate(dataBlockSize);
		buf = new byte[dataBlockSize];

		length = dataFile.length();
		pos = dataFileHeader.size();
		while (pos < length) {
			dataFile.seek(pos);
			DataBlockHeader header = new DataBlockHeader(dataFile);
			header.fp = pos;
			dataBlockHeaders.add(header);
			pos += 24 + header.compressedLength;
		}

		if (indexBlockHeaders.size() != dataBlockHeaders.size())
			throw new IOException("invalid log file");
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
		int l = 0;
		int r = indexBlockHeaders.size();
		while (l < r) {
			int m = (l + r) / 2;
			IndexBlockHeader header = indexBlockHeaders.get(m);

			if (id < header.firstId)
				r = m;
			else if (header.firstId + header.logCount <= id)
				l = m;

			indexFile.seek(header.fp + (id - header.firstId) * INDEX_ITEM_SIZE);
			int offset = indexFile.readInt();

			return getLogRecord(dataBlockHeaders.get(m), offset);
		}

		return null;
	}

	@Override
	public void traverse(int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(0, limit, callback);
	}

	@Override
	public void traverse(int offset, int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(null, null, offset, limit, callback);
	}

	@Override
	public void traverse(Date from, Date to, int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(from, to, 0, limit, callback);
	}

	@Override
	public void traverse(Date from, Date to, int offset, int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		for (int i = indexBlockHeaders.size() - 1; i >= 0; i--) {
			IndexBlockHeader index = indexBlockHeaders.get(i);
			if (index.logCount <= offset) {
				offset -= index.logCount;
				continue;
			}

			DataBlockHeader data = dataBlockHeaders.get(i);
			Long fromTime = (from == null) ? null : from.getTime();
			Long toTime = (to == null) ? null : to.getTime();
			if ((fromTime == null || data.endDate >= fromTime) && (toTime == null || data.startDate <= toTime)) {
				int matched = readBlock(index, data, fromTime, toTime, offset, limit, callback);
				if (matched < offset)
					offset -= matched;
				else {
					matched -= offset;
					offset = 0;
					limit -= matched;
				}

				if (limit == 0)
					return;
			}
		}
	}

	private int readBlock(IndexBlockHeader index, DataBlockHeader data, Long from, Long to, int offset, int limit,
			LogRecordCallback callback) throws IOException, InterruptedException {
		List<Integer> offsets = new ArrayList<Integer>();
		int matched = 0;

		indexFile.seek(index.fp + 4);
		ByteBuffer indexBuffer = ByteBuffer.allocate(index.logCount * 4);
		indexFile.read(indexBuffer.array());
		for (int i = 0; i < index.logCount; i++)
			offsets.add(indexBuffer.getInt());

		// reverse order
		for (int i = offsets.size() - 1; i >= 0; i--) {
			long date = getLogRecordDate(data, offsets.get(i));
			if (from != null && date < from)
				return matched;
			if (to != null && date > to)
				continue;

			if (offset > 0) {
				offset--;
				matched++;
				continue;
			}

			if (callback.onLog(getLogRecord(data, offsets.get(i)))) {
				if (++matched == limit)
					return matched;
			}
		}

		return matched;
	}

	private long getLogRecordDate(DataBlockHeader data, int offset) throws IOException {
		prepareDataBlock(data);

		dataBuffer.position(offset + 8);
		return dataBuffer.getLong();
	}

	private LogRecord getLogRecord(DataBlockHeader data, int offset) throws IOException {
		prepareDataBlock(data);

		dataBuffer.position(offset);
		long id = dataBuffer.getLong();
		Date date = new Date(dataBuffer.getLong());
		byte[] b = new byte[dataBuffer.getInt()];
		dataBuffer.get(b);

		return new LogRecord(date, id, ByteBuffer.wrap(b));
	}

	private void prepareDataBlock(DataBlockHeader header) throws IOException {
		if (!header.equals(nowDataBlock)) {
			nowDataBlock = header;

			dataBuffer.clear();
			dataFile.seek(header.fp + 24L);
			dataFile.readFully(buf, 0, header.compressedLength);
			decompresser.setInput(buf, 0, header.compressedLength);
			try {
				dataBuffer.limit(header.origLength);
				decompresser.inflate(dataBuffer.array());
				decompresser.reset();
			} catch (DataFormatException e) {
				throw new IOException(e);
			}
		}
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
		private int firstId;
		private int logCount;

		private IndexBlockHeader(RandomAccessFile f) throws IOException {
			this.logCount = f.readInt();
			this.firstId = NEXT_ID;
			NEXT_ID += logCount;
		}
	}

	private static class DataBlockHeader {
		private static ByteBuffer buf = ByteBuffer.allocate(24);
		private long fp;
		private long startDate;
		private long endDate;
		private int origLength;
		private int compressedLength;

		private DataBlockHeader(RandomAccessFile f) throws IOException {
			f.readFully(buf.array());
			buf.position(0);
			this.startDate = buf.getLong();
			this.endDate = buf.getLong();
			this.origLength = buf.getInt();
			this.compressedLength = buf.getInt();
		}
	}
}
