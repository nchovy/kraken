package org.krakenapps.ca.crl;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bouncycastle.x509.X509V2CRLGenerator;

public class CrlGenerator {
	private PrivateKey caPrivateKey;
	private X509Certificate cert;

	public CrlGenerator(PrivateKey caPrivateKey, X509Certificate cert) {
		this.caPrivateKey = caPrivateKey;
		this.cert = cert;
	}

	public byte[] getCrl() throws Exception {
		X509V2CRLGenerator generator = new X509V2CRLGenerator();
		generator.setIssuerDN(cert.getIssuerX500Principal());

		Date[] date = getUpdateDates();
		generator.setThisUpdate(date[0]);
		generator.setNextUpdate(date[1]);
		generator.setSignatureAlgorithm(cert.getSigAlgName());

		addEntries(generator);
		X509CRL crl = generator.generate(caPrivateKey);
		return crl.getEncoded();
	}

	private Date[] getUpdateDates() throws IOException, ParseException {
		// File dateFile = new File(System.getProperty("kraken.data.dir"),
		// "kraken-ca/CA/date");
		File dateFile = new File("g:/date_file.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dateFile)));
		String thisUpdate = br.readLine();
		String nextUpdate = br.readLine();

		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");
		Date date1 = sdf.parse(thisUpdate);
		Date date2 = sdf.parse(nextUpdate);

		return new Date[] { date1, date2 };
	}

	private void addEntries(X509V2CRLGenerator generator) throws ParseException, IOException {
		// File revocationListFile = new
		// File(System.getProperty("kraken.data.dir"), "kraken-ca/CA/rc_list");
		File revocationListFile = new File("g:/rc_list.txt");
		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(revocationListFile)));

		BigInteger serialNumber = null;
		Date revocationDate = null;
		int reasonCode = -1;
		int rowCount = 0;
		while (true) {
			String line = br.readLine();

			if (line == null)
				break;

			if (line.equals(""))
				continue;

			if (rowCount == 0) {
				serialNumber = new BigInteger(line, 16);
				rowCount++;
			} else if (rowCount == 1) {
				revocationDate = sdf.parse(line);
				rowCount++;
			} else if (rowCount == 2) {
				reasonCode = Integer.parseInt(line);
				generator.addCRLEntry(serialNumber, revocationDate, reasonCode);

				serialNumber = null;
				revocationDate = null;
				reasonCode = -1;
				rowCount = 0;
			}
		}
	}
}