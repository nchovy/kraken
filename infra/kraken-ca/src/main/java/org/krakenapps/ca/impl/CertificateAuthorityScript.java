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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.krakenapps.api.PathAutoCompleter;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateAuthorityService;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.RevocationReason;
import org.krakenapps.ca.RevokedCertificate;
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

	@ScriptUsage(description = "import certificate authority", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "authority name"),
			@ScriptArgument(name = "file path", type = "string", description = "import file path", autocompletion = PathAutoCompleter.class) })
	public void importAuthority(String[] args) {
		InputStream is = null;
		try {
			File dir = (File) context.getSession().getProperty("dir");
			File importFile = canonicalize(dir, args[1]);

			if (!importFile.exists()) {
				context.println("file does not exists: " + importFile.getAbsolutePath());
				return;
			}

			if (!importFile.isFile()) {
				context.println("invalid file: " + importFile.getAbsolutePath());
				return;
			}

			if (!importFile.canRead()) {
				context.println("cannot read file, check read permission: " + importFile.getAbsolutePath());
				return;
			}

			is = new FileInputStream(importFile);
			ca.importAuthority(args[0], is);
		} catch (IOException e) {
			logger.error("kraken ca: cannot import authority", e);
			context.println(e.getMessage());
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}
	}

	@ScriptUsage(description = "export certificate authority", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "authority name"),
			@ScriptArgument(name = "file path", type = "string", description = "export file path", autocompletion = PathAutoCompleter.class) })
	public void exportAuthority(String[] args) {
		OutputStream os = null;
		try {
			File dir = (File) context.getSession().getProperty("dir");
			File exportFile = canonicalize(dir, args[1]);

			if (exportFile.exists()) {
				context.println("file already exists: " + exportFile.getAbsolutePath());
				return;
			}

			os = new FileOutputStream(exportFile);
			ca.exportAuthority(args[0], os);
		} catch (IOException e) {
			logger.error("kraken-ca: cannot export authority", e);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	private File canonicalize(File dir, String path) {
		if (path.startsWith("/"))
			return new File(path);
		else
			return new File(dir, path);
	}

	@ScriptUsage(description = "export certificate", arguments = {
			@ScriptArgument(name = "authority", type = "string", description = "authority name"),
			@ScriptArgument(name = "crl base url", type = "string", description = "new crl distribution point base url", optional = true) })
	public void crlDistPoint(String[] args) {
		String authorityName = args[0];
		CertificateAuthority authority = ca.getAuthority(authorityName);
		if (authority == null) {
			context.println("authority not found");
			return;
		}

		try {
			if (args.length > 1) {
				authority.setCrlDistPoint(new URL(args[1]));
				context.print("set ");
			}
		} catch (MalformedURLException e) {
			context.println("invalid CRL base URL: " + args[1]);
			return;
		}

		if (authority.getCrlDistPoint() == null) {
			context.println("not set");
			return;
		}

		context.println(authority.getCrlDistPoint());
	}

	@ScriptUsage(description = "export certificate", arguments = {
			@ScriptArgument(name = "authority", type = "string", description = "authority name"),
			@ScriptArgument(name = "serial", type = "string", description = "serial number") })
	public void export(String[] args) throws InterruptedException {
		String authorityName = args[0];
		CertificateAuthority authority = ca.getAuthority(authorityName);
		if (authority == null) {
			context.println("authority not found");
			return;
		}

		String serial = args[1];
		CertificateMetadata cm = authority.findCertificate("serial", serial);
		if (cm == null) {
			context.println("certificate not found");
			return;
		}

		File dir = (File) context.getSession().getProperty("dir");
		String ext = cm.getType();
		if (ext.equals("pkcs12"))
			ext = "pfx";

		File pfx = new File(dir, parseCN(cm.getSubjectDn()) + "." + ext);
		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(pfx, "rw");
			f.write(cm.getBinary());
			context.println("exported pfx file to " + pfx.getAbsolutePath());
		} catch (Exception e) {
			context.println("io failed: " + e.getMessage());
			logger.error("kraken ca: export failed", e);
		} finally {
			if (f != null) {
				try {
					f.close();
				} catch (IOException e) {
				}
			}
		}
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

			CertificateAuthority authority = ca.getAuthority(authorityName);
			CertificateMetadata cm = authority.getRootCertificate();
			X509Certificate crt = cm.getCertificate();
			RSAPrivateKey key = cm.getPrivateKey(authority.getRootKeyPassword());

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

	@ScriptUsage(description = "print all issued certs", arguments = { @ScriptArgument(name = "authority name", type = "string", description = "authority name") })
	public void certs(String[] args) throws InterruptedException {
		CertificateAuthority authority = ca.getAuthority(args[0]);
		if (authority == null) {
			context.println("authority not found");
			return;
		}

		context.println("Certificates");
		context.println("--------------");
		for (CertificateMetadata cm : authority.getCertificates()) {
			context.println(cm);
		}
	}

	public void issue(String[] args) {
		try {
			// find authority
			context.print("Authority Name? ");
			String authorityName = context.readLine();
			CertificateAuthority authority = ca.getAuthority(authorityName);
			if (authority == null) {
				context.println("authority not found");
				return;
			}

			CertificateRequest req = inputRequest();
			CertificateMetadata cm = authority.issueCertificate(req);
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
			context.println("created");
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		} catch (Exception e) {
			context.println("error: " + e.getMessage());
			logger.warn("kraken ca: create cert failed", e);
		}
	}

	@ScriptUsage(description = "remove authority and purge all certificates", arguments = { @ScriptArgument(name = "authority name", type = "string", description = "authority name") })
	public void removeAuthority(String[] args) {
		CertificateAuthority authority = ca.getAuthority(args[0]);
		if (authority == null) {
			context.println("authority not found");
			return;
		}

		ca.removeAuthority(args[0]);
		context.println("removed");
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

	@ScriptUsage(description = "print cert revocation list", arguments = { @ScriptArgument(name = "authority name", type = "string", description = "authority name") })
	public void crl(String[] args) {
		CertificateAuthority authority = ca.getAuthority(args[0]);
		if (authority == null) {
			context.println("authority not found");
			return;
		}

		for (RevokedCertificate c : authority.getRevokedCertificates()) {
			context.println(c);
		}
	}
}