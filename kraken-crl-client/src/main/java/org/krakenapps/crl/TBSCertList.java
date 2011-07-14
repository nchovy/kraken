package org.krakenapps.crl;

import java.util.Date;
import java.util.List;

public class TBSCertList {
	private int version;
	private AlgorithmIdentifier signature;
	private Issuer issuer;
	private Date thisUpdate;
	private Date nextUpdate;
	private List<RevokedCertificate> revokedCertificates;
	
	public TBSCertList(int version, AlgorithmIdentifier signature, Issuer issuer, Date thisUpdate, Date nextUpdate, List<RevokedCertificate> revokedCertificates) {
		this.version = version;
		this.signature = signature;
		this.issuer = issuer;
		this.thisUpdate = thisUpdate;
		this.nextUpdate = nextUpdate;
		this.revokedCertificates = revokedCertificates;
	}
	
	public int getVersion() {
		return version;
	}
	
	public AlgorithmIdentifier getSignature() {
		return signature;
	}
	
	public Issuer getIssuer() {
		return issuer;
	}
	
	public Date getThisUpdate() {
		return thisUpdate;
	}
	
	public Date getNextUpdate() {
		return nextUpdate;
	}
	
	public List<RevokedCertificate> getRevokedCertificates() {
		return revokedCertificates;
	}
}