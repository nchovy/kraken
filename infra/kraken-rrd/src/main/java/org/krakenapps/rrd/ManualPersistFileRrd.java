package org.krakenapps.rrd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class ManualPersistFileRrd implements Rrd {
	private RrdRawImpl impl = new RrdRawImpl();
	private RrdPersistentLayer persLayer = null;
	boolean isInitialized = false;
	
	public ManualPersistFileRrd()
	{
	}

	public void init(RrdPersistentLayer persLayer) {
		if (persLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = persLayer;
		if (!load())
			throw new BrokenPersLayerException();
		isInitialized = true;
	}

	public void init(RrdConfig config, RrdPersistentLayer persLayer) {
		if (persLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = persLayer;
		impl.init(config);
		isInitialized = true;
	}

	@Override
	public void dump(OutputStream stream) {
		checkInitialized();
		impl.dump(stream);
	}

	private void checkInitialized() {
		if (!isInitialized)
			throw new UninitializedException();
	}

	@Override
	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		checkInitialized();
		return impl.fetch(f, start, end, resolution);
	}

	@Override
	public void update(Date time, Double[] values) {
		checkInitialized();
		impl.update(time, values);
	}

	public boolean load(RrdPersistentLayer persLayer) {
		if (persLayer == null)
			throw new NullPersistentLayerException();

		return impl.readFromPersLayer(new PersLayerInputStream(persLayer));
	}

	public void save(RrdPersistentLayer persLayer) {
		if (persLayer == null)
			throw new NullPersistentLayerException();

		impl.writeToPersLayer(new PersLayerOutputStream(persLayer));
	}

	public boolean load() {
		checkInitialized();
		boolean ret = false;
		try {
			this.persLayer.reopen();
			ret = impl.readFromPersLayer(new PersLayerInputStream(persLayer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void save() {
		checkInitialized();
		try {
			this.persLayer.reopen();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
