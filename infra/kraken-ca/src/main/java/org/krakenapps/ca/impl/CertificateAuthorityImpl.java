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

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertEventListener;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateMetadataIterator;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.RevocationReason;
import org.krakenapps.ca.RevokedCertificate;
import org.krakenapps.ca.RevokedCertificateIterator;
import org.krakenapps.ca.util.CertificateBuilder;
import org.krakenapps.ca.util.CertificateExporter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See {@link http://www.bouncycastle.org/wiki/display/JA1/Home}
 * 
 * @author xeraph
 * 
 */
public class CertificateAuthorityImpl implements CertificateAuthority {
	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityImpl.class.getName());
	private static final String[] sigAlgorithms = new String[] { "MD5withRSA", "MD5withRSA", "SHA1withRSA", "SHA224withRSA",
			"SHA256withRSA", "SHA384withRSA", "SHA512withRSA" };

	/**
	 * config database which contains "metadata", "certs", and "revoked" config
	 * collections.
	 */
	private ConfigDatabase db;

	/**
	 * authority name
	 */
	private String name;

	private CopyOnWriteArraySet<CertEventListener> listeners;

	public CertificateAuthorityImpl(ConfigDatabase db, String name) {
		this.db = db;
		this.name = name;
		listeners = new CopyOnWriteArraySet<CertEventListener>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CertificateMetadata getRootCertificate() {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config jks = metadata.findOne(Predicates.field("type", "jks"));
		if (jks == null)
			throw new IllegalStateException("jks not found for " + name);

		return PrimitiveConverter.parse(CertificateMetadata.class, jks.getDocument());
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getRootKeyPassword() {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config c = metadata.findOne(Predicates.field("type", "rootpw"));
		if (c == null)
			throw new IllegalStateException("root key password not found");

		Map<String, Object> m = (Map<String, Object>) c.getDocument();
		return (String) m.get("password");
	}

	@Override
	public URL getCrlDistPoint() {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config c = metadata.findOne(Predicates.field("type", "crl"));
		if (c == null)
			return null;

		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) c.getDocument();
			return new URL((String) m.get("base_url"));
		} catch (MalformedURLException e) {
			// unreachable
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void setCrlDistPoint(URL url) {
		String baseUrl = url.toString();
		if (baseUrl.endsWith("/"))
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

		ConfigCollection metadata = db.ensureCollection("metadata");
		Config c = metadata.findOne(Predicates.field("type", "crl"));

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("type", "crl");

		m.put("base_url", baseUrl);

		if (c == null) {
			metadata.add(m, "kraken-ca", "set CRL distribution point");
		} else {
			c.setDocument(m);
			metadata.update(c, false, "kraken-ca", "updated CRL distribution point");
		}
	}

	@Override
	public CertificateMetadataIterator getCertificateIterator() {
		return getCertificateIterator(null);
	}

	@Override
	public CertificateMetadataIterator getCertificateIterator(Predicate pred) {
		ConfigCollection certs = db.ensureCollection("certs");
		ConfigIterator it = certs.find(pred);

		return new CertificateMetadataIterator(it);
	}

	@Override
	public List<CertificateMetadata> getCertificates(Predicate pred) {
		ConfigCollection certs = db.ensureCollection("certs");

		List<CertificateMetadata> l = new LinkedList<CertificateMetadata>();
		ConfigIterator it = certs.find(pred);
		try {
			while (it.hasNext()) {
				Config c = it.next();
				CertificateMetadata cm = PrimitiveConverter.parse(CertificateMetadata.class, c.getDocument());
				l.add(cm);
			}
		} finally {
			it.close();
		}
		return l;
	}

	@Override
	public List<CertificateMetadata> getCertificates() {
		return getCertificates(null);
	}

	@Override
	public BigInteger getLastSerial() {
		ConfigCollection col = db.ensureCollection("metadata");
		Config c = col.findOne(Predicates.field("type", "serial"));
		if (c == null)
			return new BigInteger("1");

		CertSerial s = PrimitiveConverter.parse(CertSerial.class, c.getDocument());
		return new BigInteger(s.serial);
	}

	@Override
	public BigInteger getNextSerial() {
		ConfigCollection col = db.ensureCollection("metadata");
		Config c = col.findOne(Predicates.field("type", "serial"));
		if (c == null) {
			Object doc = PrimitiveConverter.serialize(new CertSerial());
			col.add(doc, "kraken-ca", "init serial");
			return new BigInteger("2");
		}

		CertSerial s = PrimitiveConverter.parse(CertSerial.class, c.getDocument());
		s.serial = new BigInteger(s.serial).add(new BigInteger("1")).toString();
		c.setDocument(PrimitiveConverter.serialize(s));
		col.update(c, false, "kraken-ca", "set next serial");
		return new BigInteger(s.serial);
	}

	@Override
	public CertificateMetadata findCertificate(String field, String value) {
		ConfigCollection certs = db.ensureCollection("certs");
		Config c = certs.findOne(Predicates.field(field, value));
		if (c == null)
			return null;

		return PrimitiveConverter.parse(CertificateMetadata.class, c.getDocument());
	}

	@Override
	public CertificateMetadata issueCertificate(CertificateRequest req) throws Exception {
		ConfigCollection metadata = db.ensureCollection("metadata");
		Config jks = metadata.findOne(Predicates.field("type", "jks"));
		if (jks == null)
			throw new IllegalStateException("ca not found for " + name);

		CertificateMetadata cm = PrimitiveConverter.parse(CertificateMetadata.class, jks.getDocument());
		req.setSerial(getNextSerial());
		req.setIssuerDn(cm.getSubjectDn());
		req.setIssuerKey(cm.getPrivateKey(getRootKeyPassword()));

		URL dist = getCrlDistPoint();
		if (dist != null) {
			dist = new URL(dist + "/ca/crl/" + name + "?serial=" + req.getSerial().toString());
			req.setCrlUrl(dist);
		}

		// check availability of signature algorithm
		validateSignatureAlgorithm(req.getSignatureAlgorithm());

		// generate cert
		X509Certificate caCert = cm.getCertificate();
		X509Certificate cert = CertificateBuilder.createCertificate(req);
		byte[] pkcs12 = CertificateExporter.exportPkcs12(cert, req.getKeyPair(), req.getKeyPassword(), caCert);

		cm = new CertificateMetadata();
		cm.setType("pkcs12");
		cm.setSerial(req.getSerial().toString());
		cm.setSubjectDn(req.getSubjectDn());
		cm.setNotBefore(req.getNotBefore());
		cm.setNotAfter(req.getNotAfter());
		cm.setBinary(pkcs12);

		ConfigCollection certs = db.ensureCollection("certs");
		Object c = PrimitiveConverter.serialize(cm);
		certs.add(c, "kraken-ca", "issued certificate for " + req.getSubjectDn());

		logger.info("kraken ca: generated new certificate [{}]", cert.getSubjectX500Principal().getName());

		for (CertEventListener listener : listeners) {
			try {
				listener.onIssued(this, cm);
			} catch (Throwable t) {
				logger.error("kraken ca: certificate issue callback should not throw any exception", t);
			}
		}

		return cm;
	}

	@Override
	public void importCertificate(CertificateMetadata cm) {
		ConfigCollection certs = db.ensureCollection("certs");
		Object c = PrimitiveConverter.serialize(cm);
		certs.add(c, "kraken-ca", "import certificate, " + cm.getSubjectDn());

		logger.info("kraken ca: import new certificate [{}]", cm.getSubjectDn());

		for (CertEventListener listener : listeners) {
			try {
				listener.onIssued(this, cm);
			} catch (Throwable t) {
				logger.error("kraken ca: certificate issue callback should not throw any exception", t);
			}
		}
	}

	public static void validateSignatureAlgorithm(String algorithm) {
		for (int i = 0; i < sigAlgorithms.length; i++)
			if (sigAlgorithms[i].equals(algorithm))
				return;

		throw new IllegalArgumentException("invalid signature algorithm: " + algorithm);
	}

	@Override
	public void revoke(CertificateMetadata cm) {
		revoke(cm, RevocationReason.Unspecified);
	}

	@Override
	public void revoke(CertificateMetadata cm, RevocationReason reason) {
		ConfigCollection revoked = db.ensureCollection("revoked");

		Config c = revoked.findOne(Predicates.field("serial", cm.getSerial()));
		if (c != null)
			throw new IllegalStateException("already revoked: serial " + cm.getSerial());

		RevokedCertificate r = new RevokedCertificate(cm.getSerial(), new Date(), reason);
		revoked.add(PrimitiveConverter.serialize(r), "kraken-ca", "revoked " + cm);

		for (CertEventListener listener : listeners) {
			try {
				listener.onRevoked(this, cm, reason);
			} catch (Throwable t) {
				logger.error("kraken ca: certificate revoke callback should not throw any exception", t);
			}
		}
	}

	@Override
	public RevokedCertificate getRevokedCertificate(String serial) {
		ConfigCollection revoked = db.ensureCollection("revoked");
		Config config = revoked.findOne(Predicates.field("serial", serial));

		if (config == null)
			return null;

		RevokedCertificate rc = PrimitiveConverter.parse(RevokedCertificate.class, config.getDocument());

		return rc;
	}

	@Override
	public List<RevokedCertificate> getRevokedCertificates() {
		ConfigCollection revoked = db.ensureCollection("revoked");

		ConfigIterator it = revoked.findAll();

		List<RevokedCertificate> l = new LinkedList<RevokedCertificate>();

		try {
			while (it.hasNext()) {
				Config c = it.next();
				RevokedCertificate rc = PrimitiveConverter.parse(RevokedCertificate.class, c.getDocument());
				l.add(rc);
			}
		} finally {
			it.close();
		}

		return l;
	}

	@Override
	public RevokedCertificateIterator getRevokedCertificateIterator() {
		return getRevokedCertificateIterator(null);
	}

	@Override
	public RevokedCertificateIterator getRevokedCertificateIterator(Predicate pred) {
		ConfigCollection revoked = db.ensureCollection("revoked");

		ConfigIterator it = revoked.find(pred);
		return new RevokedCertificateIterator(it);
	}

	@Override
	public String toString() {
		return name + ": " + getRootCertificate().getSubjectDn();
	}

	private static class CertSerial {
		@SuppressWarnings("unused")
		private final String type = "serial";
		private String serial = "2";

		public CertSerial() {
		}
	}

	@Override
	public void addListener(CertEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should be not null");
		listeners.add(listener);
	}

	@Override
	public void removeListener(CertEventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should be not null");
		listeners.remove(listener);
	}
}