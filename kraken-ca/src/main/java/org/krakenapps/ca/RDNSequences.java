package org.krakenapps.ca;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RDNSequences {
	private List<RDNSequence> rdnSet;

	public static void main(String[] args) {
		RDNSequence rdn = new RDNSequence("C", "US");
		RDNSequence rdn2 = new RDNSequence("O", "VeriSign, Inc.");
		RDNSequence rdn3 = new RDNSequence("OU", "Class 3 Public Primary Certification Authority");

		RDNSequences sequences = new RDNSequences();
		sequences.addRDNSequence(rdn);
		sequences.addRDNSequence(rdn2);
		sequences.addRDNSequence(rdn3);

		int i = 0;
		for (byte b : sequences.getBytes()) {
			if (i == 8) {
				System.out.println();
				i = 0;
			}
			System.out.printf("%02x ", b);
			i++;
		}
	}

	public RDNSequences() {
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