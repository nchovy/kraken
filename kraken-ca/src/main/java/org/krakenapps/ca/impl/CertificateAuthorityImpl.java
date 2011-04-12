/*
 * Copyright 2009 NCHOVY
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.ca.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.jce.interfaces.PKCS12BagAttributeCarrier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;
import org.krakenapps.ca.CertificateAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See {@link http://www.bouncycastle.org/wiki/display/JA1/Home}
 * 
 * @author xeraph
 * 
 */
@Component(name = "certificate-authority")
@Provides
public class CertificateAuthorityImpl implements CertificateAuthority {
	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityImpl.class.getName());
	private final File home = new File("data/kraken-ca/CA/");
	private static final String[] sigAlgorithms = new String[] { "MD5withRSA", "MD5withRSA", "SHA1withRSA",
			"SHA224withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA" };

	// install JCE provider
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}

	@Override
	public X509Certificate createSelfSignedCertificate(KeyPair keyPair, String dn, Date notBefore, Date notAfter,
			String signatureAlgorithm) throws Exception {
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

		certGen.setSerialNumber(new BigInteger("1"));
		certGen.setIssuerDN(new X509Name(dn));
		certGen.setNotBefore(notBefore);
		certGen.setNotAfter(notAfter);
		certGen.setSubjectDN(new X509Name(dn));
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm(signatureAlgorithm);

		return certGen.generate(keyPair.getPrivate());
	}

	@Override
	public X509Certificate createCertificate(X509Certificate caCert, PrivateKey caKey, KeyPair keyPair, String dn,
			Map<String, String> attrs, Date notBefore, Date notAfter, String signatureAlgorithm) throws Exception {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
		Vector<Object> oids = new Vector<Object>();
		Vector<Object> values = new Vector<Object>();
		parseDn(dn, oids, values);

		if (attrs != null) {
			for (String key : attrs.keySet()) {
				DERObjectIdentifier oid = (DERObjectIdentifier) X509Name.DefaultLookUp.get(key.toLowerCase());
				if (oid != null) {
					oids.add(oid);
					values.add(attrs.get(key));
				}
			}
		}

		certGen.setSerialNumber(new BigInteger("1"));
		certGen.setIssuerDN(caCert.getSubjectX500Principal());
		certGen.setNotBefore(notBefore);
		certGen.setNotAfter(notAfter);
		certGen.setSubjectDN(new X509Principal(oids, values));
		certGen.setPublicKey(keyPair.getPublic());
		certGen.setSignatureAlgorithm(signatureAlgorithm);

		return certGen.generate(caKey, "BC");
	}

	private void parseDn(String dn, Vector<Object> oids, Vector<Object> values) {
		String[] tokens = dn.split(",");
		for (String token : tokens) {
			int p = token.indexOf('=');
			String key = token.substring(0, p).trim().toLowerCase();
			String value = token.substring(p + 1).trim();

			DERObjectIdentifier oid = (DERObjectIdentifier) X509Name.DefaultLookUp.get(key);
			if (oid != null) {
				oids.add(oid);
				values.add(value);
			}
		}
	}

	public void exportPkcs12(String alias, File f, KeyPair keyPair, String keyPassword, Certificate cert,
			Certificate caCert) throws Exception {
		PKCS12BagAttributeCarrier bagAttr = (PKCS12BagAttributeCarrier) keyPair.getPrivate();
		bagAttr.setBagAttribute(new DERObjectIdentifier("1.2.840.113549.1.9.20"), new DERBMPString(alias));
		bagAttr.setBagAttribute(new DERObjectIdentifier("1.2.840.113549.1.9.21"), new SubjectKeyIdentifierStructure(
				keyPair.getPublic()));

		KeyStore pfx = KeyStore.getInstance("PKCS12", "BC");
		pfx.load(null, null);
		pfx.setKeyEntry(alias, keyPair.getPrivate(), null, new Certificate[] { cert, caCert });

		FileOutputStream out = new FileOutputStream(f);
		pfx.store(out, keyPassword.toCharArray());
		out.close();

	}

	@Override
	public Collection<String> getCertificates(String caCommonName) {
		File pfxBase = new File(home, caCommonName);
		File[] files = pfxBase.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".pfx");
			}
		});

		List<String> certFileNames = new ArrayList<String>();
		for (File file : files)
			certFileNames.add(file.getName());

		return certFileNames;
	}

	@Override
	public Collection<X509Certificate> getRootCertificates() {
		List<X509Certificate> certs = new ArrayList<X509Certificate>();

		for (File f : home.listFiles()) {
			if (f.isDirectory()) {
				File caFile = new File(f, "CA.jks");
				FileInputStream fs = null;
				try {
					fs = new FileInputStream(caFile);
					KeyStore store = KeyStore.getInstance("JKS");
					store.load(fs, null);
					X509Certificate cert = (X509Certificate) store.getCertificate("ca");
					certs.add(cert);
				} catch (Exception e) {
					logger.error("kraken ca: keystore error", e);
				} finally {
					if (fs != null)
						try {
							fs.close();
						} catch (IOException e) {
						}
				}
			}
		}

		return certs;
	}

	@Override
	public X509Certificate getCertificate(String caCommonName, String keyAlias, String keyPassword) throws Exception {
		File pfxBase = new File(home, caCommonName);
		File certFile = new File(pfxBase, keyAlias + ".pfx");

		FileInputStream fs = new FileInputStream(certFile);

		KeyStore pfx = KeyStore.getInstance("PKCS12", "BC");
		pfx.load(fs, keyPassword.toCharArray());
		return (X509Certificate) pfx.getCertificate(keyAlias);
	}

	@Override
	public byte[] getPfxFile(String caCommonName, String keyAlias, String keyPassword) throws IOException {
		File pfxBase = new File(home, caCommonName);
		File certFile = new File(pfxBase, keyAlias + ".pfx");

		byte[] b = new byte[(int) certFile.length()];
		FileInputStream fs = new FileInputStream(certFile);
		try {
			fs.read(b);
			return b;
		} finally {
			if (fs != null)
				fs.close();
		}
	}

	@Override
	public X509Certificate issueSelfSignedCertificate(String dn, String signatureAlgorithm, int days, String password)
			throws Exception {
		String cn = parseCN(dn);

		// check availability of signature algorithm
		validateSignatureAlgorithm(signatureAlgorithm);

		// generate key pair
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
		KeyPair keyPair = keyPairGen.generateKeyPair();

		// calculate date range
		Date notBefore = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(notBefore);
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date notAfter = cal.getTime();

		// generate ca cert
		X509Certificate cert = createSelfSignedCertificate(keyPair, dn, notBefore, notAfter, signatureAlgorithm);

		home.mkdirs();

		KeyStore store = KeyStore.getInstance("JKS");
		store.load(null, null);
		store.setCertificateEntry("ca", cert);
		store.setKeyEntry("ca-key", keyPair.getPrivate(), password.toCharArray(), new Certificate[] { cert });

		FileOutputStream os = null;
		String filename = "CA.jks";
		File caRoot = new File(home, cn);
		caRoot.mkdirs();

		try {
			File file = new File(caRoot, filename);
			os = new FileOutputStream(file);
			store.store(os, password.toCharArray());
		} finally {
			os.close();
		}

		return cert;
	}

	@Override
	public X509Certificate issueCertificate(String caCommonName, String caPassword, String keyAlias,
			String keyPassword, String dn, String signatureAlgorithm, int days) throws Exception {
		File caFile = new File(home, caCommonName + "/CA.jks");
		if (!caFile.exists())
			throw new IOException(caFile.getAbsolutePath() + " not found");

		Certificate caCert = null;
		RSAPrivateKey caKey = null;
		FileInputStream fs = new FileInputStream(caFile);
		KeyStore store = KeyStore.getInstance("JKS");

		try {
			store.load(fs, caPassword.toCharArray());
			caCert = store.getCertificate("ca");
			caKey = (RSAPrivateKey) store.getKey("ca-key", caPassword.toCharArray());
		} finally {
			fs.close();
		}

		// check availability of signature algorithm
		validateSignatureAlgorithm(signatureAlgorithm);

		// key pair generation
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
		KeyPair keyPair = keyPairGen.generateKeyPair();

		Date notBefore = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(notBefore);
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date notAfter = cal.getTime();

		// generate cert
		X509Certificate cert = createCertificate((X509Certificate) caCert, (PrivateKey) caKey, keyPair, dn, null,
				notBefore, notAfter, signatureAlgorithm);

		logger.info("kraken ca: generated new certificate [{}]", cert.getSubjectX500Principal().getName());

		// save pfx or p12 file
		String cn = parseCN(dn);
		File pfxBase = new File("data/kraken-ca/CA/", caCommonName);
		pfxBase.mkdirs();
		File f = new File(pfxBase, cn + ".pfx");

		logger.info("kraken ca: writing pfx file to " + f.getAbsolutePath());
		exportPkcs12(keyAlias, f, keyPair, keyPassword, cert, caCert);
		return cert;
	}

	private String parseCN(String dn) {
		int begin = dn.indexOf("CN=");
		if (begin < 0)
			throw new IllegalArgumentException("CN not found");

		int end = dn.indexOf(",", begin);
		if (end < 0)
			throw new IllegalArgumentException("CN not found");

		return dn.substring(begin + 3, end).trim();
	}

	private void validateSignatureAlgorithm(String algorithm) {
		for (int i = 0; i < sigAlgorithms.length; i++)
			if (sigAlgorithms[i].equals(algorithm))
				return;

		throw new IllegalArgumentException("invalid signature algorithm: " + algorithm);
	}
}
