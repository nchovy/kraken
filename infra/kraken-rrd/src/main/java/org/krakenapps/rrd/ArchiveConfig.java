package org.krakenapps.rrd;

public class ArchiveConfig {
	private ConsolidateFunc cf;
	private double xff;
	private int steps;
	private int rowCapacity;

	public ArchiveConfig(ConsolidateFunc cf, double xff, int steps, int size) {
		this.cf = cf;
		this.xff = xff;
		this.steps = steps;
		this.rowCapacity = size;
	}

	public ConsolidateFunc getCf() {
		return cf;
	}

	public double getXff() {
		return xff;
	}

	public int getSteps() {
		return steps;
	}

	public int getRowCapacity() {
		return rowCapacity;
	}
}