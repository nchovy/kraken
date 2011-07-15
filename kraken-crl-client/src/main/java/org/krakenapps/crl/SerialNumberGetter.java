package org.krakenapps.crl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SerialNumberGetter {
	private SerialNumberGetter() {
	}

	/**
	 * @param file path of public key certificate
	 */
	public static BigInteger getSerialNumber(String filePath) {
		InputStream is = null;
		try {
			is = new FileInputStream(new File(filePath));
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
			X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(is);
			return cert.getSerialNumber();
		} catch (FileNotFoundException e) {
			return null;
		} catch (CertificateException e) {
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
			}
		}
	}
}