package org.krakenapps.ca.impl;

import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateAuthorityService;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.util.CertificateBuilder;
import org.krakenapps.ca.util.CertificateExporter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicates;

@Component(name = "certificate-authority")
@Provides
public class CertificateAuthorityServiceImpl implements CertificateAuthorityService {

	// install JCE provider
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}

	@Requires
	private ConfigService conf;

	private ConcurrentMap<String, CertificateAuthority> authorities;

	@SuppressWarnings("unchecked")
	@Validate
	public void start() {
		authorities = new ConcurrentHashMap<String, CertificateAuthority>();
		ConfigDatabase master = conf.ensureDatabase("kraken-ca");
		ConfigCollection col = master.ensureCollection("authority");
		ConfigIterator it = col.findAll();

		try {
			while (it.hasNext()) {
				Config c = it.next();
				Map<String, Object> doc = (Map<String, Object>) c.getDocument();
				String name = (String) doc.get("name");

				ConfigDatabase db = conf.ensureDatabase("kraken-ca-" + name);
				CertificateAuthority authority = new CertificateAuthorityImpl(db, name);
				authorities.put(authority.getName(), authority);
			}
		} finally {
			it.close();
		}
	}

	@Override
	public List<CertificateAuthority> getAuthorities() {
		return new ArrayList<CertificateAuthority>(authorities.values());
	}

	@Override
	public CertificateAuthority getAuthority(String name) {
		return authorities.get(name);
	}

	@Override
	public CertificateAuthority createAuthority(String name, CertificateRequest req) throws Exception {
		validate(req);

		// generate ca cert
		Map<String, Object> doc = new HashMap<String, Object>();
		doc.put("name", name);
		doc.put("created_at", new Date());

		ConfigDatabase db = conf.ensureDatabase("kraken-ca-" + name);
		ConfigDatabase ca = conf.ensureDatabase("kraken-ca");
		ConfigCollection col = ca.ensureCollection("authority");
		col.add(doc);

		CertificateAuthority authority = new CertificateAuthorityImpl(db, name);
		authorities.put(authority.getName(), authority);

		// save pfx
		X509Certificate root = CertificateBuilder.createCertificate(req);
		byte[] jks = CertificateExporter.exportJks(root, req.getKeyPair(), req.getKeyPassword(), root);
		CertificateMetadata cm = new CertificateMetadata();
		cm.setType("jks");
		cm.setSerial(req.getSerial().toString());
		cm.setSubjectDn(req.getSubjectDn());
		cm.setNotBefore(req.getNotBefore());
		cm.setNotAfter(req.getNotAfter());
		cm.setBinary(jks);

		// set authority metadata
		ConfigCollection metadata = db.ensureCollection("metadata");
		Object cert = PrimitiveConverter.serialize(cm);
		Map<String, Object> pw = newRootKeyPassword(req);

		// commit metadata transaction
		ConfigTransaction xact = db.beginTransaction(5000);
		try {
			metadata.add(xact, cert);
			metadata.add(xact, pw);
			xact.commit("kraken-ca", "added root certificate and password");
		} catch (Exception e) {
			xact.rollback();
		}

		// set master metadata
		metadata = ca.ensureCollection("metadata");
		metadata.add(doc, "kraken-ca", "added " + name + " authority");

		return authority;
	}

	private Map<String, Object> newRootKeyPassword(CertificateRequest req) {
		Map<String, Object> pw = new HashMap<String, Object>();
		pw.put("type", "rootpw");
		pw.put("password", req.getKeyPassword());
		return pw;
	}

	private void validate(CertificateRequest req) {
		// check availability of signature algorithm
		CertificateAuthorityImpl.validateSignatureAlgorithm(req.getSignatureAlgorithm());

		if (req.getKeyPassword() == null)
			throw new IllegalArgumentException("ca key password should be not null");
	}

	@Override
	public void removeAuthority(String name) {
		CertificateAuthority authority = authorities.remove(name);
		if (authority == null)
			return;

		conf.dropDatabase("kraken-ca-" + authority.getName());

		ConfigDatabase ca = conf.ensureDatabase("kraken-ca");
		ConfigCollection col = ca.ensureCollection("authority");
		Config c = col.findOne(Predicates.field("name", name));
		if (c != null)
			col.remove(c, false, "kraken-ca", "removed authority: " + name);
	}
}
