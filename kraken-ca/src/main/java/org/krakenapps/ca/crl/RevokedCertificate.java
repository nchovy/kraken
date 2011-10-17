package org.krakenapps.ca.crl;

import java.math.BigInteger;

import java.text.ParseException;
import java.util.Date;

public class RevokedCertificate {
	private BigInteger serialNumber;
	private Date revocationDate;
	private int reasonCode;
	
	public RevokedCertificate(BigInteger serialNumber, Date revocationDate, int reasonCode) {
		this.revocationDate = revocationDate;
		this.serialNumber = serialNumber;
		this.reasonCode = reasonCode;
	}

	
	public Date getRevocationDate() throws ParseException {
		return revocationDate;
	}
	
	public BigInteger getSerialNumber() {
		return serialNumber;
	}
	
	public int getReasonCode() { 
		return reasonCode;
	}
}