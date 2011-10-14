package org.krakenapps.ca.crl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RevokedCertificatesManager {
	private File rcList;

	public RevokedCertificatesManager() {
		rcList = new File(System.getProperty("kraken.data.dir"), "kraken-ca/CA/rc_list");
	}

	public void revoke(X509Certificate cert) throws Exception {
		revoke(cert, 0);
	}

	public void revoke(X509Certificate cert, int reasonCode) throws Exception {
		String serial = cert.getSerialNumber().toString(16);
		if (isAlreadyRevoked(serial))
			throw new Exception("kraken-ca: already revoked");

		revoke(serial, reasonCode);
	}

	public void revoke(String serialNumber) throws Exception {
		revoke(serialNumber, 0);
	}

	public void revoke(String serialNumber, int reasonCode) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");
		Date date = new Date();
		String dateStr = sdf.format(date);

		OutputStream fos = new FileOutputStream(rcList, true);
		String revoked = serialNumber + "," + dateStr + "," + reasonCode + "\n";
		fos.write(revoked.getBytes());
		fos.close();
	}

	public List<RevokedCertificate> getRevokedCertifcates() throws Exception {
		if (!rcList.exists())
			return null;

		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rcList)));
		SimpleDateFormat sdf = new SimpleDateFormat("yy-mm-dd HH:mm:ss");

		List<RevokedCertificate> l = new ArrayList<RevokedCertificate>();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			String[] token = line.split(",");
			BigInteger serialNumber = new BigInteger(token[0], 16);
			Date revocationDate = sdf.parse(token[1]);
			int reasonCode = Integer.parseInt(token[2]);

			l.add(new RevokedCertificate(serialNumber, revocationDate, reasonCode));
		}

		br.close();
		return l;
	}

	private boolean isAlreadyRevoked(String serialNumber) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rcList)));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			String[] token = line.split(",");
			if (token[0].equals(serialNumber))
				return true;
		}
		return false;
	}
}