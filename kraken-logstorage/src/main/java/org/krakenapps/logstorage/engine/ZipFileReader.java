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
package org.krakenapps.logstorage.engine;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ZipFileReader {
	private static final int BLOCK_HEADER_SIZE = 4;

	private RandomAccessFile f;

	// block size in bytes
	private int blockSize;

	// retain seek position
	private long position;

	// temporary buffer for compressed binary
	private byte[] buffer;

	// uncompressed block data
	private byte[] block;

	// wrapped block
	private ByteBuffer blockBuffer;

	// current block number (buffered)
	private long currentBlockNum = -1;

	// last block will be less than block size
	private int currentBlockSize = 0;

	private byte[] intbuf;
	private byte[] longbuf;

	private Inflater decompresser;

	public ZipFileReader(File file, int blockSize) throws FileNotFoundException {
		this.f = new RandomAccessFile(file, "r");
		this.blockSize = blockSize;

		this.buffer = new byte[blockSize];
		this.block = new byte[blockSize];
		this.blockBuffer = ByteBuffer.wrap(block);
		this.decompresser = new Inflater();
	}

	private long getBlockNumber(long pos) {
		return pos / blockSize;
	}

	public void seek(long pos) {
		this.position = pos;
	}

	public int readInt() throws IOException {
		read(intbuf);
		int value = 0;

		for (int i = 0; i < 4; i++) {
			value <<= 8;
			value |= intbuf[i];
		}

		return value;
	}

	public long readLong() throws IOException {
		read(longbuf);
		int value = 0;

		for (int i = 0; i < 8; i++) {
			value <<= 8;
			value |= longbuf[i];
		}

		return value;
	}

	public int read(byte[] b) throws IOException {
		long blockNum = getBlockNumber(position);
		load(blockNum);

		int offset = (int) (position - blockNum * blockSize);
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
				}

				toRead -= nextRead;
			} while (toRead > 0);
		} catch (EOFException e) {
		}

		return readBytes;
	}

	private void load(long blockNum) throws IOException {
		if (currentBlockNum == blockNum)
			return;

		// go to block
		long i = 0;
		long offset = 0;

		while (i++ < blockNum) {
			f.seek(offset);
			offset += f.readInt() + BLOCK_HEADER_SIZE;
		}

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
