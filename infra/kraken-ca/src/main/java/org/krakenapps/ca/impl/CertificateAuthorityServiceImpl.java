/*
 * Copyright 2012 Future Systems
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
import java.security.Security;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ca.CertEventListener;
import org.krakenapps.ca.CertificateAuthority;
import org.krakenapps.ca.CertificateAuthorityListener;
import org.krakenapps.ca.CertificateAuthorityService;
import org.krakenapps.ca.CertificateMetadata;
import org.krakenapps.ca.CertificateRequest;
import org.krakenapps.ca.RevocationReason;
import org.krakenapps.ca.util.CertificateBuilder;
import org.krakenapps.ca.util.CertificateExporter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "certificate-authority")
@Provides
public class CertificateAuthorityServiceImpl implements CertificateAuthorityService {
	private static final String DBNAME_PREFIX = "kraken-ca-";
	private final Logger logger = LoggerFactory.getLogger(CertificateAuthorityServiceImpl.class.getName());
	private static File baseDir = new File(System.getProperty("kraken.data.dir"), "kraken-ca");

	// install JCE provider
	static {
		if (Security.getProvider("BC") == null)
			Security.addProvider(new BouncyCastleProvider());
	}

	@Requires
	private ConfigService conf;

	private ConcurrentMap<String, CertificateAuthority> authorities;

	private CopyOnWriteArraySet<CertificateAuthorityListener> listeners;

	private CertificateListener certListener;

	public CertificateAuthorityServiceImpl() {
		authorities = new ConcurrentHashMap<String, CertificateAuthority>();
		listeners = new CopyOnWriteArraySet<CertificateAuthorityListener>();
		certListener = new CertificateListener();
	}

	@Validate
	public void start() {
		initializeCache();
	}

	@Invalidate
	public void stop() {
		for (CertificateAuthority authority : getAuthorities())
			authority.removeListener(certListener);

	}

	private void initializeCache() {
		ConfigDatabase master = conf.ensureDatabase("kraken-ca");
		ConfigCollection col = master.ensureCollection("authority");
		ConfigIterator it = col.findAll();

		authorities.clear();
		try {
			while (it.hasNext()) {
				Config c = it.next();
				@SuppressWarnings("unchecked")
				Map<String, Object> doc = (Map<String, Object>) c.getDocument();
				String name = (String) doc.get("name");

				ConfigDatabase db = conf.ensureDatabase(DBNAME_PREFIX + name);
				CertificateAuthority authority = new CertificateAuthorityImpl(db, name);
				authority.addListener(certListener);
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

		ConfigDatabase db = conf.ensureDatabase(DBNAME_PREFIX + name);
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

		authority.addListener(certListener);
		for (CertificateAuthorityListener listener : listeners) {
			try {
				listener.onCreateAuthority(authority);
			} catch (Throwable t) {
				logger.error("kraken ca: create authority callback should not throw any exception", t);
			}
		}

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

		conf.dropDatabase(DBNAME_PREFIX + authority.getName());

		ConfigDatabase ca = conf.ensureDatabase("kraken-ca");
		ConfigCollection col = ca.ensureCollection("authority");
		Config c = col.findOne(Predicates.field("name", name));
		if (c != null) {
			col.remove(c, false, "kraken-ca", "removed authority: " + name);

			for (CertificateAuthorityListener listener : listeners) {
				try {
					listener.onRemoveAuthority(name);
				} catch (Throwable t) {
					logger.error("kraken ca: remove authority callback should not throw any exception", t);
				}
			}
		}
	}

	@Override
	public void addListener(CertificateAuthorityListener listener) {
		if (listener == null)
			throw new IllegalStateException("certificate authority listener should be not null");
		listeners.add(listener);
	}

	@Override
	public void removeListener(CertificateAuthorityListener listener) {
		if (listener == null)
			throw new IllegalStateException("certificate authority listener should be not null");
		listeners.remove(listener);
	}

	@Override
	public CertificateAuthority importAuthority(String name, InputStream is) throws IOException {
		File exportFile = File.createTempFile(name, ".cdb", baseDir);
		OutputStream os = null;
		try {
			os = new FileOutputStream(exportFile);
			CertificateAuthorityFormatter.convertToInternalFormat(is, os);
		} catch (ParseException e) {
			throw new IOException(e);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}

		InputStream cdbIs = null;
		ConfigDatabase db = conf.getDatabase(DBNAME_PREFIX + name);
		if (db != null)
			throw new IllegalStateException("authority [" + name + "] already exists");

		db = conf.createDatabase(DBNAME_PREFIX + name);
		try {
			cdbIs = new FileInputStream(exportFile);
			db.importData(cdbIs);
		} finally {
			if (cdbIs != null)
				try {
					cdbIs.close();
				} catch (IOException e) {
				}
			exportFile.delete();
		}

		CertificateAuthority authority = new CertificateAuthorityImpl(db, name);
		authorities.put(name, authority);
		authority.addListener(certListener);

		for (CertificateAuthorityListener listener : listeners) {
			try {
				listener.onImportAuthority(authority);
			} catch (Throwable t) {
				logger.error("kraken ca: import authority callback should not throw any exception", t);
			}
		}

		ConfigDatabase ca = conf.ensureDatabase("kraken-ca");
		ConfigCollection metadata = ca.ensureCollection("metadata");
		Map<String, Object> doc = new HashMap<String, Object>();
		doc.put("name", name);
		doc.put("created_at", new Date());
		metadata.add(doc, "kraken-ca", "added " + name + " authority");

		return authority;
	}

	@Override
	public void exportAuthority(String name, OutputStream os) throws IOException {
		CertificateAuthorityFormatter.exportAuthority(getAuthority(name), os);
	}

	private class CertificateListener implements CertEventListener {
		@Override
		public void onRevoked(CertificateAuthority ca, CertificateMetadata cm, RevocationReason reason) {
			for (CertificateAuthorityListener listener : listeners) {
				try {
					listener.onRevokeCert(ca, cm, reason);
				} catch (Throwable t) {
					logger.error("kraken ca: certificate revoke callback should not throw any exception", t);
				}
			}
		}

		@Override
		public void onIssued(CertificateAuthority ca, CertificateMetadata cm) {
			for (CertificateAuthorityListener listener : listeners) {
				try {
					listener.onIssueCert(ca, cm);
				} catch (Throwable t) {
					logger.error("kraken ca: certificate issue callback should not throw any exception", t);
				}
			}
		}
	}
}
