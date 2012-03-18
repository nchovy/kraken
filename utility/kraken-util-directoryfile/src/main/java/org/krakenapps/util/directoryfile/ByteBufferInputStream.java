package org.krakenapps.util.directoryfile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;

public class ByteBufferInputStream extends InputStream {

	private final MappedByteBuffer map;

	public ByteBufferInputStream(MappedByteBuffer map) {
		this.map = map;
	}
	
	public MappedByteBuffer getMappedByteBuffer() {
		return map;
	}

	@Override
	public int read() throws IOException {
		if (!map.isLoaded()) {
			map.load();
		}
		return map.get();
	}

	@Override
	public int available() throws IOException {
		return map.remaining();
	}

	@Override
	public synchronized void mark(int readlimit) {
		if (!map.isLoaded()) {
			map.load();
		}
		map.mark();
	}

	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (!map.isLoaded()) {
			map.load();
		}
		if (map.remaining() < len) {
			len = map.remaining();
		}			
		map.get(b, off, len);
		return len;
	}

	@Override
	public int read(byte[] b) throws IOException {
		if (!map.isLoaded()) {
			map.load();
		}
		map.get(b);
		return b.length;
	}

	@Override
	public synchronized void reset() throws IOException {
		if (!map.isLoaded()) {
			map.load();
		}
		map.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		if (!map.isLoaded()) {
			map.load();
		}
		if (map.position() + n > map.limit()) {
			long ret = map.remaining();
			map.position(map.limit());
			return ret;
		} else {
			map.position((int) (map.position() + n));
			return n;
		}
	}

}
