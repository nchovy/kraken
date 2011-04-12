package org.krakenapps.rrd;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.commons.lang.NotImplementedException;

public class FilePersistentLayer implements RrdPersistentLayer {
	private final String absPath;
	public RandomAccessFile fileObj;
	public boolean fileOpened;

	private ByteBuffer readBuffer;
	private ByteBuffer writeBuffer;

	public FilePersistentLayer(File file) throws IOException {
		this.absPath = file.getAbsolutePath();
		this.fileObj = new RandomAccessFile(file, "rw");
		readBuffer = allocNewBuffer();
		writeBuffer = allocNewBuffer();
		readBuffer.flip();
		fileOpened = true;
	}

	@Override
	public void close() {
		try {
			if (writeBuffer.position() != 0) {
				writeBuffer.flip();
				fileObj.write(writeBuffer.array(), writeBuffer.position(), writeBuffer.remaining());
			}
			fileOpened = false;
			fileObj.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void reopen() throws IOException {
		if (fileOpened)
			close();
		fileObj = new RandomAccessFile(this.absPath, "rw");
		fileOpened = true;
		seek(0);
	}

	@Override
	public long length() {
		try {
			return fileObj.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int read(byte[] b) {
		try {
			if (readBuffer.remaining() < b.length) {
				int oldRemaining = readBuffer.remaining();
				readBuffer.compact();
				int ret = fileObj.read(readBuffer.array(), readBuffer.position(), readBuffer.remaining());
				if (ret == -1) {
					int remaining = readBuffer.remaining();
					readBuffer.get(b, 0, remaining);
					return remaining;
				} else if (oldRemaining + ret < b.length) {
					int remaining = oldRemaining + ret;
					readBuffer.limit(remaining);
					readBuffer.position(0);
					readBuffer.get(b, 0, remaining);
					return remaining;
				} else {
					readBuffer.limit(oldRemaining + ret);
					readBuffer.position(0);					
				}
			}
			readBuffer.get(b);
			return b.length;
		} catch (BufferUnderflowException e) {
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int len) {
		try {
			if (readBuffer.remaining() < len) {
				int oldRemaining = readBuffer.remaining();
				readBuffer.compact();
				int ret = fileObj.read(readBuffer.array(), readBuffer.position(), readBuffer.remaining());
				if (ret == -1) {
					int remaining = readBuffer.remaining();
					readBuffer.get(b, off, remaining);
					return remaining;
				} else if (oldRemaining + ret < len) {
					int remaining = oldRemaining + ret;
					readBuffer.limit(remaining);
					readBuffer.position(0);
					readBuffer.get(b, off, remaining);
					return remaining;
				} else {
					readBuffer.limit(oldRemaining + ret);
					readBuffer.position(0);					
				}
			}
			readBuffer.get(b, off, len);
			return len;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean readBoolean() throws IOException {
		fillUpReadBuffer(Integer.SIZE / 8);
		int ret = readBuffer.getInt();
		return ret != 0;
	}

	private void fillUpReadBuffer(int minimumRequiredSize) throws IOException {
		if (readBuffer.remaining() < minimumRequiredSize) {
			if (!fileOpened)
				reopen();
			int oldRemaining = readBuffer.remaining();
			readBuffer.compact();
			int ret = fileObj.read(readBuffer.array(), readBuffer.position(), readBuffer.remaining());
			if (ret == -1 || oldRemaining + ret < minimumRequiredSize) {
				throw new BufferUnderflowException();
			}
			readBuffer.limit(oldRemaining + ret);
			readBuffer.position(0);
		}
	}

	@Override
	public byte readByte() throws IOException {
		fillUpReadBuffer(Byte.SIZE / 8);
		return readBuffer.get();
	}

	@Override
	public double readDouble() throws IOException {
		fillUpReadBuffer(Double.SIZE / 8);
		return readBuffer.getDouble();
	}

	@Override
	public long readLong() throws IOException {
		fillUpReadBuffer(Long.SIZE / 8);
		return readBuffer.getLong();
	}

	@Override
	public int readInteger() throws IOException {
		fillUpReadBuffer(Integer.SIZE / 8);
		return readBuffer.getInt();
	}

	@Override
	public short readShort() throws IOException {
		fillUpReadBuffer(Short.SIZE / 8);
		return readBuffer.getShort();
	}

	@Override
	public String readString() throws IOException {
		try {
			short len = readShort();
			ByteBuffer buf = ByteBuffer.allocate(len);
			fillUpReadBuffer(len);
			readBuffer.get(buf.array());

			CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();

			return decoder.decode(buf).toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}

	@Override
	public void seek(long pos) throws IOException {
		fileObj.seek((int) pos);
		readBuffer = allocNewBuffer();
		readBuffer.limit(0);
		writeBuffer = allocNewBuffer();
	}

	@Override
	public int skipBytes(int n) throws IOException {
		writeBuffer.flip();
		fileObj.write(writeBuffer.array(), writeBuffer.position(), writeBuffer.remaining());
		int ret = fileObj.skipBytes(n);
		readBuffer = allocNewBuffer();
		readBuffer.flip();
		writeBuffer = allocNewBuffer();
		return ret;
	}

	@Override
	public void write(byte[] b) throws IOException {
		flushWhenExpectedOverflow(b.length);
		writeBuffer.put(b);
	}

	private void flushWhenExpectedOverflow(int len) throws IOException {
		if (writeBuffer.capacity() - writeBuffer.position() - 1 < len) {
			if (!fileOpened)
				reopen();
			writeBuffer.flip();
			fileObj.write(writeBuffer.array(), writeBuffer.position(), writeBuffer.remaining());
			writeBuffer = allocNewBuffer();
		}
	}

	private ByteBuffer allocNewBuffer() {
		return ByteBuffer.allocate(5 * 1024);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		flushWhenExpectedOverflow(len);
		writeBuffer.put(b, off, len);
	}

	@Override
	public void writeBoolean(boolean v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Integer.SIZE));
		writeBuffer.putInt(v ? 1 : 0);
	}

	@Override
	public void writeInteger(int v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Integer.SIZE));
		writeBuffer.putInt(v);
	}

	@Override
	public void writeShort(short v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Short.SIZE));
		writeBuffer.putShort(v);
	}

	private int getByteSize(int nBit) {
		return (nBit + 7) / 8;
	}

	@Override
	public void writeByte(int v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Byte.SIZE));
		writeBuffer.put((byte) v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Double.SIZE));
		writeBuffer.putDouble(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		flushWhenExpectedOverflow(getByteSize(Long.SIZE));
		writeBuffer.putLong(v);
	}

	@Override
	public void writeString(String s) throws IOException {
		byte[] utf8buf = s.getBytes("UTF-8");
		flushWhenExpectedOverflow(utf8buf.length + getByteSize(Short.SIZE));
		if (utf8buf.length > Short.MAX_VALUE)
			throw new IllegalArgumentException("string is longer than Short.MAX_VALUE");
		writeBuffer.putShort((short) utf8buf.length);
		writeBuffer.put(utf8buf);
	}

	public String getAbsPath() {
		return absPath;
	}

	@Override
	public void truncate() throws IOException {
		fileObj.getChannel().truncate(0);
		readBuffer = allocNewBuffer();
		readBuffer.flip();
		writeBuffer = allocNewBuffer();
	}

	@Override
	public void sync() throws IOException {
		writeBuffer.flip();
		fileObj.write(writeBuffer.array(), writeBuffer.position(), writeBuffer.remaining());
		writeBuffer = allocNewBuffer();
	}

	@Override
	public <T extends Enum<T>> T readEnum(Class<T> t) throws IOException {
		return (T) T.valueOf(t, readString());
	}

	@Override
	public <T extends Enum<T>> void writeEnum(T v) throws IOException {
		writeString(v.toString());

	}

	@Override
	public long remaining() {
		throw new NotImplementedException();
	}

}
