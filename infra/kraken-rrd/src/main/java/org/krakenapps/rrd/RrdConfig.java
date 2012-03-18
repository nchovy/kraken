package org.krakenapps.rrd;

import java.util.ArrayList;
import java.util.Date;

public class RrdConfig {

	/* member fields */
	int version;
	long stepSeconds;
	long startTime;

	ArrayList<DataSourceConfig> dataSources;
	ArrayList<ArchiveConfig> archives;

	public RrdConfig(Date startTime, long stepSeconds) {
		this.version = 3;
		this.startTime = startTime.getTime() / 1000;
		this.stepSeconds = stepSeconds;
		this.dataSources = new ArrayList<DataSourceConfig>();
		this.archives = new ArrayList<ArchiveConfig>();
	}

	public long getStep() {
		return stepSeconds;
	}

	public void setStep(long step) {
		this.stepSeconds = step;
	}

	public void addDataSource(String name, DataSource.Type type, long heartbeat, double min, double max) {
		dataSources.add(new DataSourceConfig(name, type, heartbeat, min, max));
	}

	public void addRoundRobinArchive(ConsolidateFunc func, double xff, int steps, int size) {
		archives.add(new ArchiveConfig(func, xff, steps, size));
	}
	
	public long getStartTimeLong() {
		return startTime;
	}

	public Date getStartTimeDate() {
		return new Date(startTime * 1000);
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime.getTime() / 1000;
	}
}