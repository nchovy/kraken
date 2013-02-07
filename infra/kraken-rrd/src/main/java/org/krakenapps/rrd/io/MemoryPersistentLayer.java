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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MemoryPersistentLayer extends PersistentLayer {
	private static final int DEFAULT_CAPACITY = 8192;

	private byte[] b;
	private ByteBuffer readbuf;
	private ByteBuffer writebuf;

	public MemoryPersistentLayer() {
		reallocate();
	}

	public MemoryPersistentLayer(byte[] oldBuffer, int size) {
		reallocate(size / DEFAULT_CAPACITY * DEFAULT_CAPACITY);
		readbuf.put(oldBuffer, 0, size);
	}

	private void reallocate() {
		reallocate((readbuf != null) ? (readbuf.capacity() * 2) : DEFAULT_CAPACITY);
	}

	private void reallocate(int capacity) {
		if (b != null)
			b = Arrays.copyOf(b, capacity);
		else
			b = new byte[capacity];

		int readpos = (readbuf != null) ? readbuf.position() : 0;
		readbuf = ByteBuffer.wrap(b);
		readbuf.position(readpos);

		int writepos = (writebuf != null) ? writebuf.position() : 0;
		writebuf = ByteBuffer.wrap(b);
		writebuf.position(writepos);
		setLimit();
	}

	private void checkRemaining(int needs) {
		if (writebuf.remaining() < needs)
			reallocate();
	}

	private void setLimit() {
		readbuf.limit(writebuf.position());
	}

	@Override
	public void open(boolean read) {
		readbuf.position(0);
		writebuf.position(0);
	}

	@Override
	public void write(int b) throws IOException {
		checkRemaining(1);
		writebuf.put((byte) (b & 0xFF));
		setLimit();
	}

	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		checkRemaining(len);
		writebuf.put(b, off, len);
		setLimit();
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		checkRemaining(1);
		writebuf.put((byte) (v ? 1 : 0));
		setLimit();
	}

	@Override
	public void writeByte(int v) throws IOException {
		checkRemaining(1);
		write(v);
		setLimit();
	}

	@Override
	public void writeShort(int v) throws IOException {
		checkRemaining(2);
		writebuf.putShort((short) (v & 0xFFFF));
		setLimit();
	}

	@Override
	public void writeChar(int v) throws IOException {
		checkRemaining(2);
		writebuf.putChar((char) (v & 0xFFFF));
		setLimit();
	}

	@Override
	public void writeInt(int v) throws IOException {
		checkRemaining(4);
		writebuf.putInt(v);
		setLimit();
	}

	@Override
	public void writeLong(long v) throws IOException {
		checkRemaining(8);
		writebuf.putLong(v);
		setLimit();
	}

	@Override
	public void writeFloat(float v) throws IOException {
		checkRemaining(4);
		writebuf.putFloat(v);
		setLimit();
	}

	@Override
	public void writeDouble(double v) throws IOException {
		checkRemaining(8);
		writebuf.putDouble(v);
		setLimit();
	}

	@Override
	public void writeBytes(String s) throws IOException {
		byte[] bytes = s.getBytes("utf-8");
		checkRemaining(2 + bytes.length);
		writebuf.putShort((short) bytes.length);
		writebuf.put(bytes);
		setLimit();
	}

	@Override
	public void writeChars(String s) throws IOException {
		char[] charArray = s.toCharArray();
		checkRemaining(charArray.length);
		for (char c : charArray)
			writebuf.putChar(c);
		setLimit();
	}

	@Override
	public void writeUTF(String s) throws IOException {
		writeBytes(s);
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		readbuf.get(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		readbuf.get(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		readbuf.position(readbuf.position() + n);
		return n;
	}

	@Override
	public boolean readBoolean() throws IOException {
		return (readbuf.get() != (byte) 0x0);
	}

	@Override
	public byte readByte() throws IOException {
		return readbuf.get();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return (readbuf.get() & 0xFFFFFFFF);
	}

	@Override
	public short readShort() throws IOException {
		return readbuf.getShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return (readbuf.getShort() & 0xFFFFFFFF);
	}

	@Override
	public char readChar() throws IOException {
		return readbuf.getChar();
	}

	@Override
	public int readInt() throws IOException {
		return readbuf.getInt();
	}

	@Override
	public long readLong() throws IOException {
		return readbuf.getLong();
	}

	@Override
	public float readFloat() throws IOException {
		return readbuf.getFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return readbuf.getDouble();
	}

	@Override
	public String readLine() throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		byte[] b = new byte[readbuf.getShort()];
		read(b);
		return new String(b, "utf-8");
	}

	@Override
	public void close() throws IOException {
		readbuf.position(0);
		writebuf.position(0);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		len = Math.max(0, Math.min(len, readbuf.remaining() - off));
		readbuf.get(b, off, len);
		return len;
	}
}
