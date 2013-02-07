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
package org.krakenapps.rrd.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class FilePersistentLayer extends PersistentLayer {
	private static final int DEFAULT_CAPACITY = 64 * 1024;

	private File file;
	private RandomAccessFile raf;
	private int capacity;
	private ByteBuffer bb;
	private long fp = -1L;
	private long length;
	private int maxPos = 0;
	private volatile boolean modified = false;
	private boolean read;

	public FilePersistentLayer(File file) throws IOException {
		this(file, DEFAULT_CAPACITY);
	}

	public FilePersistentLayer(File file, boolean read) throws IOException {
		this(file, DEFAULT_CAPACITY, read);
	}

	public FilePersistentLayer(File file, int capacity) throws IOException {
		this(file, capacity, true);
	}

	public FilePersistentLayer(File file, int capacity, boolean read) throws IOException {
		this.file = file;
		this.bb = ByteBuffer.allocate(capacity);
		this.capacity = capacity;
		open(read);
	}

	@Override
	public void open(boolean read) {
		this.read = read;
		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		try {
			raf = new RandomAccessFile(file, "rw");
			if (!read)
				raf.setLength(0L);
			seek(0);
		} catch (IOException e) {
		}
	}

	private synchronized void seek(long pos) throws IOException {
		length = Math.max(length, Math.max(pos, fp + bb.position()));
		int offset = (int) (pos % capacity);
		if (fp != pos - offset) {
			if (modified)
				flush();

			fp = pos - offset;
			raf.seek(fp);
			int readed = read ? raf.read(bb.array()) : -1;
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
		return Math.max(length, fp + Math.max(maxPos, bb.position()));
	}

	public synchronized void flush() throws IOException {
		raf.seek(fp);
		raf.write(bb.array(), 0, Math.max(maxPos, bb.position()));
		modified = false;
	}

	@Override
	public synchronized void writeByte(int v) throws IOException {
		byte b = (byte) (v & 0xFF);
		if (!bb.hasRemaining())
			seek(fp + capacity);
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
			seek(fp + capacity + (length - remain));
		}
	}

	@Override
	public synchronized void writeBoolean(boolean v) throws IOException {
		writeByte(v ? 1 : 0);
	}

	@Override
	public synchronized void writeShort(int v) throws IOException {
		short s = (short) (v & 0xFF);
		int remain = bb.remaining();
		if (remain >= 2) {
			bb.putShort(s);
			modified = true;
		} else {
			for (int i = 1; i >= 0; i--) {
				if (!bb.hasRemaining())
					seek(fp + capacity);
				bb.put((byte) ((s >>> (i * 8)) & 0xFF));
				modified = true;
			}
		}
	}

	@Override
	public synchronized void writeChar(int v) throws IOException {
		writeShort((char) (v & 0xFF));
	}

	@Override
	public synchronized void write(int b) throws IOException {
		writeInt(b);
	}

	public synchronized void writeInt(int i) throws IOException {
		int remain = bb.remaining();
		if (remain >= 4) {
			bb.putInt(i);
			modified = true;
		} else {
			for (int j = 3; j >= 0; j--) {
				if (!bb.hasRemaining())
					seek(fp + capacity);
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
					seek(fp + capacity);
				bb.put((byte) ((l >>> (i * 8)) & 0xFF));
				modified = true;
			}
		}
	}

	@Override
	public synchronized void writeFloat(float v) throws IOException {
		writeInt(Float.floatToRawIntBits(v));
	}

	@Override
	public synchronized void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToRawLongBits(v));
	}

	@Override
	public synchronized void writeBytes(String s) throws IOException {
		write(s.getBytes());
	}

	@Override
	public synchronized void writeChars(String s) throws IOException {
		for (char c : s.toCharArray())
			writeChar(c);
	}

	@Override
	public synchronized void writeUTF(String s) throws IOException {
		byte[] bytes = s.getBytes("utf-8");
		writeShort(bytes.length);
		write(bytes);
	}

	@Override
	public synchronized int skipBytes(int n) throws IOException {
		seek(fp + bb.position() + n);
		return n;
	}

	public synchronized int read(byte[] b) throws IOException {
		readFully(b);
		return b.length;
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		readFully(b, off, len);
		return len;
	}

	@Override
	public synchronized void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public synchronized void readFully(byte[] b, int off, int len) throws IOException {
		skipBytes(off);
		if (bb.remaining() >= len)
			bb.get(b, 0, len);
		else {
			int remain = bb.remaining();
			bb.get(b, 0, remain);
			raf.seek(fp + capacity);
			raf.read(b, remain, len - remain);
			seek(fp + capacity + (len - remain));
		}
	}

	@Override
	public synchronized boolean readBoolean() throws IOException {
		return (readByte() != (byte) 0x0);
	}

	public synchronized byte readByte() throws IOException {
		if (!bb.hasRemaining())
			seek(fp + capacity);
		return bb.get();
	}

	@Override
	public synchronized int readUnsignedByte() throws IOException {
		return (readByte() & 0xFFFFFFFF);
	}

	public synchronized short readShort() throws IOException {
		if (bb.remaining() >= 2)
			return bb.getShort();
		else {
			short s = 0;
			for (int i = 0; i < 2; i++) {
				if (!bb.hasRemaining())
					seek(fp + capacity);
				s <<= 8;
				s |= bb.get() & 0xFF;
			}
			return s;
		}
	}

	@Override
	public synchronized char readChar() throws IOException {
		return (char) (readShort() & 0xFFFF);
	}

	@Override
	public synchronized int readUnsignedShort() throws IOException {
		return (readShort() & 0xFFFFFFFF);
	}

	public synchronized int readInt() throws IOException {
		if (bb.remaining() >= 4)
			return bb.getInt();
		else {
			int i = 0;
			for (int j = 0; j < 4; j++) {
				if (!bb.hasRemaining())
					seek(fp + capacity);
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
					seek(fp + capacity);
				l <<= 8;
				l |= bb.get() & 0xFF;
			}
			return l;
		}
	}

	@Override
	public synchronized float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public synchronized double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public synchronized String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized String readUTF() throws IOException {
		byte[] b = new byte[readShort()];
		read(b);
		return new String(b, "utf-8");
	}

	@Override
	public synchronized void close() {
		try {
			if (raf != null)
				flush();
		} catch (IOException e) {
		}
		try {
			if (raf != null)
				raf.close();
		} catch (IOException e) {
		}
		raf = null;
	}
}
