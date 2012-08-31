package org.krakenapps.rrd;

public class DataSourceConfig {
	private String name;
	private DataSourceType type;
	private long minimalHeartbeat;
	private double min;
	private double max;

	public DataSourceConfig(String name, DataSourceType type, long minimalHeartbeat, double min, double max) {
		this.name = name;
		this.type = type;
		this.minimalHeartbeat = minimalHeartbeat;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return name;
	}

	public void setType(DataSourceType type) {
		this.type = type;
	}

	public DataSourceType getType() {
		return type;
	}

	public long getMinimalHeartbeat() {
		return minimalHeartbeat;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
}