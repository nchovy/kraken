package org.krakenapps.fluxmon.impl;

class FluxCountryStat implements Comparable<FluxCountryStat> {
	private String name;
	private int count;

	public FluxCountryStat(String name, int count) {
		this.name = name;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public int getCount() {
		return count;
	}

	@Override
	public int compareTo(FluxCountryStat o) {
		return o.count - count; // descending order
	}

	@Override
	public String toString() {
		return name + ": " + count;
	}
}
