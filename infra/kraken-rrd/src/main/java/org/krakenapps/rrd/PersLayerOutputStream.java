package org.krakenapps.rrd;

import java.io.IOException;
import java.io.OutputStream;

import org.krakenapps.rrd.RrdPersistentLayer;

public class PersLayerOutputStream extends OutputStream {
	
	private final RrdPersistentLayer persLayer;
	public PersLayerOutputStream(RrdPersistentLayer persLayer) {
		this.persLayer = persLayer;
	}

	@Override
	public void write(int b) throws IOException {
		persLayer.writeByte(b);
	}

	@Override
	public void close() throws IOException {
		this.persLayer.close();
		super.close();
	}

	@Override
	public void flush() throws IOException {
		super.flush();
		persLayer.sync();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		persLayer.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		persLayer.write(b);
	}
}
