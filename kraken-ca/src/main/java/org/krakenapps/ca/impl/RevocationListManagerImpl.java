package org.krakenapps.ca.impl;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.util.Iterator;
import java.util.Set;

import org.krakenapps.ca.RevocationListManager;

public class RevocationListManagerImpl implements RevocationListManager {
	public static void main(String[] args) throws Exception { 
		URL url = new URL("http://crl.verisign.com/pca1.1.1.crl");
		URLConnection conn = url.openConnection();

		InputStream is = conn.getInputStream();
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509CRL crl = (X509CRL) cf.generateCRL(is);
		is.close();
		
		Set<? extends X509CRLEntry> entries = crl.getRevokedCertificates();
		for(Iterator<? extends X509CRLEntry> it = entries.iterator(); it.hasNext(); ) { 
			X509CRLEntry entry = (X509CRLEntry)it.next();
			System.out.println("==================================");
			System.out.println(entry.getSerialNumber().toString(16));
			for(byte b: entry.toString().getBytes()) {
				System.out.printf("%02x ", b);
			}
			System.out.println("\n==================================");
		}
		
		System.out.println(crl.getIssuerDN().getName());
		BigInteger serial = new BigInteger("77458001810146857637016282784576820437");
		System.out.println(crl.getRevokedCertificate(serial));
	}
	
	@Override
	public void revoke(BigInteger serialNumber) {
//		File revokeList = new File(System.getProperty("kraken.data.dir"), "kraken-ca/");
	}

	@Override
	public boolean isRevoked(BigInteger serialNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public X509CRLEntry[] getRevocationList() {
		// TODO Auto-generated method stub
		return null;
	}
}