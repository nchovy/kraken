package org.krakenapps.ca.crl;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bouncycastle.x509.X509V2CRLGenerator;

public class CrlGenerator {
	private PrivateKey caPrivateKey;
	private X509Certificate caCert;

	public CrlGenerator(PrivateKey caPrivateKey, X509Certificate caCert) {
		this.caPrivateKey = caPrivateKey;
		this.caCert = caCert;
	}

	@SuppressWarnings("deprecation")
	public byte[] getCrl() throws Exception {
		X509V2CRLGenerator generator = new X509V2CRLGenerator();
		generator.setIssuerDN(caCert.getIssuerX500Principal());

		Date[] date = getUpdateDates();
		generator.setThisUpdate(date[0]);
		generator.setNextUpdate(date[1]);
		generator.setSignatureAlgorithm(caCert.getSigAlgName());

		RevokedCertificatesManager manager = new RevokedCertificatesManager();
		List<RevokedCertificate> l = manager.getRevokedCertifcates();
		for(RevokedCertificate rc: l)
			generator.addCRLEntry(rc.getSerialNumber(), rc.getRevocationDate(), rc.getReasonCode());
		X509CRL crl = generator.generate(caPrivateKey);
		return crl.getEncoded();
	}

	private Date[] getUpdateDates() throws IOException, ParseException {
		File dateFile = new File(System.getProperty("kraken.data.dir"), "kraken-ca/CA/date");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dateFile)));
		String thisUpdate = br.readLine();
		String nextUpdate = br.readLine();

		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");
		Date date1 = sdf.parse(thisUpdate);
		Date date2 = sdf.parse(nextUpdate);

		return new Date[] { date1, date2 };
	}
}