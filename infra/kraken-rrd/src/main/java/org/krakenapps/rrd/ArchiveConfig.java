package org.krakenapps.rrd;
public class ArchiveConfig {
	private ConsolidateFunc cf;
	public double xff;
	public int steps;
	public int rowCapacity;

	public ArchiveConfig(ConsolidateFunc cf, double xff, int steps, int size) {
		this.setCf(cf);
		this.xff = xff;
		this.steps = steps;
		this.rowCapacity = size;
	}

	public void setCf(ConsolidateFunc cf) {
		this.cf = cf;
	}

	public ConsolidateFunc getCf() {
		return cf;
	}
}