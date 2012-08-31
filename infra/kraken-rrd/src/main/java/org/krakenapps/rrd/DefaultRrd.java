package org.krakenapps.rrd;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.rrd.exception.NullPersistentLayerException;
import org.krakenapps.rrd.impl.RrdRaw;
import org.krakenapps.rrd.io.PersistentLayer;

public class DefaultRrd implements Rrd {
	private RrdRaw raw;
	private PersistentLayer persLayer;
	private boolean autoSave;

	public DefaultRrd(PersistentLayer persLayer) {
		this(persLayer, null);
	}

	public DefaultRrd(PersistentLayer persLayer, boolean autoSave) {
		this(persLayer, null, autoSave);
	}

	public DefaultRrd(PersistentLayer persLayer, RrdConfig config) {
		this(persLayer, config, true);
	}

	public DefaultRrd(PersistentLayer persLayer, RrdConfig config, boolean autoSave) {
		if (persLayer == null)
			throw new NullPersistentLayerException();
		this.persLayer = persLayer;
		this.autoSave = autoSave;

		raw = (config != null) ? (new RrdRaw(config)) : (new RrdRaw(persLayer));
	}

	@Override
	public List<DataSourceConfig> getDataSources() {
		return raw.getDataSourceConfigs();
	}

	@Override
	public List<ArchiveConfig> getArchives() {
		return raw.getArchiveConfigs();
	}

	@Override
	public void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max) {
		raw.addDataSource(new DataSourceConfig(name, type, heartbeat, min, max));
	}

	@Override
	public void removeDataSource(String name) {
		raw.removeDataSource(name);
	}

	@Override
	public void update(Date time, Map<String, Double> values) {
		raw.update(time, values);
		if (autoSave)
			save();
	}

	@Override
	public void update(Date time, Double[] values) {
		raw.update(time, values);
		if (autoSave)
			save();
	}

	@Override
	public int length() {
		return raw.length();
	}

	@Override
	public void save() {
		raw.write(persLayer);
	}

	@Override
	public void save(PersistentLayer persLayer) {
		raw.write(persLayer);
	}

	@Override
	public FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution) {
		return raw.fetch(f, start, end, resolution);
	}

	@Override
	public void dump(OutputStream os) {
		raw.dump(os);
	}
}
