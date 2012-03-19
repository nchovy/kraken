package org.krakenapps.rrd;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

public class CompactMemoryRrd implements Rrd {
	private MemoryPersistentLayer persLayer = new MemoryPersistentLayer();
	private WeakReference<RrdRawImpl> pImpl = new WeakReference<RrdRawImpl>(null);
	boolean isInitialized = false;

	@Override
	public void dump(OutputStream stream) {
		checkInitialized();
		RrdRawImpl impl = getRrdRawImpl();
		impl.dump(stream);
	}

	private RrdRawImpl getRrdRawImpl() {
		if (pImpl.get() == null) {
			RrdRawImpl impl = new RrdRawImpl();
			pImpl = new WeakReference<RrdRawImpl>(impl);
			readFromPersLayer(impl);
			return impl;
		} else {
			return pImpl.get();
		}
	}

	private void readFromPersLayer(RrdRawImpl impl) {
		this.persLayer.seek(0);
		impl.readFromPersLayer(new PersLayerInputStream(this.persLayer));
	}

	@Override
	public synchronized FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		checkInitialized();
		return getRrdRawImpl().fetch(f, start, end, resolution);
	}

	private void checkInitialized() {
		if (!isInitialized) 
			throw new UninitializedException();
	}
	
	public synchronized void init() {
		isInitialized = true;
	}

	public synchronized void init(RrdConfig config) {
		RrdRawImpl impl = getRrdRawImpl();
		impl.init(config);
		writeToPersLayer(impl);
		isInitialized = true;
	}

	public synchronized boolean load(InputStream stream) {
		RrdRawImpl rrdRawImpl = getRrdRawImpl();
		boolean ret = rrdRawImpl.readFromPersLayer(stream);
		writeToPersLayer(rrdRawImpl);
		return ret;
	}

	public synchronized boolean load(RrdPersistentLayer persLayer) {
		RrdRawImpl rrdRawImpl = getRrdRawImpl();
		boolean ret = rrdRawImpl.readFromPersLayer(new PersLayerInputStream(persLayer));
		writeToPersLayer(rrdRawImpl);
		return ret;
	}

	private void writeToPersLayer(RrdRawImpl rrdRawImpl) {
		this.persLayer.seek(0);
		rrdRawImpl.writeToPersLayer(new PersLayerOutputStream(this.persLayer));
		this.persLayer.flip();
		this.persLayer.shrink();
	}

	@Override
	public synchronized void save() {
	}

	public synchronized void save(RrdPersistentLayer persLayer) {
		getRrdRawImpl().writeToPersLayer(new PersLayerOutputStream(persLayer));
	}

	@Override
	public synchronized void update(Date time, Double[] values) {
		RrdRawImpl impl = getRrdRawImpl();
		impl.update(time, values);
		writeToPersLayer(impl);
	}

	public MemoryPersistentLayer getMemoryPersistentLayer() {
		return this.persLayer;
	}

	@Override
	public void save(OutputStream ostream) {
		// TODO Auto-generated method stub
		
	}
}
