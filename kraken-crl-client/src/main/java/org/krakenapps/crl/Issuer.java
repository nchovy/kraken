package org.krakenapps.crl;

import java.util.List;

public class Issuer {
	private List<RelativeDistinguishedName> rdns;
	
	public List<RelativeDistinguishedName> getRdns() {
		return rdns;
	}

	public Issuer(List<RelativeDistinguishedName> rdns) {
		this.rdns = rdns;
	}
}