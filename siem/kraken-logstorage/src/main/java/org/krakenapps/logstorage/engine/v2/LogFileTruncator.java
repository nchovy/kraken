/*
 * Copyright 2012 Future Systems
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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.krakenapps.logstorage.engine.LogFileHeader;

/**
 * check log .idx and .dat file metadata and block size, and truncate broken
 * data blocks.
 * 
 * @author xeraph
 * 
 */
public class LogFileTruncator {
	public LogFileFixReport fix(File indexPath, File dataPath) throws IOException {
		RandomAccessFile indexFile = null;
		RandomAccessFile dataFile = null;

		try {
			indexFile = new RandomAccessFile(indexPath, "rw");
			dataFile = new RandomAccessFile(dataPath, "rw");

			LogFileHeader indexFileHeader = LogFileHeader.extractHeader(indexFile, indexPath);
			LogFileHeader.extractHeader(dataFile, dataPath);

			indexFile.seek(indexFileHeader.size());

			List<LogIndexBlock> indexBlocks = readIndexBlocks(indexFile);
			List<LogDataBlockHeader> dataBlockHeaders = readDataBlockHeaders(dataFile, dataPath);

			if (indexBlocks.size() == dataBlockHeaders.size())
				return null;

			if (indexBlocks.size() < dataBlockHeaders.size())
				throw new UnsupportedOperationException("index generation recovery is not supported yet");

			// truncate index file
			int validLogCount = 0;

			LogFileFixReport report = new LogFileFixReport();

			// count only matched index blocks
			for (int i = 0; i < dataBlockHeaders.size(); i++) {
				LogIndexBlock b = indexBlocks.get(i);
				validLogCount += b.getCount();
			}

			long logicalEndOfIndex = validLogCount * 4 + dataBlockHeaders.size() * 4;
			long indexOver = indexFile.length() - logicalEndOfIndex;

			if (indexOver > 0) {
				indexFile.setLength(logicalEndOfIndex);
			}

			// truncate data file
			LogDataBlockHeader lastDataBlockHeader = dataBlockHeaders.get(dataBlockHeaders.size() - 1);
			long logicalEndOfData = lastDataBlockHeader.getFilePointer() + 24 + lastDataBlockHeader.getCompressedLength();

			long dataOver = dataFile.length() - logicalEndOfData;
			if (dataOver > 0) {
				dataFile.setLength(logicalEndOfData);
			}

			report.setIndexPath(indexPath);
			report.setDataPath(dataPath);
			report.setTotalLogCount(countLogs(indexBlocks));
			report.setTotalIndexBlocks(indexBlocks.size());
			report.setTotalDataBlocks(dataBlockHeaders.size());
			report.setLostLogCount((int) (report.getTotalLogCount() - validLogCount));
			report.setTruncatedIndexBlocks(indexBlocks.size() - dataBlockHeaders.size());
			report.setTruncatedIndexBytes((int) indexOver);
			report.setTruncatedDataBytes((int) dataOver);

			return report;
		} finally {
			if (indexFile != null)
				indexFile.close();
			if (dataFile != null)
				dataFile.close();
		}
	}

	private long countLogs(List<LogIndexBlock> indexBlocks) {
		long total = 0;
		for (LogIndexBlock b : indexBlocks)
			total += b.getCount();
		return total;
	}

	private List<LogIndexBlock> readIndexBlocks(RandomAccessFile indexFile) throws IOException {
		List<LogIndexBlock> indexBlocks = new ArrayList<LogIndexBlock>();
		int index = 0;
		try {
			for (;;) {
				LogIndexBlock block = readIndexBlock(indexFile);
				block.setIndex(index++);
				indexBlocks.add(block);
			}
		} catch (EOFException e) {
		}

		return indexBlocks;
	}

	private List<LogDataBlockHeader> readDataBlockHeaders(RandomAccessFile dataFile, File dataPath) throws IOException {
		long fileLength = dataFile.length();
		LogFileHeader fileHeader = LogFileHeader.extractHeader(dataFile, dataPath);
		long pos = fileHeader.size();

		List<LogDataBlockHeader> headers = new ArrayList<LogDataBlockHeader>();

		int index = 0;
		for (;;) {
			LogDataBlockHeader header = readDataBlockHeader(dataFile, pos);
			if (header == null)
				break;

			header.setIndex(index++);
			headers.add(header);
			pos += header.getCompressedLength() + 24;
			if (pos > fileLength)
				break;

			dataFile.seek(pos);
		}

		return headers;
	}

	private LogIndexBlock readIndexBlock(RandomAccessFile indexFile) throws IOException {
		int count = indexFile.readInt();
		ByteBuffer bb = ByteBuffer.allocate(LogFileReaderV2.INDEX_ITEM_SIZE * count);
		indexFile.read(bb.array());
		int[] offsets = new int[count];
		for (int i = 0; i < count; i++)
			offsets[i] = bb.getInt();
		LogIndexBlock indexBlock = new LogIndexBlock();
		indexBlock.setCount(count);
		indexBlock.setOffsets(offsets);
		return indexBlock;
	}

	private LogDataBlockHeader readDataBlockHeader(RandomAccessFile dataFile, long pos) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(24);
		dataFile.seek(pos);
		int readBytes = dataFile.read(bb.array());
		if (readBytes < 24)
			return null;

		LogDataBlockHeader h = new LogDataBlockHeader();
		h.setMinDate(new Date(bb.getLong()));
		h.setMaxDate(new Date(bb.getLong()));
		h.setOriginalLength(bb.getInt());
		h.setCompressedLength(bb.getInt());
		h.setFilePointer(pos);
		return h;
	}

	public static void main(String[] args) throws IOException {
		File indexPath = new File("d:\\2012-03-08.idx");
		File dataPath = new File("d:\\2012-03-08.dat");
		System.out.println(new LogFileTruncator().fix(indexPath, dataPath));
	}
}
