package org.krakenapps.ca;

import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.List;

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

}
