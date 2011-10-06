package org.krakenapps.ca;

import java.math.BigInteger;
import java.util.Date;

public class RevocationList {
	private Date revocationDate;
	private BigInteger serialNumber;

	public RevocationList(Date revocationDate, BigInteger serialNumber) {
		this.revocationDate = revocationDate;
		this.serialNumber = serialNumber;
	}

	public Date getRevocationDate() {
		return revocationDate;
	}

	public BigInteger getSerialNumber() {
		return serialNumber;
	}
}