package org.krakenapps.dnet;

public class Address {
	public enum Type {
		None, Ethernet, IP, IPv6
	};

	private Type type;
	private int bits;
	private byte[] data;

	public Address(Type type, int bits, byte[] data) {
		this.type = type;
		this.bits = bits;
		this.data = data;
	}

	public Address(String type, int bits, byte[] data) {
		this.type = Type.valueOf(type);
		this.bits = bits;
		this.data = data;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getBits() {
		return bits;
	}

	public void setBits(int bits) {
		this.bits = bits;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
