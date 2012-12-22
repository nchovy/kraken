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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFileReaderV1 extends LogFileReader {
	private Logger logger = LoggerFactory.getLogger(LogFileReaderV1.class);
	private static final int INDEX_ITEM_SIZE = 16;

	private BufferedRandomAccessFileReader indexFile;
	private BufferedRandomAccessFileReader dataFile;

	private List<BlockHeader> blockHeaders = new ArrayList<BlockHeader>();

	public LogFileReaderV1(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this.indexFile = new BufferedRandomAccessFileReader(indexPath);
		LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexFile, indexPath);
		if (indexFileHeader.version() != 1)
			throw new InvalidLogFileHeaderException("version not match");

		RandomAccessFile f = new RandomAccessFile(indexPath, "r");
		long length = f.length();
		long pos = indexFileHeader.size();
		while (pos < length) {
			f.seek(pos);
			BlockHeader header = new BlockHeader(f);
			header.fp = pos;
			blockHeaders.add(header);
			if (header.blockLength == 0)
				break;
			pos += 18 + header.blockLength;
		}
		f.close();
		logger.trace("kraken logstorage: {} has {} blocks.", indexPath.getName(), blockHeaders.size());

		this.dataFile = new BufferedRandomAccessFileReader(dataPath);
		LogFileHeader dataFileHeader = LogFileHeader.extractHeader(dataFile, dataPath);
		if (dataFileHeader.version() != 1)
			throw new InvalidLogFileHeaderException("version not match");
	}

	@Override
	public LogRecord find(long id) throws IOException {
		Long pos = null;
		for (BlockHeader header : blockHeaders) {
			if (id < header.firstId + header.blockLength / INDEX_ITEM_SIZE) {
				// fp + header size + offset + (id + date) index
				long indexPos = header.fp + 18 + (id - header.firstId) * INDEX_ITEM_SIZE + 10;
				indexFile.seek(indexPos);
				pos = read6Bytes(indexFile);
				break;
			}
		}
		if (pos == null)
			return null;

		// read data length
		dataFile.seek(pos);
		int key = dataFile.readInt();
		Date date = new Date(dataFile.readLong());
		int dataLen = dataFile.readInt();

		// read block
		byte[] block = new byte[dataLen];
		dataFile.readFully(block);

		ByteBuffer bb = ByteBuffer.wrap(block);
		return new LogRecord(date, key, bb);
	}

	@Override
	public void traverse(long limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(0, limit, callback);
	}

	@Override
	public void traverse(long offset, long limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(null, null, offset, limit, callback);
	}

	@Override
	public void traverse(Date from, Date to, long limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(from, to, 0, limit, callback);
	}

	@Override
	public void traverse(Date from, Date to, long offset, long limit, LogRecordCallback callback) throws IOException, InterruptedException {
		int matched = 0;

		int block = blockHeaders.size() - 1;
		BlockHeader header = blockHeaders.get(block);
		long blockLogNum = header.blockLength / INDEX_ITEM_SIZE;

		if (header.endTime == 0)
			blockLogNum = (indexFile.length() - (header.fp + 18)) / INDEX_ITEM_SIZE;

		// block validate
		while ((from != null && header.endTime != 0L && header.endTime < from.getTime()) || (to != null && header.startTime > to.getTime())) {
			if (--block < 0)
				return;
			header = blockHeaders.get(block);
			blockLogNum = header.blockLength / INDEX_ITEM_SIZE;
		}

		while (true) {
			if (--blockLogNum < 0) {
				do {
					if (--block < 0)
						return;
					header = blockHeaders.get(block);
					blockLogNum = header.blockLength / INDEX_ITEM_SIZE - 1;
				} while ((from != null && header.endTime < from.getTime()) || (to != null && header.startTime > to.getTime()));
			}

			// begin of item (ignore id)
			indexFile.seek(header.fp + 18 + INDEX_ITEM_SIZE * blockLogNum + 4);

			// read index
			Date indexDate = new Date(read6Bytes(indexFile));

			if (from != null && indexDate.before(from))
				continue;
			if (to != null && indexDate.after(to))
				continue;

			// read data file fp
			long pos = read6Bytes(indexFile);

			// read data
			dataFile.seek(pos);
			int dataId = dataFile.readInt();
			Date dataDate = new Date(dataFile.readLong());
			int dataLen = dataFile.readInt();
			byte[] data = new byte[dataLen];
			dataFile.readFully(data);

			if (offset > matched) {
				matched++;
				continue;
			}

			ByteBuffer bb = ByteBuffer.wrap(data, 0, dataLen);
			if (callback.onLog(new LogRecord(dataDate, dataId, bb))) {
				if (++matched == offset + limit)
					return;
			}
		}
	}

	private long read6Bytes(BufferedRandomAccessFileReader f) throws IOException {
		return ((long) f.readInt() << 16) | (f.readShort() & 0xFFFF);
	}

	@Override
	public void close() throws IOException {
		indexFile.close();
		dataFile.close();
	}

	private static class BlockHeader {
		private static Integer NEXT_ID = 1;
		private long fp;
		private long startTime;
		private long endTime;
		private long blockLength;
		private int firstId;

		private BlockHeader(RandomAccessFile f) throws IOException {
			this.startTime = read6Bytes(f);
			this.endTime = read6Bytes(f);
			this.blockLength = read6Bytes(f);
			this.firstId = NEXT_ID;
			NEXT_ID += (int) this.blockLength / INDEX_ITEM_SIZE;
		}

		private long read6Bytes(RandomAccessFile f) throws IOException {
			return ((long) f.readInt() << 16) | (f.readShort() & 0xFFFF);
		}
	}
}
