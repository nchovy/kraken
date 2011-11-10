package org.krakenapps.ca;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

/**
 * Certificate metadata that contains certificate binary
 * 
 * @author xeraph
 */
public class CertificateMetadata {
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
				+ notAfter;
	}
}
