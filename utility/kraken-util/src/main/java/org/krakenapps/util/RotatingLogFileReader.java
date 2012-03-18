/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.util;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class RotatingLogFileReader implements Closeable {
	private RandomAccessFile file;
	private CharsetDecoder decoder;
	private String filePath;
	private long lastOffset;
	private String firstLine;
	private ByteBuffer byteBuffer;

	public RotatingLogFileReader(String filePath) {
		this.filePath = filePath;
		this.byteBuffer = ByteBuffer.allocate(4096);
		this.decoder = Charset.forName("utf-8").newDecoder();
	}

	public void setEncoding(Charset charset) {
		this.decoder = charset.newDecoder();
	}

	public String getFilePath() {
		return filePath;
	}

	public long getLastOffset() {
		return lastOffset;
	}

	public void setLastOffset(long lastOffset) {
		this.lastOffset = lastOffset;
	}

	public String getFirstLine() {
		return firstLine;
	}

	public void setFirstLine(String firstLine) {
		this.firstLine = firstLine;
	}

	public void open() throws FileNotFoundException, IOException {
		close(); // for sure

		file = new RandomAccessFile(filePath, "r");
		String line = file.readLine();

		if (firstLine == null) {
			// first open
			firstLine = line;
		} else if (firstLine.equals(line) == false) {
			// log rotated
			lastOffset = 0;
			firstLine = line;
		}

		file.seek(lastOffset);
	}

	public String readLine() throws IOException {
		if (file == null)
			throw new IOException("Stream not opened.");

		long offset = lastOffset;
		long remainingBytes = file.length() - offset;

		while (remainingBytes >= 0) {
			int b = file.read();

			if (isASCII(b) && isNewLine(b)) {
				byteBuffer.flip();
				if (isFirstLine())
					removeBOM(byteBuffer);

				lastOffset = file.getFilePointer();
				String line = getString(byteBuffer);
				byteBuffer.clear();
				return line;
			}

			byteBuffer.put((byte) b);
			remainingBytes -= 1;
		}

		return null;
	}

	private boolean isFirstLine() {
		return lastOffset == 0;
	}

	private boolean isNewLine(int b) {
		return b == '\n';
	}

	private boolean isASCII(int b) {
		return (b & 0x80) == 0;
	}

	private void removeBOM(ByteBuffer bb) {
		// only removes UTF-8 BOM
		if (bb.get(0) == 0xEF && bb.get(1) == 0xBB && bb.get(2) == 0xBF) {
			bb.get();
			bb.get();
			bb.get();
		}
	}

	private String getString(ByteBuffer bb) throws CharacterCodingException {
		return decoder.decode(bb).toString();
	}

	@Override
	public void close() {
		if (file == null)
			return;

		try {
			file.close();
			file = null;
		} catch (IOException e) {
			// ignore
			e.printStackTrace();
		}

	}
}
