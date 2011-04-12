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
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Date;

public class LogFileReader {
	private static final int INDEX_ITEM_SIZE = 16;

	private BufferedRandomAccessFileReader indexFile;
	private BufferedRandomAccessFileReader dataFile;
	
	private LogFileHeaderV1 indexFileHeader;
	@SuppressWarnings("unused")
	private LogFileHeaderV1 dataFileHeader;

	public LogFileReader(File indexPath, File dataPath) throws IOException, InvalidLogFileHeaderException {
		this.indexFile = new BufferedRandomAccessFileReader(indexPath);
		indexFileHeader = LogFileHeaderV1.extractHeader(indexFile);
		this.dataFile = new BufferedRandomAccessFileReader(dataPath);
		dataFileHeader = LogFileHeaderV1.extractHeader(dataFile);
	}

	public LogRecord find(int id) throws IOException {
		long pos = findPosition(id);
		if (pos < 0)
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

	private int findIndex(int id) throws IOException {
		int low = 0;
		int high = (int) ((indexFile.length() - indexFileHeader.size()) / INDEX_ITEM_SIZE);

		while (low <= high) {
			int mid = low + (high - low) / 2;
			long pos = mid * INDEX_ITEM_SIZE;

			int key = -1;
			try {
				indexFile.seek(indexFileHeader.size() + pos);
				key = indexFile.readInt();
			} catch (IllegalArgumentException e) {
				return -1;
			} catch (BufferUnderflowException e) {
				return -1;
			}

			if (id > key)
				low = mid + 1;
			else if (id < key)
				high = mid - 1;
			else
				return mid;
		}

		return -1;
	}

	private long findPosition(int id) throws IOException {
		int indexPos = findIndex(id);
		if (indexPos < 0)
			return -1;

		indexFile.seek(indexFileHeader.size() + indexPos * INDEX_ITEM_SIZE + 8);
		//                              0011223344556677
		return indexFile.readLong() & 0x0000FFFFFFFFFFFFL;
	}

	public void traverse(Date from, Date to, Integer upperBound, Integer lowerBound, int limit,
			LogRecordCallback callback)
			throws IOException, InterruptedException {
		int matched = 0;

		if (upperBound != null && lowerBound != null && upperBound < lowerBound)
			throw new IllegalArgumentException("upper bound should bigger than lower bound");

		long length = indexFile.length();

		int i = 0;
		if (upperBound != null) {
			i = findIndex(upperBound);
			if (i < 0)
				return;
		} else
			i = (int) ((length - indexFileHeader.size()) / INDEX_ITEM_SIZE);

		while (i > 0) {
			if (matched >= limit)
				break;

			// begin of last item
			indexFile.seek(indexFileHeader.size() + (i - 1) * INDEX_ITEM_SIZE);

			// read key
			int key = indexFile.readInt();

			if (upperBound != null && key > upperBound) {
				i--;
				continue;
			}

			if (lowerBound != null && key < lowerBound)
				break;

			// read date
			long tmp = indexFile.readLong();
			Date date = new Date(tmp >>> 16);

			if (from != null && date.before(from)) {
				// it should stop here because all next items will also before
				// 'from' date. it is descending scan.
				break;
			}

			if (to != null && date.after(to)) {
				i--;
				continue;
			}

			// read data length  0011223344556677
			long pos = ((tmp & 0x000000000000FFFFL) << 32) | (long) indexFile.readInt();

			dataFile.seek(pos + 12); // move after (key + date)
			int dataLen = dataFile.readInt();

			// read block
			byte[] block = new byte[dataLen];
			dataFile.readFully(block);

			ByteBuffer bb = ByteBuffer.wrap(block, 0, dataLen);
			if (callback.onLog(new LogRecord(date, key, bb)))
				matched++;

			i--;
		}
	}

	public void traverse(Integer upperBound, Integer lowerBound, int limit, LogRecordCallback callback) {
		traverse(upperBound, lowerBound, limit, callback);
	}

	public void traverse(Date from, Date to, int limit, LogRecordCallback callback) throws IOException,
			InterruptedException {
		traverse(from, to, null, null, limit, callback);
	}

	public void traverse(int limit, LogRecordCallback callback) throws IOException, InterruptedException {
		traverse(null, null, null, null, limit, callback);
	}

	public void close() throws IOException {
		indexFile.close();
		dataFile.close();
	}
}
