/*
 * Copyright 2010 NCHOVY
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
import java.util.Collection;
import java.util.List;

import org.krakenapps.confdb.Predicate;

public interface CertificateAuthority {
	/**
	 * @return the authority name
	 */
	String getName();

	/**
	 * @return the last serial.
	 */
	BigInteger getLastSerial();

	/**
	 * Increase internal serial counter and return new one
	 * 
	 * @return the next serial for issuing new certificate
	 */
	BigInteger getNextSerial();

	/**
	 * Get root X.509 certificate and metadata
	 * 
	 * @return the certificate metadata
	 */
	CertificateMetadata getRootCertificate();

	/**
	 * Get key password for private key of root certificate. Key password is
	 * required for CA signing (e.g. CRL sign)
	 * 
	 * @return the root key password
	 */
	String getRootKeyPassword();

	/**
	 * Certificate's CRL Distribution URL will be CrlDistPoint followed by
	 * /ca/crl/[authority]?serial=[serial]. For example,
	 * http://localhost/ca/crl/test?serial=2
	 * 
	 * @return the CRL distribution base URL
	 */
	URL getCrlDistPoint();

	/**
	 * Set CRL distribution base URL. Certificate's CRL Distribution URL will be
	 * CrlDistPoint followed by /ca/crl/[authority]?serial=[serial]. For
	 * example, http://localhost/ca/crl/test?serial=2
	 */
	void setCrlDistPoint(URL url);

	/**
	 * Return all issued certificates including revoked ones
	 * 
	 * @return the all issued certificates
	 */
	Collection<CertificateMetadata> getCertificates();

	Collection<CertificateMetadata> getCertificates(Predicate pred);

	CertificateMetadataIterator getCertificateIterator();

	CertificateMetadataIterator getCertificateIterator(Predicate pred);

	/**
	 * Find a certificate by given search condition
	 * 
	 * @param field
	 *            the search field name. "subject_dn" or "serial"
	 * @param value
	 *            the search value
	 * @return the certificate
	 */
	CertificateMetadata findCertificate(String field, String value);

	/**
	 * Issue a new certificate
	 * 
	 * @param req
	 *            the certificate sign request
	 * @return the issued certificate, private key and metadata
	 * @throws Exception
	 *             when any cryptographic error is raised
	 */
	CertificateMetadata issueCertificate(CertificateRequest req) throws Exception;

	/**
	 * import a new certificate
	 * 
	 * @param cm
	 *            the certificate metadata
	 */
	void importCertificate(CertificateMetadata cm);

	/**
	 * @return all revoked certificate list
	 */
	List<RevokedCertificate> getRevokedCertificates();

	/**
	 * @param serial
	 *            the revoked certificate serial
	 * 
	 * @return revoked certificate
	 */
	RevokedCertificate getRevokedCertificate(String serial);

	RevokedCertificateIterator getRevokedCertificateIterator();

	RevokedCertificateIterator getRevokedCertificateIterator(Predicate pred);

	/**
	 * Revoke a certificate
	 * 
	 * @param cm
	 *            the certificate metadata. use findCertificate() to get
	 *            certificate metadata.
	 */
	void revoke(CertificateMetadata cm);

	/**
	 * Revoke a certificate with reason
	 * 
	 * @param cm
	 *            the certificate metadata. use findCertificate() to get
	 *            certificate metadata.
	 * @param reason
	 *            the revocation reason
	 */
	void revoke(CertificateMetadata cm, RevocationReason reason);

	void addListener(CertEventListener listener);

	void removeListener(CertEventListener listener);
}
