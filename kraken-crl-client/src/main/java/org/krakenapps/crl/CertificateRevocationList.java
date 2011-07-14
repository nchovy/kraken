package org.krakenapps.crl;

public class CertificateRevocationList {
	private TBSCertList tbsCertList;
	private AlgorithmIdentifier identifier;
	
	public CertificateRevocationList(TBSCertList tbsCertList, AlgorithmIdentifier identifier) {
		this.tbsCertList = tbsCertList;
		this.identifier = identifier;
	}
	
	public TBSCertList getTbsCertList() {
		return tbsCertList;
	}

	public AlgorithmIdentifier getIdentifier() {
		return identifier;
	}
}
