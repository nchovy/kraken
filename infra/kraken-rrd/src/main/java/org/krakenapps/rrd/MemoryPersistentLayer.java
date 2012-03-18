package org.krakenapps.rrd;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MemoryPersistentLayer implements RrdPersistentLayer {
	private ByteBuffer buffer;
	private int bufferCapacity;

	private final int initialBufferCapacity = 1024;

	private void reallocateBuffer(int bufferCapacity) {
		ByteBuffer newBuffer = ByteBuffer.allocate(bufferCapacity);
		if (buffer != null) {
			int oldPos = this.buffer.position();
			this.buffer.position(0);
			newBuffer.put(this.buffer);
			newBuffer.position(oldPos);
		}
		this.buffer = newBuffer;
		this.bufferCapacity = bufferCapacity;
	}

	public MemoryPersistentLayer() {
		reallocateBuffer(initialBufferCapacity);
	}

	public MemoryPersistentLayer(byte[] oldBuffer, int size) {
		reallocateBuffer(size / initialBufferCapacity * initialBufferCapacity);
		this.buffer.put(oldBuffer, 0, size);
	}
	
	@Override
	public void close() {
		// do nothing
	}

	@Override
	public long length() {
		return buffer.position();
	}
	
	@Override
	public long remaining() {
		return buffer.remaining();
	}

	@Override
	public int read(byte[] b) {
		this.buffer.get(b);
		return b.length;
	}

	@Override
	public int read(byte[] b, int off, int len) {
		if (len > this.buffer.remaining()) {
			this.buffer.get(b, off, this.buffer.remaining());
			return this.buffer.remaining();
		} else {
			this.buffer.get(b, off, len);
			return len;
		}
	}

	@Override
	public boolean readBoolean() {
		byte tmp = this.buffer.get();
		return tmp != 0 ? true : false;
	}

	@Override
	public byte readByte() {
		return this.buffer.get();
	}

	@Override
	public double readDouble() {
		return this.buffer.getDouble();
	}

	@Override
	public long readLong() {
		return this.buffer.getLong();
	}

	@Override
	public String readString() {
		short len = this.buffer.getShort();
		String ret = new String(buffer.array(), buffer.position(), len, Charset.forName("UTF-8"));
		buffer.position(buffer.position() + len);
		return ret;
	}

	@Override
	public void seek(long pos) {
		buffer.position((int) pos);
	}

	@Override
	public int skipBytes(int n) {
		if (buffer.position() + n > buffer.limit()) {
			int ret = buffer.limit() - buffer.position();
			buffer.position(buffer.limit());
			return ret;
		} else {
			buffer.position(buffer.position() + n);
			return n;
		}
	}

	@Override
	public void write(byte[] b) {
		if (buffer.remaining() < b.length) {
			reallocateBuffer(bufferCapacity * 2);
			write(b);
		} else {
			buffer.put(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if (buffer.remaining() < len) {
			reallocateBuffer(bufferCapacity * 2);
			write(b, off, len);
		} else {
			buffer.put(b, off, len);
		}
	}

	@Override
	public void writeBoolean(boolean v) {
		try {
			buffer.put((byte) (v ? 1 : 0));
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.put((byte) (v ? 1 : 0));
		}
	}

	@Override
	public void writeByte(int v) {
		try {
			buffer.put((byte) v);
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.put((byte) v);
		}
	}

	@Override
	public void writeDouble(double v) {
		try {
			buffer.putDouble(v);
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.putDouble(v);
		}
	}

	@Override
	public void writeLong(long v) {
		try {
			buffer.putLong(v);
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.putLong(v);
		}
	}

	@Override
	public void writeString(String s) {
		if (buffer.remaining() <= 2 + s.length()) {
			reallocateBuffer(bufferCapacity * 2);
			writeString(s);
		} else {
			byte[] utf8Buf = s.getBytes(Charset.forName("UTF-8"));
			buffer.putShort((short) utf8Buf.length);
			buffer.put(utf8Buf);
		}
	}

	@Override
	public void truncate() throws IOException {
		this.buffer = ByteBuffer.allocate(bufferCapacity);
	}

	@Override
	public void sync() throws IOException {
	}

	@Override
	public void reopen() throws IOException {
		
		seek(0);
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
	public int readInteger() throws IOException {
		return this.buffer.getInt();
	}

	@Override
	public short readShort() throws IOException {
		return this.buffer.getShort();
	}

	@Override
	public void writeInteger(int v) throws IOException {
		try {
			buffer.putInt(v);
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.putInt(v);
		}
	}

	@Override
	public void writeShort(short v) throws IOException {
		try {
			buffer.putShort(v);
		} catch (BufferOverflowException e) {
			reallocateBuffer(bufferCapacity * 2);
			buffer.putShort(v);
		}
	}
	
	public void flip() {
		buffer.flip();
	}

	public void shrink() {
		if (buffer.position() != 0)
			throw new InvalidStateException();
		if (this.buffer.limit() != this.buffer.capacity())
			reallocateBuffer(this.buffer.limit());
	}

	public ByteBuffer getByteBuffer() {
		return buffer; 
	}
}
