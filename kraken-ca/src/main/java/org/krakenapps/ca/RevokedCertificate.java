package org.krakenapps.ca;

import java.math.BigInteger;

public class RevokedCertificate {
	private static final int REVOKED_CERTIFICATE_LENGTH = 35;
	private BigInteger serialNumber;
	private String revocationDate;

	public static void main(String[] args) {
		RevokedCertificate rc = new RevokedCertificate(new BigInteger("2cd24b62c497a417cd6ea3c89c7a2dc8", 16), "04-04-01 17:56:15");
		RevokedCertificate rc2 = new RevokedCertificate(new BigInteger("3a45de56cb02cddcdc4e7763221bd4d5", 16), "01-05-08 19:22:34");
		rc.getBytes();
		rc2.getBytes();
	}

	public RevokedCertificate(BigInteger serialNumber, String revocationDate) {
		this.revocationDate = revocationDate;
		this.serialNumber = serialNumber;
	}

	public String getRevocationDate() {
		return revocationDate;
	}

	public BigInteger getSerialNumber() {
		return serialNumber;
	}

	public byte[] getBytes() {
		byte[] b = new byte[REVOKED_CERTIFICATE_LENGTH];
		b[0] = 0x30;
		b[1] = 0x21; // fixed length of revoked certificate data
		b[2] = 0x02;
		b[3] = 0x10; // fixed length of serial number(16 bytes)

		int i = 4;
		for (byte b1 : serialNumber.toByteArray()) {
			b[i] = b1;
			i++;
		}

		revocationDate = revocationDate.replaceAll("[-: ]*", "");
		b[i] = 0x17;
		b[i + 1] = 0x0d; // fixed length of UTC time(13 bytes)
		i = i + 2;
		for (byte b1 : revocationDate.getBytes()) {
			b[i] = b1;
			i++;
		}
		b[i] = 0x5a; // suffix of UTC('Z')

		return b;
	}
}