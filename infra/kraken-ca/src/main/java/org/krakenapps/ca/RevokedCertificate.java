package org.krakenapps.ca;

import java.text.ParseException;
import java.util.Date;

public class RevokedCertificate {
	/**
	 * certificate serial (big integer as string)
	 */
	private String serial;

	/**
	 * revocation date
	 */
	private Date date;

	/**
	 * revocation reason
	 */
	private RevocationReason reason;

	public RevokedCertificate() {
	}

	public RevokedCertificate(String serial, Date date, RevocationReason reason) {
		this.date = date;
		this.serial = serial;
		this.reason = reason;
	}

	public Date getRevocationDate() throws ParseException {
		return date;
	}

	public String getSerial() {
		return serial;
	}

	public RevocationReason getReason() {
		return reason;
	}

	@Override
	public String toString() {
		return "serial=" + serial + ", date=" + date + ", reason=" + reason;
	}

}