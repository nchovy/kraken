package org.krakenapps.ca;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.List;

public class CrlGenerator {
	private X509Certificate cert;

	public CrlGenerator(X509Certificate cert) {
		this.cert = cert;
	}

	public void generate(List<RevokedCertificate> l) throws ParseException, IOException {
		File dateFile = new File(System.getProperty("kraken.data.dir"), "kraken-ca/CA/date");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dateFile)));
		// String thisUpdate = br.readLine();
		// String nextUpdate = br.readLine();
	}
	
	
	private void createIssuer() {
		// order: make BERObject. C -> ST -> ... -> CN

	}
}
