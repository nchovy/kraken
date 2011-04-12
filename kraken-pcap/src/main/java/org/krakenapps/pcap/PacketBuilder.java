package org.krakenapps.pcap;

public interface PacketBuilder {
	Injectable build();

	Object getDefault(String name);
}
