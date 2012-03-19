package org.krakenapps.rrd;
class DataSourceConfig {
	private String name;
	private DataSource.Type type;
	private long minimalHeartbeat;
	private double min;
	private double max;

	public DataSourceConfig(String name, DataSource.Type type, long minimalHeartbeat, double min, double max) {
		this.name = name;
		this.type = type;
		this.minimalHeartbeat = minimalHeartbeat;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return name;
	}

	public void setType(DataSource.Type type) {
		this.type = type;
	}

	public DataSource.Type getType() {
		return type;
	}
	
	public long getMinimalHeartbeat()
	{
		return minimalHeartbeat;
	}
	
	public double getMin()
	{
		return this.min;
	}
	
	public double getMax()
	{
		return this.max;
	}
}