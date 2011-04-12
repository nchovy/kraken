package org.krakenapps.pcap.decoder.dhcp.options;

import org.krakenapps.pcap.util.ByteArrayParser;

public class MaxDhcpMessageSizeOption extends RawDhcpOption {
	public MaxDhcpMessageSizeOption(byte type, int length, byte[] value) {
		super(type, length, value);
	}

	public int getMaxSize() {
		byte[] b = getValue();
		short s = ByteArrayParser.getShort(b, 0);
		return (int) s & 0xFFFF;
	}

	@Override
	public String toString() {
		return "Maximum DHCP Message Size = " + getMaxSize();
	}
}
