package org.krakenapps.rrd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RrdConfig {
	private int version;
	private long stepSeconds;
	private long startTime;

	private List<DataSourceConfig> dataSources;
	private List<ArchiveConfig> archives;

	public RrdConfig(Date startTime, long stepSeconds) {
		this.version = 3;
		this.startTime = startTime.getTime();
		this.stepSeconds = stepSeconds;
		this.dataSources = new ArrayList<DataSourceConfig>();
		this.archives = new ArrayList<ArchiveConfig>();
	}

	public int getVersion() {
		return version;
	}

	public long getStep() {
		return stepSeconds;
	}

	public long getStartTime() {
		return startTime;
	}

	public List<DataSourceConfig> getDataSources() {
		return dataSources;
	}

	public void addDataSource(String name, DataSourceType type, long heartbeat, double min, double max) {
		dataSources.add(new DataSourceConfig(name, type, heartbeat, min, max));
	}

	public List<ArchiveConfig> getArchives() {
		return archives;
	}

	public void addArchive(ConsolidateFunc func, double xff, int steps, int size) {
		archives.add(new ArchiveConfig(func, xff, steps, size));
	}
}