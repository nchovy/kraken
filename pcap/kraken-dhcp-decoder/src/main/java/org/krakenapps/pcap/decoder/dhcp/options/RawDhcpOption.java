package org.krakenapps.pcap.decoder.dhcp.options;

public class RawDhcpOption implements DhcpOption {
	private byte type;
	private int length;
	private byte[] value;
	
	public RawDhcpOption(byte type, int length, byte[] value) {
		this.type = type;
		this.length = length;
		this.value = value;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("DHCP Option (t=%d,l=%d)", type, length);
	}
}
