package org.krakenapps.rrd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class MemoryRrd implements Rrd {
	RrdRawImpl impl = new RrdRawImpl();
	RrdPersistentLayer persLayer = new MemoryPersistentLayer();

	@Override
	public void dump(OutputStream stream) {
		impl.dump(stream);
	}

	@Override
	public synchronized FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		return impl.fetch(f, start, end, resolution);
	}

	public synchronized void init(RrdConfig config) {
		impl.init(config);
	}

	public synchronized void init(RrdPersistentLayer persLayer) {
		if (!load(persLayer)) {
			throw new BrokenPersLayerException();
		}
	}

	public synchronized void init(RrdConfig config, RrdPersistentLayer persLayer) {
		this.persLayer = persLayer;
		impl.init(config);
	}

	public synchronized boolean load() {
		boolean ret = false;
		try {
			persLayer.reopen();
			ret = impl.readFromPersLayer(new PersLayerInputStream(persLayer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public synchronized boolean load(RrdPersistentLayer persLayer) {
		return impl.readFromPersLayer(new PersLayerInputStream(persLayer));
	}
	
	public synchronized boolean load(InputStream stream) {
		return impl.readFromPersLayer(stream);
	}

	@Override
	public synchronized void save() {
		try {
			persLayer.reopen();
			impl.writeToPersLayer(new PersLayerOutputStream(persLayer));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void save(RrdPersistentLayer persLayer) {
		impl.writeToPersLayer(new PersLayerOutputStream(persLayer));
	}
	
	public synchronized void save(OutputStream stream) {
		impl.writeToPersLayer(stream);
	}

	@Override
	public synchronized void update(Date time, Double[] values) {
		impl.update(time, values);
	}
}
