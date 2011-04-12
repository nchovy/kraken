package org.krakenapps.pcap.decoder.dhcp.options;

public interface DhcpOption {
	byte getType();

	int getLength();

	byte[] getValue();
}
