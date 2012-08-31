package org.krakenapps.rrd;

import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.rrd.exception.NullPersistentLayerException;
import org.krakenapps.rrd.impl.RrdRaw;
import org.krakenapps.rrd.io.PersistentLayer;

public class CompactRrd implements Rrd {
	private WeakReference<RrdRaw> rawref = new WeakReference<RrdRaw>(null);
	private PersistentLayer persLayer;

	public CompactRrd(PersistentLayer persLayer) {
		this(persLayer, null);
	}

	public CompactRrd(PersistentLayer persLayer, RrdConfig config) {
		if (persLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = persLayer;

		if (config != null)
			new RrdRaw(config).write(persLayer);
	}

	@Override
	public List<DataSourceConfig> getDataSources() {
		return getRrdRaw().getDataSourceConfigs();
	}

	@Override
	public List<ArchiveConfig> getArchives() {
		return getRrdRaw().getArchiveConfigs();
	}

	@Override
	public void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max) {
		getRrdRaw().addDataSource(new DataSourceConfig(name, type, heartbeat, min, max));
	}

	@Override
	public void removeDataSource(String name) {
		getRrdRaw().removeDataSource(name);
	}

	@Override
	public void update(Date time, Map<String, Double> values) {
		RrdRaw raw = getRrdRaw();
		raw.update(time, values);
		raw.write(persLayer);
	}

	@Override
	public void update(Date time, Double[] values) {
		RrdRaw raw = getRrdRaw();
		raw.update(time, values);
		raw.write(persLayer);
	}

	@Override
	public int length() {
		return getRrdRaw().length();
	}

	@Override
	public void save() {
	}

	@Override
	public void save(PersistentLayer persLayer) {
		getRrdRaw().write(persLayer);
	}

	@Override
	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		return getRrdRaw().fetch(f, start, end, resolution);
	}

	@Override
	public void dump(OutputStream os) {
		getRrdRaw().dump(os);
	}

	private RrdRaw getRrdRaw() {
		RrdRaw raw = rawref.get();
		if (raw == null) {
			raw = new RrdRaw(persLayer);
			rawref = new WeakReference<RrdRaw>(raw);
		}
		return raw;
	}
}
