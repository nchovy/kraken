package org.krakenapps.util.directoryfile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;

public class ByteBufferOutputStream extends OutputStream {

	private final MappedByteBuffer map;

	public ByteBufferOutputStream(MappedByteBuffer map) {
		this.map = map;
	}
	
	public MappedByteBuffer getMappedByteBuffer() {
		return map;
	}

	@Override
	public void write(int b) throws IOException {
		map.put((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		map.put(b);
	}

	@Override
	public void flush() throws IOException {
		map.force();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		map.put(b, off, len);
	}

	@Override
	public void close() throws IOException {
		map.force();
	}
	
	

}
