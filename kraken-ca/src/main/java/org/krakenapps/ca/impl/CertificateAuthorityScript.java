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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateAuthorityService;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.RevocationReason;
import org.krakenapps.ca.util.CertificateExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateAuthorityScript implements Script {
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}

	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityScript.class.getName());

	private CertificateAuthorityService ca;
	private ScriptContext context;

	private static final String[] sigAlgorithms = new String[] { "MD2withRSA", "MD5withRSA", "SHA1withRSA", "SHA224withRSA",
			"SHA256withRSA", "SHA384withRSA", "SHA512withRSA" };

	public CertificateAuthorityScript(CertificateAuthorityService ca) {
		this.ca = ca;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void exportRootCert(String[] args) {
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
			context.print("Authority Name? ");
			String authorityName = context.readLine();

			context.print("CA Private Key Password? ");
			String password = context.readPassword();

			CertificateAuthority authority = ca.getAuthority(authorityName);
			CertificateMetadata cm = authority.getRootCertificate();
			X509Certificate crt = cm.getCertificate(password);
			RSAPrivateKey key = cm.getPrivateKey(password);

			String extension = usePem ? ".pem" : ".crt";
			File dir = (File) context.getSession().getProperty("dir");
			File f = new File(dir, authorityName + extension);

			if (usePem)
				CertificateExporter.writePemFile(crt, key, f, exportKey);
			else
				CertificateExporter.writeCrtFile(crt, f);

		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken ca: export ca cert failed", e);
		}
	}

	public void createCert(String[] args) {
		try {
			// find authority
			context.print("Authority Name? ");
			String authorityName = context.readLine();
			CertificateAuthority authority = ca.getAuthority(authorityName);
			if (authority == null) {
				context.println("authority not found");
				return;
			}

			// ca private key password for signing
			context.print("CA Private Key Password? ");
			String password = context.readPassword();

			CertificateRequest req = inputRequest();
			CertificateMetadata cm = authority.issueCertificate(password, req);
			X509Certificate cert = cm.getCertificate(req.getKeyPassword());

			context.println(cert.toString());
			context.println("");
			context.println("Complete!");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (NumberFormatException e) {
			context.println("invalid number format");
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken ca: failed to create certificate", e);
		}
	}

	public void createAuthority(String[] args) {
		try {
			context.print("Authority Name? ");
			String name = context.readLine();

			CertificateRequest req = inputRequest();

			// for self signing
			req.setIssuerKey(req.getKeyPair().getPrivate());

			ca.createAuthority(name, req);
			context.println("Completed");
		} catch (InterruptedException e) {
			context.println("");
			context.println("Interrupted");
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken ca: create cert failed", e);
		}
	}

	private CertificateRequest inputRequest() throws Exception {
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

		String dn = String.format("CN=%s,OU=%s,O=%s,L=%s,ST=%s,C=%s", cn, ou, o, l, st, c);

		context.print("Days (default 365)? ");
		String daysLine = context.readLine();
		int days = 0;
		if (daysLine.isEmpty())
			days = 365; // 1 year
		else
			days = Integer.parseInt(daysLine);

		context.println("Generating key pairs...");
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
		KeyPair keyPair = keyPairGen.generateKeyPair();

		context.print("Key Password? ");
		String keyPassword = context.readPassword();

		Date notBefore = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(notBefore);
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date notAfter = cal.getTime();

		// generate
		return CertificateRequest.createSelfSignedCertRequest(keyPair, keyPassword, dn, notBefore, notAfter, signatureAlgorithm);
	}

	public void authorities(String[] args) {
		context.println("Certificate Authorities");
		context.println("-------------------------");

		for (CertificateAuthority authority : ca.getAuthorities())
			context.println(authority);
	}

	@ScriptUsage(description = "revoke a certificate", arguments = {
			@ScriptArgument(name = "authority name", type = "string", description = "authority name"),
			@ScriptArgument(name = "serial", type = "string", description = "cert serial (big integer range)"),
			@ScriptArgument(name = "reason", type = "string", description = "cert revocation reason (select enum constant)", optional = true) })
	public void revoke(String[] args) {
		String authorityName = args[0];
		String serial = args[1];
		RevocationReason reason = RevocationReason.Unspecified;
		if (args.length > 2)
			reason = RevocationReason.valueOf(args[2]);

		try {
			CertificateAuthority authority = ca.getAuthority(authorityName);
			if (authority == null) {
				context.println("authority not found");
				return;
			}

			CertificateMetadata cm = authority.findCertificate("serial", serial);
			if (cm == null) {
				context.println("certificate not found");
				return;
			}

			authority.revoke(cm, reason);
			context.println("revoked");
		} catch (Exception e) {
			context.println("Error: " + e.getMessage());
			logger.warn("kraken ca: failed to revoke certificate", e);
		}
	}
}