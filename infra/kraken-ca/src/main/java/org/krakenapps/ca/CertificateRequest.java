/*
 * Copyright 2011 Future Systems
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

import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

/**
 * Certificate Sign Request
 * 
 * @author xeraph
 */
public class CertificateRequest {
	/**
	 * cert serial
	 */
	private BigInteger serial;

	/**
	 * issuer distinguished name
	 */
	private String issuerDn;

	/**
	 * issuer's private key for signing
	 */
	private PrivateKey issuerKey;

	/**
	 * public key and private key pair
	 */
	private KeyPair keyPair;

	/**
	 * key password for protect private key. used in pkcs12 export.
	 */
	private String keyPassword;

	/**
	 * subject distinguished name
	 */
	private String subjectDn;

	/**
	 * additional oids
	 */
	private Map<String, String> attrs;

	/**
	 * valid period starts from this date
	 */
	private Date notBefore;

	/**
	 * valid period ends at this date (expire)
	 */
	private Date notAfter;

	/**
	 * signature algorithm. support "MD5withRSA", "MD5withRSA", "SHA1withRSA",
	 * "SHA224withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA"
	 */
	private String signatureAlgorithm;

	/**
	 * CRL distribution point URL will be set at
	 * CertificateAuthority.issueCertificate(). You cannot set this parameter
	 * directly.
	 */
	private URL crlUrl;

	public static CertificateRequest createSelfSignedCertRequest(KeyPair keyPair, String keyPassword, String dn, Date notBefore,
			Date notAfter, String signatureAlgorithm) {
		CertificateRequest req = new CertificateRequest();
		req.setSerial(new BigInteger("1"));
		req.setIssuerDn(dn);
		req.setSubjectDn(dn);
		req.setNotBefore(notBefore);
		req.setNotAfter(notAfter);
		req.setKeyPair(keyPair);
		req.setKeyPassword(keyPassword);
		req.setSignatureAlgorithm(signatureAlgorithm);
		return req;
	}

	public BigInteger getSerial() {
		return serial;
	}

	public void setSerial(BigInteger serial) {
		this.serial = serial;
	}

	public String getIssuerDn() {
		return issuerDn;
	}

	public void setIssuerDn(String issuerDn) {
		this.issuerDn = issuerDn;
	}

	public PrivateKey getIssuerKey() {
		return issuerKey;
	}

	public void setIssuerKey(PrivateKey issuerKey) {
		this.issuerKey = issuerKey;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public String getKeyPassword() {
		return keyPassword;
	}

	public void setKeyPassword(String keyPassword) {
		this.keyPassword = keyPassword;
	}

	public String getSubjectDn() {
		return subjectDn;
	}

	public void setSubjectDn(String subjectDn) {
		this.subjectDn = subjectDn;
	}

	public Map<String, String> getAttributes() {
		return attrs;
	}

	public void setAttributes(Map<String, String> attrs) {
		this.attrs = attrs;
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

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public URL getCrlUrl() {
		return crlUrl;
	}

	public void setCrlUrl(URL crlUrl) {
		this.crlUrl = crlUrl;
	}

}
