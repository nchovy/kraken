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

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

import org.krakenapps.api.FieldOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Certificate metadata that contains certificate binary
 * 
 * @author xeraph
 */
public class CertificateMetadata {

	@FieldOption(skip = true)
	private final Logger logger = LoggerFactory.getLogger(CertificateMetadata.class.getName());
	/**
	 * "jks" or "pkcs12" string
	 */
	private String type;

	/**
	 * big integer serial is represented as string
	 */
	private String serial;

	/**
	 * subject distinguished name
	 */
	private String subjectDn;

	/**
	 * valid period start from
	 */
	private Date notBefore;

	/**
	 * valid period expiry date
	 */
	private Date notAfter;

	/**
	 * jks or pkcs12 (.pfx) binary (related to type)
	 */
	private byte[] binary;

	/**
	 * issued date
	 */
	private Date issuedDate = new Date();

	public Date getIssuedDate() {
		return issuedDate;
	}

	public void setIssuedDate(Date issuedDate) {
		this.issuedDate = issuedDate;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getSubjectDn() {
		return subjectDn;
	}

	public void setSubjectDn(String subjectDn) {
		this.subjectDn = subjectDn;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public byte[] getBinary() {
		return binary;
	}

	public void setBinary(byte[] binary) {
		this.binary = binary;
	}

	public X509Certificate getCertificate() {
		// jks don't need password, but pkcs12 need password to decrypt file
		return getCertificate(null);
	}

	public X509Certificate getCertificate(String password) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(binary);
			KeyStore store = null;
			if (type.equals("pkcs12"))
				store = KeyStore.getInstance(type.toUpperCase(), "BC");
			else
				store = KeyStore.getInstance(type.toUpperCase());

			logger.debug("kraken ca: request get certificate, type [{}], password [{}], binary [{}]", new Object[] { type,
					password, binary });
			store.load(is, password == null ? null : password.toCharArray());
			return (X509Certificate) store.getCertificate("public");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public RSAPrivateKey getPrivateKey(String password) {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(binary);
			KeyStore store = KeyStore.getInstance(type.toUpperCase());
			store.load(is, password.toCharArray());
			return (RSAPrivateKey) store.getKey("private", password.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return "type=" + type + ", serial=" + serial + ", subject=" + subjectDn + ", not_before=" + notBefore + ", not_after="
				+ notAfter + ", issued_date=" + issuedDate;
	}
}
