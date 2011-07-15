package org.krakenapps.crl;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;

import javax.naming.NamingException;

public class CRLClient {
	private X509CRL crl;
	
	public CRLClient(String crlDpUrl, String filter) {
		try {
			byte[] crlBytes = LdapSearch.getCrls(crlDpUrl, filter);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crlBytes));
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (CRLException e) {
			e.printStackTrace();
		}
	}

	public boolean isRevoked(BigInteger serialNumber) {
		if(crl == null) 
			throw new NullPointerException();
		
		if(crl.getRevokedCertificate(serialNumber) == null)
			return false;
		else
			return true;
	}

	/**
	 * @param file path of public key certificate
	 */
	public boolean isRevoked(String filePath) {
		BigInteger serialNumber = SerialNumberGetter.getSerialNumber(filePath);
		return isRevoked(serialNumber);
	}
}