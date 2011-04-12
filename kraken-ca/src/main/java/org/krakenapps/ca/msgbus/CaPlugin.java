/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.ca.msgbus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.bouncycastle.util.encoders.Base64;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MsgbusPlugin
@Component(name = "ca-plugin")
public class CaPlugin {
	private final Logger logger = LoggerFactory.getLogger(CaPlugin.class.getName());

	@Requires
	private CertificateAuthority ca;

	@MsgbusMethod
	public void issueRootCertificate(Request req, Response resp) {
		try {
			// parameters
			int days = req.getInteger("days");
			String cn = req.getString("common_name");
			String ou = req.getString("org_unit");
			String o = req.getString("org");
			String l = req.getString("city");
			String st = req.getString("state");
			String c = req.getString("country");
			String signatureAlgorithm = req.getString("signature_algorithm");
			String password = req.getString("password");

			String dn = String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", cn, ou, o, l, st, c);

			X509Certificate cert = ca.issueSelfSignedCertificate(dn, signatureAlgorithm, days, password);
			resp.put("cert", marshal(cert));

		} catch (Exception e) {
			logger.error("kraken ca: cannot create ca cert", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void issueCertificate(Request req, Response resp) {
		try {
			String caCN = req.getString("ca_common_name");
			String caPassword = req.getString("ca_password");

			String keyAlias = req.getString("key_alias");
			String cn = req.getString("common_name");
			String ou = req.getString("org_unit");
			String o = req.getString("org");
			String l = req.getString("city");
			String st = req.getString("state");
			String c = req.getString("country");
			String signatureAlgorithm = req.getString("signature_algorithm");
			String keyPassword = req.getString("password");

			int days = req.getInteger("days");

			String dn = String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", cn, ou, o, l, st, c);

			ca.issueCertificate(caCN, caPassword, keyAlias, keyPassword, dn, signatureAlgorithm, days);
		} catch (MsgbusException e) {
			throw e;
		} catch (Exception e) {
			logger.error("kraken ca: cannot create cert", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void getRootCertificates(Request req, Response resp) {
		Collection<X509Certificate> rootCerts = ca.getRootCertificates();

		List<Object> l = new ArrayList<Object>();
		for (X509Certificate rootCert : rootCerts)
			l.add(marshal(rootCert));

		resp.put("root_certs", l);
	}

	@MsgbusMethod
	public void getCertificates(Request req, Response resp) {
		String caCN = req.getString("ca_common_name");
		resp.put("certs", ca.getCertificates(caCN));
	}

	@MsgbusMethod
	public void getCertificate(Request req, Response resp) {
		try {
			String caCN = req.getString("ca_common_name");
			String keyAlias = req.getString("key_alias");
			String keyPassword = req.getString("key_password");

			X509Certificate cert = ca.getCertificate(caCN, keyAlias, keyPassword);
			resp.put("cert", marshal(cert));

		} catch (FileNotFoundException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "cert-not-found");
		} catch (KeyStoreException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "keystore-not-found");
		} catch (NoSuchProviderException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "provider-not-found");
		} catch (NoSuchAlgorithmException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "algorithm-not-found");
		} catch (CertificateException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "cert-error");
		} catch (IOException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "io-error");
		} catch (Exception e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void getPfxFile(Request req, Response resp) {
		try {
			String caCN = req.getString("ca_common_name");
			String keyAlias = req.getString("key_alias");
			String keyPassword = req.getString("key_password");

			byte[] b = ca.getPfxFile(caCN, keyAlias, keyPassword);
			byte[] encoded = Base64.encode(b);
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < encoded.length; i++)
				sb.append((char) encoded[i]);
			
			resp.put("pfx", sb.toString());
		} catch (IOException e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "io-error");
		}
	}

	private Map<String, Object> marshal(X509Certificate cert) {
		Map<String, Object> m = new HashMap<String, Object>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		m.put("type", cert.getType());
		m.put("version", cert.getVersion());
		m.put("serial", cert.getSerialNumber());
		m.put("issuer", cert.getIssuerX500Principal().getName());
		m.put("subject", cert.getSubjectX500Principal().getName());
		m.put("not_before", dateFormat.format(cert.getNotBefore()));
		m.put("not_after", dateFormat.format(cert.getNotAfter()));
		m.put("public_key", cert.getPublicKey());
		m.put("signature_algorithm", cert.getSigAlgName());
		m.put("signature", toHexString(cert.getSignature()));

		return m;
	}

	private String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++)
			sb.append(String.format("%02x", b[i]));

		return sb.toString();
	}
}
