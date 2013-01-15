/*
 * Copyright 2012 Future Systems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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