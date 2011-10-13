package org.krakenapps.ca.crl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class Issuer {
	private List<RDNSequence> rdnSet;

	public Issuer() {
		rdnSet = new ArrayList<RDNSequence>();
	}

	public void addRDNSequence(RDNSequence sequence) {
		rdnSet.add(sequence);
	}

	public void removeRDNSequence(RDNSequence sequence) {
		rdnSet.remove(sequence);
	}

	public byte[] getBytes() {
		int length = length();
		ByteBuffer bb = ByteBuffer.allocate(2 + length);
		bb.put((byte) 0x30);
		bb.put(BERUtil.getLengthBytes(length));
		for (RDNSequence sequence : rdnSet) {
			bb.put(sequence.getBytes());
		}
		bb.flip();
		byte[] b = new byte[2 + length];
		bb.get(b);
		return b;
	}

	private int length() {
		int length = 0;
		for (RDNSequence sequence : rdnSet) {
			length += sequence.getBytes().length;
		}
		return length;
	}
}