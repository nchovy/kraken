package org.krakenapps.crl;

import java.util.Date;

import org.krakenapps.ber.BERObject;
import org.krakenapps.pcap.util.ByteArrayParser;

public class RevokedCertificate {
	private int serialNumber;
	private Date revocationDate;
	private BERObject crlExtensions;
	
	public RevokedCertificate(byte[] b, Date revocationDate, BERObject crlExtensions) {
		this.serialNumber = ByteArrayParser.getInt(b, 0);
		this.revocationDate = revocationDate;
		this.crlExtensions = crlExtensions;
	}

	public int getSerialNumber() {
		return serialNumber;
	}
	
	public Date getRevocationDate() {
		return revocationDate;
	}
	
	public BERObject getCrlExtensions() {
		return crlExtensions;
	}
}