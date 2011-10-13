package org.krakenapps.ca.crl;

import java.math.BigInteger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RevokedCertificate {
	private BigInteger serialNumber;
	private String revocationDate;
	private int reasonCode;
	
	public RevokedCertificate(BigInteger serialNumber, String revocationDate, int reasonCode) {
		this.revocationDate = revocationDate;
		this.serialNumber = serialNumber;
		this.reasonCode = reasonCode;
	}

	public String getRevocationDateString() {
		return revocationDate;
	}
	
	public Date getRevocationDate() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");
		return sdf.parse(revocationDate);
	}
	
	public BigInteger getSerialNumber() {
		return serialNumber;
	}
	
	public int getReasonCode() { 
		return reasonCode;
	}

	public byte[] getBytes() {
		byte[] sb = serialNumber.toByteArray();
		revocationDate = revocationDate.replaceAll("[-: ]*", "");
		byte[] rdb = revocationDate.getBytes();
		byte[] b = new byte[7 + sb.length + rdb.length];

		b[0] = 0x30;
		b[1] = (byte) (5 + sb.length + rdb.length);
		b[2] = 0x02;
		b[3] = (byte) sb.length;

		int i = 4;
		for (byte b1 : sb) {
			b[i] = b1;
			i++;
		}

		b[i] = 0x17;
		b[i + 1] = 0x0d; // fixed length of UTC time(13 bytes)
		i = i + 2;
		for (byte b1 : rdb) {
			b[i] = b1;
			i++;
		}
		b[i] = 0x5a; // suffix of UTC('Z')

		return b;
	}
}