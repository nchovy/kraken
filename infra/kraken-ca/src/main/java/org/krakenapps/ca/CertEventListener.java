package org.krakenapps.ca;

public interface CertEventListener {
	void onRevoked(CertificateMetadata cm, RevocationReason reason);
	
	void onIssued(CertificateMetadata cm);
}
