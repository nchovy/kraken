package org.krakenapps.rrd;

import java.io.IOException;
import java.io.InputStream;

public class PersLayerInputStream extends InputStream {
	
	private final RrdPersistentLayer persLayer;

	public PersLayerInputStream(RrdPersistentLayer persLayer) {
		this.persLayer = persLayer;
	}

	@Override
	public int read() throws IOException {
		return persLayer.readByte();
	}

	@Override
	public int available() throws IOException {
		return (int) persLayer.remaining();
	}

	@Override
	public void close() throws IOException {
		persLayer.close();
		super.close();
	}

	@Override
	public synchronized void mark(int readlimit) {
		// intended no operation
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return persLayer.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return persLayer.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		// intended no operation
	}

	@Override
	public long skip(long n) throws IOException {
		return persLayer.skipBytes((int) n);
	}

}
