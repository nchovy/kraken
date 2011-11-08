package org.krakenapps.ca.util;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import org.bouncycastle.x509.X509V2CRLGenerator;
import org.krakenapps.ca.RevokedCertificate;

@SuppressWarnings("deprecation")
public class CrlBuilder {
	private PrivateKey caPrivateKey;
	private X509Certificate caCert;

	public CrlBuilder(PrivateKey caPrivateKey, X509Certificate caCert) {
		this.caPrivateKey = caPrivateKey;
		this.caCert = caCert;
	}

	public byte[] getCrl(List<RevokedCertificate> certs) throws Exception {
		X509V2CRLGenerator generator = new X509V2CRLGenerator();
		generator.setIssuerDN(caCert.getIssuerX500Principal());

		generator.setThisUpdate(new Date());
		generator.setSignatureAlgorithm(caCert.getSigAlgName());

		for (RevokedCertificate r : certs) {
			BigInteger serial = new BigInteger(r.getSerial());
			generator.addCRLEntry(serial, r.getRevocationDate(), r.getReason().ordinal());
		}

		X509CRL crl = generator.generate(caPrivateKey);
		return crl.getEncoded();
	}
}