package org.krakenapps.rrd;

import java.io.IOException;

public interface RrdPersistentLayer {
	void close();
	void reopen() throws IOException;
	long length();
	
	void seek(long pos) throws IOException;
	int skipBytes(int n) throws IOException;
	
	void sync() throws IOException;

	int read(byte[] b);
	int read(byte[] b, int off, int len);
	boolean readBoolean() throws IOException;
	byte readByte() throws IOException;
	double readDouble() throws IOException;
	long readLong() throws IOException;
	String readString() throws IOException;
	int readInteger() throws IOException;
	short readShort() throws IOException;
	<T extends Enum<T>> T readEnum(Class<T> t) throws IOException;
	
	void write(byte[] b) throws IOException;
	void write(byte[] b, int off, int len) throws IOException;
	void writeBoolean(boolean v) throws IOException;
	void writeByte(int v) throws IOException;
	void writeDouble(double v) throws IOException;
	void writeLong(long v) throws IOException;
	void writeInteger(int v) throws IOException;
	void writeShort(short v) throws IOException;
	void writeString(String s) throws IOException;
	void truncate() throws IOException;
	<T extends Enum<T>> void writeEnum(T v) throws IOException;
	long remaining();

}
