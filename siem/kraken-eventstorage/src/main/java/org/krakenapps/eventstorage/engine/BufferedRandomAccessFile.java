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
package org.krakenapps.eventstorage.engine;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class BufferedRandomAccessFile {
	private RandomAccessFile raf;
	private int hdrSize;
	private int capacity;
	private ByteBuffer bb;
	private long fp = -1L;
	private long length;
	private int maxPos = 0;
	private volatile boolean modified = false;

	public BufferedRandomAccessFile(File file, String mode, int capacity) throws IOException {
		this(file, mode, capacity, 0);
	}

	public BufferedRandomAccessFile(File file, String mode, int capacity, int hdrSize) throws IOException {
		this(new RandomAccessFile(file, mode), capacity, hdrSize);
	}

	public BufferedRandomAccessFile(RandomAccessFile raf, int capacity) throws IOException {
		this(raf, capacity, 0);
	}

	public BufferedRandomAccessFile(RandomAccessFile raf, int capacity, int hdrSize) throws IOException {
		this.raf = raf;
		this.hdrSize = hdrSize;
		this.capacity = capacity;
		this.bb = ByteBuffer.allocate(capacity);
		this.length = raf.length() - hdrSize;
		seek(length);
	}

	public synchronized void seek(long pos) throws IOException {
		length = Math.max(length, Math.max(pos, fp + bb.position() - hdrSize));
		pos += hdrSize;
		int offset = (int) (pos % capacity);
		if (fp != pos - offset) {
			if (modified)
				flush(false);

			fp = pos - offset;
			raf.seek(fp);
			int readed = raf.read(bb.array());
			if (readed != capacity) {
				if (readed == -1)
					readed = 0;
				Arrays.fill(bb.array(), readed, capacity, (byte) 0x00);
			}
			maxPos = offset;
		} else
			maxPos = Math.max(maxPos, Math.max(offset, bb.position()));
		bb.position(offset);
	}

	public long length() {
		return Math.max(length, fp + Math.max(maxPos, bb.position()) - hdrSize);
	}

	public synchronized void flush(boolean sync) throws IOException {
		raf.seek(fp);
		raf.write(bb.array(), 0, Math.max(maxPos, bb.position()));
		if (sync)
			raf.getFD().sync();
		modified = false;
	}

	public synchronized void write(byte b) throws IOException {
		if (!bb.hasRemaining())
			seek(fp + capacity - hdrSize);
		bb.put(b);
		modified = true;
	}

	public synchronized void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	public synchronized void write(byte[] b, int offset, int length) throws IOException {
		int remain = bb.remaining();
		if (remain >= length) {
			bb.put(b, offset, length);
			modified = true;
		} else {
			bb.put(b, offset, remain);
			modified = true;
			raf.seek(fp + capacity);
			raf.write(b, offset + remain, length - remain);
			seek(fp + capacity + (length - remain) - hdrSize);
		}
	}

	public synchronized void writeShort(short s) throws IOException {
		int remain = bb.remaining();
		if (remain >= 2) {
			bb.putShort(s);
			modified = true;
		} else {
			for (int i = 1; i >= 0; i--) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				bb.put((byte) ((s >>> (i * 8)) & 0xFF));
				modified = true;
			}
		}
	}

	public synchronized void writeInt(int i) throws IOException {
		int remain = bb.remaining();
		if (remain >= 4) {
			bb.putInt(i);
			modified = true;
		} else {
			for (int j = 3; j >= 0; j--) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				bb.put((byte) ((i >>> (j * 8)) & 0xFF));
				modified = true;
			}
		}
	}

	public synchronized void writeLong(long l) throws IOException {
		int remain = bb.remaining();
		if (remain >= 8) {
			bb.putLong(l);
			modified = true;
		} else {
			for (int i = 7; i >= 0; i--) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				bb.put((byte) ((l >>> (i * 8)) & 0xFF));
				modified = true;
			}
		}
	}

	public synchronized void read(byte[] b) throws IOException {
		if (bb.remaining() >= b.length)
			bb.get(b);
		else {
			int remain = bb.remaining();
			bb.get(b, 0, remain);
			raf.seek(fp + capacity);
			raf.read(b, remain, b.length - remain);
			seek(fp + capacity + (b.length - remain) - hdrSize);
		}
	}

	public synchronized byte readByte() throws IOException {
		if (!bb.hasRemaining())
			seek(fp + capacity - hdrSize);
		return bb.get();
	}

	public synchronized short readShort() throws IOException {
		if (bb.remaining() >= 2)
			return bb.getShort();
		else {
			short s = 0;
			for (int i = 0; i < 2; i++) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				s <<= 8;
				s |= bb.get() & 0xFF;
			}
			return s;
		}
	}

	public synchronized int readInt() throws IOException {
		if (bb.remaining() >= 4)
			return bb.getInt();
		else {
			int i = 0;
			for (int j = 0; j < 4; j++) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				i <<= 8;
				i |= bb.get() & 0xFF;
			}
			return i;
		}
	}

	public synchronized long readLong() throws IOException {
		if (bb.remaining() >= 8)
			return bb.getLong();
		else {
			long l = 0L;
			for (int i = 0; i < 8; i++) {
				if (!bb.hasRemaining())
					seek(fp + capacity - hdrSize);
				l <<= 8;
				l |= bb.get() & 0xFF;
			}
			return l;
		}
	}

	public void close() {
		try {
			flush(true);
		} catch (IOException e) {
		}
		try {
			raf.close();
		} catch (IOException e) {
		}
	}
}
