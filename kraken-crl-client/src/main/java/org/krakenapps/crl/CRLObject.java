package org.krakenapps.crl;

public class CRLObject {
	private byte type;
	private int length;
	private byte[] data;
	
	public CRLObject(byte type, int length, byte[] data) {
		this.type = type;
		this.length = length;
		this.data = data;
	}

	public byte getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public byte[] getData() {
		return data;
	}	
}