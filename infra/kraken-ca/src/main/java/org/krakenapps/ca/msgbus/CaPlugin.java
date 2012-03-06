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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.bouncycastle.util.encoders.Base64;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateAuthorityService;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
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
	private CertificateAuthorityService ca;

	@MsgbusMethod
	public void issueRootCertificate(Request req, Response resp) {
		try {
			// parameters
			String authorityName = req.getString("authority");
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

			Date notBefore = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(notBefore);
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date notAfter = cal.getTime();

			// key pair generation
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			KeyPair keyPair = keyPairGen.generateKeyPair();

			CertificateRequest certReq = CertificateRequest.createSelfSignedCertRequest(keyPair, password, dn, notBefore,
					notAfter, signatureAlgorithm);
			ca.createAuthority(authorityName, certReq);

			CertificateAuthority authority = ca.createAuthority(authorityName, certReq);
			X509Certificate cert = authority.getRootCertificate().getCertificate(password);
			resp.put("cert", marshal(cert));

		} catch (Exception e) {
			logger.error("kraken ca: cannot create ca cert", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void issueCertificate(Request req, Response resp) {
		try {
			String authorityName = req.getString("authority");

			// check authority
			CertificateAuthority authority = ca.getAuthority(authorityName);
			if (authority == null)
				throw new MsgbusException("kraken-ca", "authority-not-found");

			// generate public/private key pair
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			CertificateRequest certReq = new CertificateRequest();

			// calculate valid period
			int days = req.getInteger("days");
			Date notBefore = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(notBefore);
			cal.add(Calendar.DAY_OF_YEAR, days);
			Date notAfter = cal.getTime();

			// build dn
			String cn = req.getString("common_name");
			String ou = req.getString("org_unit");
			String o = req.getString("org");
			String l = req.getString("city");
			String st = req.getString("state");
			String c = req.getString("country");

			// set
			certReq.setSubjectDn(String.format("CN=%s, OU=%s, O=%s, L=%s, ST=%s, C=%s", cn, ou, o, l, st, c));
			certReq.setKeyPair(keyPairGen.generateKeyPair());
			certReq.setSignatureAlgorithm(req.getString("signature_algorithm"));
			certReq.setKeyPassword(req.getString("password"));
			certReq.setNotBefore(notBefore);
			certReq.setNotAfter(notAfter);

			authority.issueCertificate(certReq);

		} catch (MsgbusException e) {
			throw e;
		} catch (Exception e) {
			logger.error("kraken ca: cannot create cert", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void getRootCertificates(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();

		for (CertificateAuthority authority : ca.getAuthorities()) {
			CertificateMetadata cm = authority.getRootCertificate();
			l.add(PrimitiveConverter.serialize(cm));
		}

		resp.put("root_certs", l);
	}

	@MsgbusMethod
	public void getCertificates(Request req, Response resp) {
		String authorityName = req.getString("authority");

		// check authority
		CertificateAuthority authority = ca.getAuthority(authorityName);
		if (authority == null)
			throw new MsgbusException("kraken-ca", "authority-not-found");

		List<Object> l = new LinkedList<Object>();
		for (CertificateMetadata cm : authority.getCertificates()) {
			l.add(PrimitiveConverter.serialize(cm));
		}

		resp.put("certs", l);
	}

	@MsgbusMethod
	public void getCertificate(Request req, Response resp) {
		try {
			String authorityName = req.getString("authority");
			String serial = req.getString("serial");

			// check authority
			CertificateAuthority authority = ca.getAuthority(authorityName);
			if (authority == null)
				throw new MsgbusException("kraken-ca", "authority-not-found");

			CertificateMetadata cm = authority.findCertificate("serial", serial);
			resp.put("cert", cm == null ? null : PrimitiveConverter.serialize(cm));
		} catch (Exception e) {
			logger.error("kraken ca: cannot get certificate", e);
			throw new MsgbusException("ca", "general-error");
		}
	}

	@MsgbusMethod
	public void getPfxFile(Request req, Response resp) {
		String authorityName = req.getString("authority");
		String serial = req.getString("serial");

		// check authority
		CertificateAuthority authority = ca.getAuthority(authorityName);
		if (authority == null)
			throw new MsgbusException("kraken-ca", "authority-not-found");

		CertificateMetadata cm = authority.findCertificate("serial", serial);
		if (cm == null)
			throw new MsgbusException("kraken-ca", "certificate-not-found");

		byte[] encoded = Base64.encode(cm.getBinary());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < encoded.length; i++)
			sb.append((char) encoded[i]);

		resp.put("pfx", sb.toString());
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
