package org.krakenapps.linux.api;

import org.krakenapps.linux.api.RoutingEntry.Flag;

public class RoutingEntryV6 {
	private String destination;
	private String nextHop;
	private int mask;
	private Flag flags;
	private int metric;
	private int ref;
	private int use;
	private String iface;
	
	public RoutingEntryV6(String destination, String nextHop, int mask, Flag flags, int metric, int ref, int use, String iface) {
		this.destination = destination;
		this.nextHop = nextHop;
		this.mask = mask;
		this.flags = flags;
		this.metric = metric;
		this.ref = ref;
		this.use = use;
		this.iface = iface;
	}
	
	public String getDestination() {
		return destination;
	}

	public String getNextHop() {
		return nextHop;
	}

	public int getMask() {
		return mask;
	}
	
	public Flag getFlags() {
		return flags;
	}

	public int getMetric() {
		return metric;
	}

	public int getRef() {
		return ref;
	}

	public int getUse() {
		return use;
	}

	public String getIface() {
		return iface;
	}
}