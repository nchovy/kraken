package org.krakenapps.ca.crl;

import java.util.ArrayList;
import java.util.List;


public class RevokedCertificates {
	private List<RevokedCertificate> l;
	private int rcsLen;

	public RevokedCertificates() {
		l = new ArrayList<RevokedCertificate>();
		rcsLen = 0;
	}

	public void addRevokedCertificate(RevokedCertificate rc) {
		l.add(rc);
		rcsLen += rc.getBytes().length;
	}

	public void removeRevokedCertificate(RevokedCertificate rc) {
		l.remove(rc);
		rcsLen -= rc.getBytes().length;
	}
		

	public byte[] getBytes() {
		byte[] lengthBytes = BERUtil.getLengthBytes(rcsLen);
		byte[] b = new byte[1 + lengthBytes.length + rcsLen];
		b[0] = 0x30;
		for (int i = 0; i < lengthBytes.length; i++)
			b[i + 1] = lengthBytes[i];

		int j = lengthBytes.length + 1;
		for (int i = 0; i < l.size(); i++) {
			byte[] b1 = l.get(i).getBytes();
			
			for (byte b2 : b1) {
				b[j] = b2;
				j++;
			}
		}
		
		return b;
	}

	public int getLength() {
		byte[] lengthBytes = BERUtil.getLengthBytes(rcsLen);
		return 1 + lengthBytes.length + rcsLen; // 1 is 0x30
	}
}