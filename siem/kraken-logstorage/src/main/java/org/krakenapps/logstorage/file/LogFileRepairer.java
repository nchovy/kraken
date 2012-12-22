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
package org.krakenapps.logstorage.file;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * check log .idx and .dat file metadata and block size, and truncate broken
 * data blocks or generate index blocks.
 * 
 * @author xeraph
 * 
 */
public class LogFileRepairer {
	private final Logger logger = LoggerFactory.getLogger(LogFileRepairer.class.getName());

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

			// check broken data file
			truncateBrokenDataBlock(dataPath, dataFile, dataBlockHeaders);

			if (indexBlocks.size() == dataBlockHeaders.size()) {
				return null;
			}

			if (indexBlocks.size() < dataBlockHeaders.size())
				return generate(indexPath, dataPath, indexFile, dataFile, indexBlocks, dataBlockHeaders);
			else
				return truncate(indexPath, dataPath, indexFile, dataFile, indexBlocks, dataBlockHeaders);
		} finally {
			if (indexFile != null)
				indexFile.close();
			if (dataFile != null)
				dataFile.close();
		}
	}

	private void truncateBrokenDataBlock(File dataPath, RandomAccessFile dataFile, List<LogDataBlockHeader> dataBlockHeaders)
			throws IOException {
		if (dataBlockHeaders.size() == 0)
			return;

		LogDataBlockHeader lastDataBlockHeader = dataBlockHeaders.get(dataBlockHeaders.size() - 1);
		long logicalEndOfData = lastDataBlockHeader.getFilePointer() + 24 + lastDataBlockHeader.getCompressedLength();

		long dataOver = dataFile.length() - logicalEndOfData;
		if (dataOver > 0) {
			dataFile.setLength(logicalEndOfData);
			logger.info("kraken logstorage: truncated immature last data block [{}], removed [{}] bytes", dataPath, dataOver);
		}
	}

	private LogFileFixReport generate(File indexPath, File dataPath, RandomAccessFile indexFile, RandomAccessFile dataFile,
			List<LogIndexBlock> indexBlocks, List<LogDataBlockHeader> dataBlockHeaders) throws IOException {
		logger.trace("kraken logstorage: checking incomplete index block, file [{}]", indexPath);

		// truncate data file
		LogDataBlockHeader lastDataBlockHeader = dataBlockHeaders.get(dataBlockHeaders.size() - 1);
		long logicalEndOfData = lastDataBlockHeader.getFilePointer() + 24 + lastDataBlockHeader.getCompressedLength();

		long dataOver = dataFile.length() - logicalEndOfData;
		if (dataOver > 0) {
			dataFile.setLength(logicalEndOfData);
			dataBlockHeaders.remove(dataBlockHeaders.size() - 1);
		}

		// check immature last index block writing
		LogIndexBlock lastIndexBlock = indexBlocks.get(indexBlocks.size() - 1);
		long lastIndexBlockSize = indexFile.length() - lastIndexBlock.getFilePointer();
		long expectedIndexBlockSize = 4 + lastIndexBlock.getCount() * LogFileReaderV2.INDEX_ITEM_SIZE;

		// truncate immature last index block
		if (lastIndexBlockSize != expectedIndexBlockSize) {
			logger.trace("kraken logstorage: expected last index block size [{}], actual last index block size [{}]",
					expectedIndexBlockSize, lastIndexBlockSize);
			indexFile.setLength(lastIndexBlock.getFilePointer());
			indexBlocks.remove(indexBlocks.size() - 1);

			logger.info("kraken logstorage: truncated immature last index block [{}], removed [{}] bytes", indexPath,
					lastIndexBlockSize);
		}

		Inflater decompresser = new Inflater();
		int addedLogs = 0;
		try {
			// generate index block (support only v2 block recovery)
			int offset = indexBlocks.size();
			int missingBlockCount = dataBlockHeaders.size() - indexBlocks.size();
			byte[] intbuf = new byte[4];
			logger.info("kraken logstorage: index block [{}], data block [{}], missing count [{}]",
					new Object[] { indexBlocks.size(), dataBlockHeaders.size(), missingBlockCount });

			for (int i = 0; i < missingBlockCount; i++) {
				LogDataBlockHeader blockHeader = dataBlockHeaders.get(offset + i);
				ByteBuffer bb = readDataBlockV2(decompresser, dataFile, blockHeader);
				List<Integer> logOffsets = readLogOffsets(bb);

				// write index block
				prepareInt(logOffsets.size(), intbuf);
				indexFile.write(intbuf);
				for (int logOffset : logOffsets) {
					prepareInt(logOffset, intbuf);
					indexFile.write(intbuf);
				}

				addedLogs += logOffsets.size();
				logger.info("kraken logstorage: rewrite index block for {}, log count [{}], index file [{}]", new Object[] {
						blockHeader, logOffsets.size(), indexPath });
			}

			LogFileFixReport report = new LogFileFixReport();
			report.setIndexPath(indexPath);
			report.setDataPath(dataPath);
			report.setTotalLogCount(countLogs(indexBlocks) + addedLogs);
			report.setTotalIndexBlocks(indexBlocks.size() + missingBlockCount);
			report.setTotalDataBlocks(dataBlockHeaders.size());
			report.setAddedIndexBlocks(missingBlockCount);
			return report;
		} finally {
			decompresser.end();
		}
	}

	private List<Integer> readLogOffsets(ByteBuffer dataBlock) {
		int offset = 0;
		int length = dataBlock.remaining();
		List<Integer> offsets = new ArrayList<Integer>(length / 400);

		while (offset < length) {
			offsets.add(offset);
			offset += 16;
			dataBlock.position(offset);
			int size = dataBlock.getInt();
			offset += 4 + size;
		}

		return offsets;
	}

	private void prepareInt(int l, byte[] b) {
		for (int i = 0; i < 4; i++)
			b[i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
	}

	private ByteBuffer readDataBlockV2(Inflater decompresser, RandomAccessFile dataFile, LogDataBlockHeader blockHeader)
			throws IOException {
		ByteBuffer output = ByteBuffer.allocate(blockHeader.getOriginalLength());
		ByteBuffer input = ByteBuffer.allocate(blockHeader.getCompressedLength());

		dataFile.seek(blockHeader.getFilePointer() + 24L);
		dataFile.readFully(input.array(), 0, blockHeader.getCompressedLength());
		decompresser.setInput(input.array(), 0, blockHeader.getCompressedLength());
		try {
			output.limit(blockHeader.getOriginalLength());
			decompresser.inflate(output.array());
			decompresser.reset();
		} catch (DataFormatException e) {
			throw new IOException(e);
		}

		return output;
	}

	private LogFileFixReport truncate(File indexPath, File dataPath, RandomAccessFile indexFile, RandomAccessFile dataFile,
			List<LogIndexBlock> indexBlocks, List<LogDataBlockHeader> dataBlockHeaders) throws IOException {
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
			pos += header.getCompressedLength() + 24;
			if (pos > fileLength || pos < 0)
				break;

			headers.add(header);
			dataFile.seek(pos);
		}

		return headers;
	}

	private LogIndexBlock readIndexBlock(RandomAccessFile indexFile) throws IOException {
		long fp = indexFile.getFilePointer();
		int count = indexFile.readInt();
		ByteBuffer bb = ByteBuffer.allocate(LogFileReaderV2.INDEX_ITEM_SIZE * count);
		indexFile.read(bb.array());
		int[] offsets = new int[count];
		for (int i = 0; i < count; i++)
			offsets[i] = bb.getInt();
		LogIndexBlock indexBlock = new LogIndexBlock();
		indexBlock.setFilePointer(fp);
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
}
