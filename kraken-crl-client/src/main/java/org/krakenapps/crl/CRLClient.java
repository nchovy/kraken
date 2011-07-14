package org.krakenapps.crl;

import javax.naming.NamingException;

public class CRLClient {
	public static CertificateRevocationList getCRL(String url, String filter) throws NamingException { 
		byte[] crlBytes = LdapSearch.getCrls(url, filter);
		CRLParser parser = new CRLParser();
		return parser.parse(crlBytes);
	}
}