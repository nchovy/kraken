/*
 * Copyright 2010 NCHOVY
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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.util.CertExporter;
import org.krakenapps.ca.util.CertImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateAuthorityScript implements Script {
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}

	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityScript.class.getName());

	private CertificateAuthority x509cert;
	private ScriptContext context;
	private File home;

	private static final String[] sigAlgorithms = new String[] { "MD2withRSA", "MD5withRSA", "SHA1withRSA", "SHA224withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA" };

	public CertificateAuthorityScript(CertificateAuthority ca) {
		this.x509cert = ca;
		this.home = ca.getCARootDir();
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void exportCaCrt(String[] args) {
		boolean usePem = false;
		boolean exportKey = false;
		if (args.length >= 1) {
			for (int i = 0; i < args.length; ++i) {
				if (args[i].equals("-pem"))
					usePem = true;
				if (args[i].equals("-key"))
					exportKey = true;
			}
		}
		try {
			context.print("CA Common Name? ");
			String caCN = context.readLine();

			context.print("CA keystore password? ");
			String password = context.readPassword();

			File jks = new File(home, caCN + "/CA.jks");
			KeyStore store = CertImporter.loadJks(jks, password);

			String extension = usePem ? ".pem" : ".crt";
			File caDir = new File(home, caCN);
			File f = new File(caDir, caCN + extension);

			if (usePem)
				CertExporter.writePemFile(store, password, f, exportKey);
			else
				CertExporter.writeCrtFile(store, f);

		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken x509: export ca cert failed", e);
		}
	}

	public void createCert(String[] args) {
		try {
			context.print("CA Common Name? ");
			String caCN = context.readLine();

			File caFile = new File(home, caCN + "/CA.jks");
			if (!caFile.exists()) {
				context.println("CA keystore not found");
				return;
			}

			Certificate caCert = null;
			RSAPrivateKey caKey = null;
			FileInputStream fs = new FileInputStream(caFile);
			KeyStore store = KeyStore.getInstance("JKS");

			try {
				context.print("CA keystore password? ");
				String password = context.readPassword();
				store.load(fs, password.toCharArray());

				context.print("CA private-key password? ");
				String keyPassword = context.readPassword();

				caCert = store.getCertificate("ca");
				caKey = (RSAPrivateKey) store.getKey("ca-key", keyPassword.toCharArray());
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				context.println("CA keystore open failed: " + e.getMessage());
				logger.warn("kraken x509: ca keystore open failed", e);
				return;
			} finally {
				fs.close();
			}

			context.print("Common Name (CN)? ");
			String cn = context.readLine();

			context.print("Organization Unit (OU)? ");
			String ou = context.readLine();

			context.print("Organization (O)? ");
			String o = context.readLine();

			context.print("City (L)? ");
			String l = context.readLine();

			context.print("State (ST)? ");
			String st = context.readLine();

			context.print("Country Code (C)? ");
			String c = context.readLine();

			// select signature algorithm
			context.println("Select Signature Algorithm:");
			int i = 1;
			for (String sig : sigAlgorithms) {
				context.printf("[%d] %s\n", i, sig);
				i++;
			}

			context.printf("Select [1~%d] (default %d)? ", sigAlgorithms.length, sigAlgorithms.length);
			String sigSelect = context.readLine();

			int select;
			if (sigSelect.isEmpty())
				select = sigAlgorithms.length - 1;
			else
				select = Integer.parseInt(sigSelect) - 1;

			String signatureAlgorithm = sigAlgorithms[select];

			String dn = String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", cn, ou, o, l, st, c);

			// valid duration
			context.print("Days (default 365)?");
			String daysLine = context.readLine();
			int days = 0;
			if (daysLine.isEmpty())
				days = 365;
			else
				days = Integer.parseInt(daysLine);

			context.print("CRL Distribution Point (http)?");
			String dp = context.readLine();
			
			// attributes
			Map<String, String> attrs = new HashMap<String, String>();
			while (true) {
				context.print("Attribute Name (press enter to skip)? ");
				String name = context.readLine();
				if (name.isEmpty())
					break;

				context.print("Attribute Value? ");
				String value = context.readLine();

				DERObjectIdentifier oid = (DERObjectIdentifier) X509Name.DefaultLookUp.get(name);
				if (oid == null) {
					try {
						oid = new DERObjectIdentifier(name);
					} catch (Exception e) {
						context.println("Error: invalid oid will be ignored.");
					}
				}

				if (oid != null)
					attrs.put(name, value);
			}

			// key pair generation
			context.println("Generating key pairs...");
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			KeyPair keyPair = keyPairGen.generateKeyPair();

			Date notBefore = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(notBefore);
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date notAfter = cal.getTime();

			// generate cert
			X509Certificate cert = x509cert.createCertificate((X509Certificate) caCert, (PrivateKey) caKey, keyPair, dn, attrs, notBefore, notAfter, signatureAlgorithm, dp);

			context.println(cert.toString());

			// save pfx or p12 file
			File pfxBase = new File(home, caCN);
			pfxBase.mkdirs();
			File f = new File(pfxBase, cn + ".pfx");

			context.print("Key Alias? ");
			String alias = context.readLine();
			context.print("Key password? ");
			context.turnEchoOff();
			try {
				String password = context.readLine();
				context.println("");
				context.println("Writing pfx file to " + f.getAbsolutePath());
				x509cert.exportPkcs12(alias, f, keyPair, password, cert, caCert);
			} finally {
				context.turnEchoOn();
			}

			context.println("Completed");
		} catch (InterruptedException e) {
			context.println("");
			context.println("Interrupted");
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken x509: create cert failed", e);
		}
	}

	public void createRootCa(String[] args) {
		try {
			context.print("Common Name (CN)? ");
			String cn = context.readLine();

			context.print("Organization Unit (OU)? ");
			String ou = context.readLine();

			context.print("Organization (O)? ");
			String o = context.readLine();

			context.print("City (L)? ");
			String l = context.readLine();

			context.print("State (ST)? ");
			String st = context.readLine();

			context.print("Country Code (C)? ");
			String c = context.readLine();

			context.println("Select Signature Algorithm:");
			int i = 1;
			for (String sig : sigAlgorithms) {
				context.printf("[%d] %s\n", i, sig);
				i++;
			}

			context.printf("Select [1~%d] (default %d)? ", sigAlgorithms.length, sigAlgorithms.length);
			String sigSelect = context.readLine();

			int select;
			if (sigSelect.isEmpty())
				select = sigAlgorithms.length - 1;
			else
				select = Integer.parseInt(sigSelect) - 1;

			String signatureAlgorithm = sigAlgorithms[select];

			String dn = String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", cn, ou, o, l, st, c);

			context.print("Days (default 3650)? ");
			String daysLine = context.readLine();
			int days = 0;
			if (daysLine.isEmpty())
				days = 3650; // 10 year
			else
				days = Integer.parseInt(daysLine);

			context.println("Generating key pairs...");
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			KeyPair keyPair = keyPairGen.generateKeyPair();

			Date notBefore = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(notBefore);
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date notAfter = cal.getTime();

			// generate
			X509Certificate cert = x509cert.createSelfSignedCertificate(keyPair, dn, notBefore, notAfter, signatureAlgorithm);
			context.println(cert.toString());

			// save
			String keyPassword = null;
			String storePassword = null;

			try {
				context.turnEchoOff();
				context.print("KeyStore Password? ");
				storePassword = context.readLine();
				if (storePassword.isEmpty())
					storePassword = null;

				context.println("");
				context.print("PrivateKey Password? ");
				keyPassword = context.readLine();
				if (keyPassword.isEmpty())
					keyPassword = null;
			} finally {
				context.turnEchoOn();
			}

			home.mkdirs();

			KeyStore store = KeyStore.getInstance("JKS");
			store.load(null, null);
			store.setCertificateEntry("ca", cert);
			store.setKeyEntry("ca-key", keyPair.getPrivate(), keyPassword.toCharArray(), new Certificate[] { cert });

			FileOutputStream os = null;
			String filename = "CA.jks";
			File caRoot = new File(home, cn);
			caRoot.mkdirs();
			try {
				File file = new File(caRoot, filename);
				os = new FileOutputStream(file);
				store.store(os, storePassword.toCharArray());
			} catch (Exception e) {
				context.printf("Failed to save %s, Error: %s\n", filename, e.getMessage());
				logger.warn("kraken x509: failed to save CA cert", e);
			} finally {
				os.close();
			}

			context.println("");
			context.println("Complete!");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (NumberFormatException e) {
			context.println("invalid number format");
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken x509: failed to create CA root", e);
		}
	}
}
