package org.krakenapps.rrd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class AutoPersistFileRrd implements Rrd {
	private RrdRawImpl impl = new RrdRawImpl();
	RrdPersistentLayer persLayer = null;
	boolean isInitialized = false;
	
	public AutoPersistFileRrd(RrdConfig config, FilePersistentLayer fileLayer) {
		if (fileLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = fileLayer;
		impl.init(config);
	}
	
	public AutoPersistFileRrd(FilePersistentLayer fileLayer) {
		if (fileLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = fileLayer;
		if (!load_internal()) {
			throw new BrokenPersLayerException(); 
		}
	}

	@Override
	public void dump(OutputStream stream) {
		try {
			if (!load()) {
				throw new BrokenPersLayerException(); 
			}
			impl.dump(stream);
		} finally {
			persLayer.close();
		}
	}

	@Override
	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		try {
			return impl.fetch(f, start, end, resolution);
		} finally {
			persLayer.close();
		}
	}

	@Override
	public void update(Date time, Double[] values) {
		try {
			impl.update(time, values);
			save();
		} finally {
			persLayer.close();
		}
	}
	
	private boolean load_internal() {
		try {
			persLayer.reopen();
			return impl.readFromPersLayer(new PersLayerInputStream(this.persLayer));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean load() {
		return load_internal();
	}

	public boolean load(RrdPersistentLayer persLayer) {
		return impl.readFromPersLayer(new PersLayerInputStream(persLayer));
	}

	@Override
	public void save() {
		try {
			persLayer.reopen();
			impl.writeToPersLayer(new PersLayerOutputStream(this.persLayer));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void save(RrdPersistentLayer persLayer) {
		impl.writeToPersLayer(new PersLayerOutputStream(persLayer));
	}

	@Override
	public boolean load(InputStream istream) {
		return impl.readFromPersLayer(istream);
	}

	@Override
	public void save(OutputStream ostream) {
		impl.writeToPersLayer(ostream);
	}
}
