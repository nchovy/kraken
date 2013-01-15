/*
 * Copyright 2013 Future Systems, Inc.
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

public class MockCertificateAuthority implements CertificateAuthority {
	private String name;
	private String lastSerial;
	private String rootKeyPassword;
	private CertificateMetadata rootCertificate;
	private URL crlDistPoint;
	private Collection<CertificateMetadata> certs;
	private List<RevokedCertificate> revoked;

	public MockCertificateAuthority(String name, String lastSerial, String rootKeyPassword, CertificateMetadata rootCertificate,
			URL crlDistPoint, Collection<CertificateMetadata> certs, List<RevokedCertificate> revoked) {
		this.name = name;
		this.lastSerial = lastSerial;
		this.rootKeyPassword = rootKeyPassword;
		this.rootCertificate = rootCertificate;
		this.crlDistPoint = crlDistPoint;
		this.certs = certs;
		this.revoked = revoked;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BigInteger getLastSerial() {
		return new BigInteger(lastSerial);
	}

	@Override
	public BigInteger getNextSerial() {
		return null;
	}

	@Override
	public CertificateMetadata getRootCertificate() {
		return rootCertificate;
	}

	@Override
	public String getRootKeyPassword() {
		return rootKeyPassword;
	}

	@Override
	public URL getCrlDistPoint() {
		return crlDistPoint;
	}

	@Override
	public void setCrlDistPoint(URL url) {
	}

	@Override
	public Collection<CertificateMetadata> getCertificates() {
		return certs;
	}

	@Override
	public CertificateMetadata findCertificate(String field, String value) {
		return null;
	}

	@Override
	public CertificateMetadata issueCertificate(CertificateRequest req) throws Exception {
		return null;
	}

	@Override
	public void importCertificate(CertificateMetadata cm) {
	}

	@Override
	public List<RevokedCertificate> getRevokedCertificates() {
		return revoked;
	}

	@Override
	public RevokedCertificate getRevokedCertificate(String serial) {
		return null;
	}

	@Override
	public void revoke(CertificateMetadata cm) {
	}

	@Override
	public void revoke(CertificateMetadata cm, RevocationReason reason) {
	}

	@Override
	public void addListener(CertEventListener listener) {
	}

	@Override
	public void removeListener(CertEventListener listener) {
	}

	@Override
	public Collection<CertificateMetadata> getCertificates(Predicate pred) {
		return null;
	}

	@Override
	public CertificateMetadataIterator getCertificateIterator() {
		return new CertificateMetadataIterator(new MockConfigIterator(certs));
	}

	@Override
	public CertificateMetadataIterator getCertificateIterator(Predicate pred) {
		return new CertificateMetadataIterator(new MockConfigIterator(certs));
	}

	@Override
	public RevokedCertificateIterator getRevokedCertificateIterator() {
		return new RevokedCertificateIterator(new MockConfigIterator(revoked));
	}

	@Override
	public RevokedCertificateIterator getRevokedCertificateIterator(Predicate pred) {
		return new RevokedCertificateIterator(new MockConfigIterator(revoked));
	}
}
