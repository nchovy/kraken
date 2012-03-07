package org.krakenapps.pcap.decoder.netbios.rr;

public class NodeName {
	private String name;
	private int flags; // 2bytes, this use only NBSTAT

	public NodeName(String name, int flags) {
		this.name = name;
		this.flags = flags;
	}
	public String getName() {
		return name;
	}
	public int getFlags() {
		return flags;
	}
}
