package org.krakenapps.ca;

import java.nio.ByteBuffer;

public class RDNSequence {
	// "CN=local, OU=RND, O=FutureSystems, L=Guro, ST=Seoul, C=KR"
	private String dn;
	private String value;

	private static final int DN_MAXIMUM_LENGTH = 131;

	public static void main(String[] args) {
		RDNSequence rdn = new RDNSequence("C", "US");
		RDNSequence rdn2 = new RDNSequence("O", "VeriSign, Inc.");
		RDNSequence rdn3 = new RDNSequence("OU", "Class 3 Public Primary Certification Authority");

		System.out.println("===================================");
		for (byte b : rdn.getBytes())
			System.out.printf("%02x ", b);
		System.out.println("\n\n===================================");
		for (byte b : rdn2.getBytes())
			System.out.printf("%02x ", b);
		System.out.println("\n\n===================================");
		for (byte b : rdn3.getBytes())
			System.out.printf("%02x ", b);
	}

	public RDNSequence(String dn, String value) {
		this.dn = dn;
		this.value = value;
	}

	public byte[] getBytes() {
		byte[] rdnBytes = getRDNBytes();
		byte[] b = new byte[2 + rdnBytes.length];
		b[0] = 0x31;
		b[1] = (byte) rdnBytes.length;
		int i = 2;
		for (byte b1 : rdnBytes) {
			b[i] = b1;
			i++;
		}
		return b;
	}

	private byte[] getRDNBytes() {
		ByteBuffer bb = ByteBuffer.allocate(DN_MAXIMUM_LENGTH);
		bb.put((byte) 0x30);

		int length = 7 + value.length();
		bb.put((byte) length);

		if (dn.equals("CN")) {
			putOid((byte) 0x03, bb);
			putData(bb);
		} else if (dn.equals("SN")) {
			putOid((byte) 0x04, bb);
			putData(bb);
		} else if (dn.equals("C")) {
			putOid((byte) 0x06, bb);
			putData(bb);
		} else if (dn.equals("L")) {
			putOid((byte) 0x07, bb);
			putData(bb);
		} else if (dn.equals("S") || dn.equals("ST")) {
			putOid((byte) 0x08, bb);
			putData(bb);
		} else if (dn.equals("STREET")) {
			putOid((byte) 0x09, bb);
			putData(bb);
		} else if (dn.equals("O")) {
			putOid((byte) 0x0a, bb);
			putData(bb);
		} else if (dn.equals("OU")) {
			putOid((byte) 0x0b, bb);
			putData(bb);
		} else if (dn.equals("T") || dn.equals("TITLE")) {
			putOid((byte) 0x0c, bb);
			putData(bb);
		}

		bb.rewind();
		byte[] b = new byte[length + 2];
		bb.get(b);
		return b;
	}

	private void putOid(byte b, ByteBuffer bb) {
		bb.put((byte) 0x06);
		bb.put((byte) 0x03);
		bb.put((byte) 0x55);
		bb.put((byte) 0x04);
		bb.put(b);
	}

	private void putData(ByteBuffer bb) {
		int length = value.length();
		bb.put((byte) 0x13);
		bb.put((byte) length);
		for (int i = 0; i < length; i++)
			bb.put((byte) value.codePointAt(i));
	}
}