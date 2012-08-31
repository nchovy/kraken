package org.krakenapps.rrd;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.rrd.io.PersistentLayer;

public interface Rrd {
	List<DataSourceConfig> getDataSources();

	List<ArchiveConfig> getArchives();

	void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max);

	void removeDataSource(String name);

	void update(Date time, Map<String, Double> values);

	void update(Date time, Double[] values);

	int length();

	void save();

	void save(PersistentLayer persLayer);

	FetchResult fetch(ConsolidateFunc f, Date start, Date end, long resolution);

	void dump(OutputStream os);
}
