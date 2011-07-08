/*
 * Copyright 2011 Future Systems
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
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.krakenapps.logstorage.engine.LogFileHeader;

public class ZipFileReader {
	private static final int BLOCK_HEADER_SIZE = 4;

	private RandomAccessFile f;

	private long length;

	// retain seek position
	private long position;

	// temporary buffer for compressed binary
	private byte[] buffer;

	private List<Long> blockOffsets;
	private List<Long> lengthOffsets;

	// uncompressed block data
	private byte[] block;

	// wrapped block
	private ByteBuffer blockBuffer;

	// current block number (buffered)
	private long currentBlockNum = -1;

	// last block will be less than block size
	private int currentBlockSize = 0;

	private byte[] shortbuf = new byte[2];
	private byte[] intbuf = new byte[4];
	private byte[] longbuf = new byte[8];

	private Inflater decompresser;

	public ZipFileReader(File file, int blockSize) throws IOException {
		this.f = new RandomAccessFile(file, "r");

		LogFileHeader header = LogFileHeader.extractHeader(f);
		if (header.version() != 2)
			throw new IOException("invalid log version");
		f.seek(header.size());

		this.buffer = new byte[blockSize];
		this.block = new byte[blockSize];
		this.blockBuffer = ByteBuffer.wrap(block);
		this.decompresser = new Inflater();

		this.blockOffsets = new ArrayList<Long>();
		this.lengthOffsets = new ArrayList<Long>();

		this.length = header.size();
		long offset = header.size();
		while (offset < f.length()) {
			lengthOffsets.add(length);
			blockOffsets.add(offset);

			int compLen = f.readInt();
			offset += compLen + BLOCK_HEADER_SIZE;
			try {
				f.read(buffer, 0, compLen);
				decompresser.setInput(buffer, 0, compLen);
				int uncompSize = decompresser.inflate(block);
				decompresser.reset();
				blockBuffer.clear();
				length += uncompSize;
			} catch (DataFormatException e) {
				throw new IOException(e);
			}
		}
	}

	public long length() throws IOException {
		return length;
	}

	public void seek(long pos) {
		this.position = pos;
	}

	public short readShort() throws IOException {
		read(shortbuf);
		short value = 0;

		for (int i = 0; i < 2; i++) {
			value <<= 8;
			value |= shortbuf[i] & 0xFF;
		}

		return value;
	}

	public int readInt() throws IOException {
		read(intbuf);
		int value = 0;

		for (int i = 0; i < 4; i++) {
			value <<= 8;
			value |= intbuf[i] & 0xFF;
		}

		return value;
	}

	public long readLong() throws IOException {
		read(longbuf);
		long value = 0;

		for (int i = 0; i < 8; i++) {
			value <<= 8;
			value |= longbuf[i] & 0xFFL;
		}

		return value;
	}

	public int read(byte[] b) throws IOException {
		long blockNum = getBlockNumber(position);
		load(blockNum);

		int offset = (int) (position - lengthOffsets.get((int) blockNum));
		blockBuffer.position(offset);

		// can read at once? or need next block?
		int canRead = currentBlockSize - offset;
		int toRead = b.length;

		if (canRead >= toRead) {
			blockBuffer.get(b);
			position += b.length;
			return b.length;
		}

		// read chained blocks
		int readBytes = 0;
		try {
			do {
				int nextRead = (canRead >= toRead) ? toRead : canRead;
				blockBuffer.get(b, readBytes, nextRead);

				// read bytes addition should before load(). otherwise, last
				// fetch will return 0 even if it has some read bytes.
				readBytes += nextRead;
				position += nextRead;

				// if next block is needed, load it
				if (nextRead < toRead) {
					load(++blockNum);
					canRead = currentBlockSize;
					if (canRead == 0)
						break;
				}

				toRead -= nextRead;
			} while (toRead > 0);
		} catch (EOFException e) {
		}

		return readBytes;
	}

	private long getBlockNumber(long pos) {
		int l = 0;
		int r = lengthOffsets.size() - 1;
		while (l < r) {
			int m = (l + r) / 2;
			long l1 = lengthOffsets.get(m);
			long l2 = lengthOffsets.get(m + 1);

			if (l1 <= pos && pos < l2)
				return (long) m;
			else if (pos == l2)
				return (long) (m + 1);
			else if (pos < l1)
				r = m;
			else if (pos > l2)
				l = m + 1;
		}
		throw new IllegalArgumentException("invalid position: " + pos);
	}

	private void load(long blockNum) throws IOException {
		if (currentBlockNum == blockNum)
			return;

		if (blockNum >= blockOffsets.size()) {
			currentBlockSize = 0;
			return;
		}

		long offset = blockOffsets.get((int) blockNum);

		f.seek(offset);
		int compressedLength = f.readInt();
		f.read(buffer, 0, compressedLength);

		// decompress
		try {
			decompresser.setInput(buffer, 0, compressedLength);
			currentBlockSize = decompresser.inflate(block);
			decompresser.reset();
			blockBuffer.clear();
		} catch (DataFormatException e) {
			throw new IOException(e);
		}

		currentBlockNum = blockNum;
	}

	public void close() throws IOException {
		decompresser.end();
		f.close();
	}
}
