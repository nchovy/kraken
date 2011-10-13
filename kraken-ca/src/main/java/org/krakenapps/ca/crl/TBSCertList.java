package org.krakenapps.ca.crl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;


public class TBSCertList {
	private static final int MAXIMUM_CAPACITY = 10000;
	private X509Certificate cert;
	private RevokedCertificates rcs;

	public TBSCertList(X509Certificate cert, RevokedCertificates rcList) {
		this.cert = cert;
		this.rcs = rcList;
	}

	public byte[] getBytes() throws FileNotFoundException, IOException {
		ByteBuffer bb = ByteBuffer.allocate(MAXIMUM_CAPACITY);
		byte[] sigBytes = getSignature();
		byte[] issuerBytes = getIssuer();
		byte[] updateBytes = getUpdateDate();

		int totalLength = sigBytes.length + issuerBytes.length + updateBytes.length + rcs.getLength();
		byte[] lengthBytes = BERUtil.getLengthBytes(totalLength);

		bb.put((byte) 0x30);
		bb.put(lengthBytes);
		bb.put(sigBytes);
		bb.put(issuerBytes);
		bb.put(updateBytes);
		bb.put(rcs.getBytes());
		
		int pos = bb.position();
		byte[] b = new byte[pos];
		bb.flip();
		bb.get(b);
		return b;
	}

	private byte[] getSignature() {
		byte[] oid = BERUtil.encodingOID(cert.getSigAlgOID());
		int oidLength = oid.length;
		// 06 + length byte of OID + OID + 05 00(NULL)
		int totalLength = 4 + oidLength;
		byte[] sigBytes = new byte[2 + totalLength];

		sigBytes[0] = 0x30;
		sigBytes[1] = (byte) totalLength;
		sigBytes[2] = 0x06;
		sigBytes[3] = (byte) oidLength;
		for (int i = 0; i < oidLength; i++) {
			sigBytes[4 + i] = oid[i];
		}
		sigBytes[sigBytes.length - 2] = 0x05;
		sigBytes[sigBytes.length - 1] = 0x00;
		return sigBytes;
	}

	private byte[] getIssuer() {
		RDNSequence rdn = new RDNSequence("C", "US");
		RDNSequence rdn2 = new RDNSequence("O", "VeriSign, Inc.");
		RDNSequence rdn3 = new RDNSequence("OU", "Class 1 Public Primary Certification Authority");

		Issuer sequences = new Issuer();
		sequences.addRDNSequence(rdn);
		sequences.addRDNSequence(rdn2);
		sequences.addRDNSequence(rdn3);

		return sequences.getBytes();
	}

	private byte[] getUpdateDate() throws FileNotFoundException, IOException {
		String thisUpdate = "11-09-20 00:00:00Z";
		String nextUpdate = "11-12-30 23:59:59Z";

		// File dateFile = new File(System.getProperty("kraken.data.dir"),
		// "kraken-ca/CA/date");
		// BufferedReader br = new BufferedReader(new InputStreamReader(new
		// FileInputStream(dateFile)));
		// String thisUpdate = br.readLine();
		// String nextUpdate = br.readLine();

		thisUpdate = thisUpdate.replaceAll("[-: ]*", "");
		nextUpdate = nextUpdate.replaceAll("[-: ]*", "");

		byte[] thisUpdateBytes = thisUpdate.getBytes();
		byte[] nextUpdateBytes = nextUpdate.getBytes();
		byte[] b = new byte[4 + thisUpdateBytes.length + nextUpdateBytes.length];

		b[0] = 0x17;
		b[1] = 0x0d;
		for (int i = 0; i < thisUpdateBytes.length; i++) {
			b[i + 2] = thisUpdateBytes[i];
		}

		int j = 2 + thisUpdateBytes.length;
		b[j] = 0x17;
		b[j + 1] = 0x0d;
		j += 2;
		for (int i = 0; i < nextUpdateBytes.length; i++) {
			b[j + i] = nextUpdateBytes[i];
		}

		return b;
	}
}