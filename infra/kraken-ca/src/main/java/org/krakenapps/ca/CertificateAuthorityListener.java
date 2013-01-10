package org.krakenapps.ca;

public interface CertificateAuthorityListener {
	/**
	 * fire event after a new certificate authority is created
	 * 
	 * @param ca
	 */
	void onCreateAuthority(CertificateAuthority ca);

	void onRemoveAuthority(String name);

	void onRevokeCert(CertificateAuthority ca, CertificateMetadata cm, RevocationReason reason);

	void onIssueCert(CertificateAuthority ca, CertificateMetadata cm);
}
