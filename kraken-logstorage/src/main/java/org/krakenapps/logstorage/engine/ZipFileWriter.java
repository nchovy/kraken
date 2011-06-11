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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;

public class ZipFileWriter {
	private RandomAccessFile f;
	private int blockSize;
	private ByteBuffer bb;
	private ByteBuffer compressed;
	private Deflater compresser;
	private byte[] intbuf;

	/**
	 * @param blockSize
	 *            compress data block size
	 * @throws FileNotFoundException
	 */
	public ZipFileWriter(File file, int blockSize) throws FileNotFoundException {
		this.blockSize = blockSize;
		this.bb = ByteBuffer.allocate(blockSize);
		this.compressed = ByteBuffer.allocate(blockSize);
		this.compresser = new Deflater(1);
		this.intbuf = new byte[4];

		f = new RandomAccessFile(file, "rw");
	}

	public int getBlockSize() {
		return blockSize;
	}

	public void append(byte[] b, int offset, int length) throws IOException {
		do {
			int free = bb.remaining();
			int len = (free >= length) ? length : free;
			bb.put(b, offset, len);

			// is flush needed?
			if (len == free)
				flush();
			
			offset += len;
			length -= len;
		} while (length > 0);
	}

	public void flush() throws IOException {
		compresser.setInput(bb.array(), 0, bb.position());
		compresser.finish();
		int written = compresser.deflate(compressed.array());

		// compressed size + compressed payload
		prepareInt(written, intbuf);
		f.write(intbuf);
		f.write(compressed.array(), 0, written);

		// reset
		bb.clear();
		compresser.reset();
	}

	private void prepareInt(int l, byte[] b) {
		for (int i = 0; i < 4; i++)
			b[i] = (byte) ((l >> ((3 - i) * 8)) & 0xff);
	}

	public void close() throws IOException {
		flush();
		compresser.end();
		f.close();
	}
}
