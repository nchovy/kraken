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
package org.krakenapps.logstorage.file;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class BufferedRandomAccessFileReader implements DataInput {
	private static final int BUFFER_SIZE = 8192;
	private final RandomAccessFile file;
	private ByteBuffer buf;
	private DataInputStream dataInputStream;

	private boolean isInvalidated = true;
	private long bufStartPos = 0;

	private boolean isClosed = false;

	public BufferedRandomAccessFileReader(File path) throws FileNotFoundException {
		file = new RandomAccessFile(path, "r");
		buf = ByteBuffer.allocate(BUFFER_SIZE);
		dataInputStream = new DataInputStream(new InputStream() {
			@Override
			public synchronized int read() throws IOException {
				if (!buf.hasRemaining()) {
					if (file.length() - (bufStartPos + buf.position()) < 1)
						return -1;
					else
						syncBuffer();
				}
				return (int) buf.get() & 0xff;
			}

			@Override
			public synchronized int read(byte[] bytes, int off, int len) throws IOException {
				if (len > buf.capacity()) {
					bufStartPos += buf.position();
					isInvalidated = true;
					return file.read(bytes, off, len);
				} else if (len > buf.remaining()) {
					bufStartPos += buf.position();
					syncBuffer();
					buf.get(bytes, off, len);
					return len;
				} else {
					len = Math.min(len, buf.remaining());
					buf.get(bytes, off, len);
					return len;
				}
			}
		});
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		dataInputStream.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		dataInputStream.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}

		return dataInputStream.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readChar();
	}

	@Override
	public int readInt() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readInt();
	}

	@Override
	public long readLong() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readDouble();
	}

	@SuppressWarnings("deprecation")
	@Override
	public String readLine() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}
		return dataInputStream.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		if (isInvalidated) {
			syncBuffer();
		}

		return dataInputStream.readUTF();
	}

	public void seek(long pos) throws IOException {
		if (pos > bufStartPos + buf.remaining()) {
			buf.clear();
			bufStartPos = pos;
			syncBuffer(buf.capacity());
		} else if (pos - bufStartPos < 0) {
			buf.clear();
			long seekPos = pos - BUFFER_SIZE;
			if (seekPos < 0)
				seekPos = 0;
			bufStartPos = seekPos;
			file.seek(bufStartPos);
			buf.clear();
			if (buf.capacity() < BUFFER_SIZE * 2) {
				buf = ByteBuffer.allocate(BUFFER_SIZE * 2);
			}

			int read = file.read(buf.array());
			if (read != -1) {
				buf.limit(read);
				if (seekPos == 0) {
					buf.position((int) pos);
				} else {
					buf.position(BUFFER_SIZE);
				}
			}

			isInvalidated = false;
		} else {
			buf.position((int) (pos - bufStartPos));
		}
	}

	public long length() throws IOException {
		return file.length();
	}

	public long getFilePointer() throws IOException {
		invalidateBuffer();
		return file.getFilePointer();
	}

	public FileDescriptor getFD() throws IOException {
		invalidateBuffer();
		return file.getFD();
	}

	public FileChannel getChannel() {
		invalidateBuffer();
		return file.getChannel();
	}

	private void invalidateBuffer() {
		isInvalidated = true;
	}

	private void syncBuffer() throws IOException {
		syncBuffer((int) buf.capacity());
	}

	private void syncBuffer(int bufSize) throws IOException {
		long nextSeekPos = bufStartPos + buf.position();
		file.seek(nextSeekPos);
		bufStartPos = nextSeekPos;
		buf.clear();
		if (buf.capacity() < bufSize) {
			if (bufSize != buf.capacity()) {
				buf = ByteBuffer.allocate(bufSize);
			}
		}
		int read = file.read(buf.array());
		if (read != -1) {
			buf.limit(read);
		}
		isInvalidated = false;
	}

	public void close() throws IOException {
		if (isClosed)
			return;
		file.close();
		isClosed = true;
	}
}
