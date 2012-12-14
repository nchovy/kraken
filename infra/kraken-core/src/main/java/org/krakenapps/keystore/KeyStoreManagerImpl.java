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
package org.krakenapps.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.api.Environment;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreManagerImpl implements KeyStoreManager {
	private final Logger logger = LoggerFactory.getLogger(KeyStoreManagerImpl.class.getName());
	private ConfigService conf;
	private Preferences prefs;
	private Map<String, KeyStore> keyStoreMap;
	private Map<String, Properties> keyStoreProps;

	public KeyStoreManagerImpl(ConfigService conf, Preferences prefs) {
		this.conf = conf;
		this.prefs = prefs;
		this.keyStoreMap = new ConcurrentHashMap<String, KeyStore>();
		this.keyStoreProps = new ConcurrentHashMap<String, Properties>();

		migrateKeyStores();
		loadKeyStoreFiles();
	}

	private void loadKeyStoreFiles() {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		ConfigIterator it = db.findAll(KeyStoreConfig.class);

		for (KeyStoreConfig c : it.getDocuments(KeyStoreConfig.class)) {
			String type = c.getType();

			String pp = c.getPassword();
			char[] password = null;
			if (pp != null)
				password = pp.toCharArray();
			String path = c.getPath();

			FileInputStream fs = null;
			try {
				fs = new FileInputStream(new File(Environment.expandSystemProperties(path)));
				registerKeyStore(c.getAlias(), type, fs, password);
				Properties props = keyStoreProps.get(c.getAlias());
				props.put("path", path);
			} catch (Exception e) {
				logger.warn("load key store error: ", e);
			} finally {
				if (fs != null) {
					try {
						fs.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	@Override
	public Collection<String> getKeyStoreNames() {
		return keyStoreMap.keySet();
	}

	@Override
	public Properties getKeyStoreProperties(String alias) {
		return keyStoreProps.get(alias);
	}

	@Override
	public KeyStore getKeyStore(String alias) {
		if (alias == null)
			return null;

		FileInputStream is = null;
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(KeyStoreConfig.class, Predicates.field("alias", alias));
		if (c == null)
			return null;

		try {
			KeyStoreConfig ksc = c.getDocument(KeyStoreConfig.class);
			String type = ksc.getType();
			String path = ksc.getPath();
			String passwd = ksc.getPassword();
			char[] password = null;
			if (passwd != null)
				password = passwd.toCharArray();

			KeyStore ks = KeyStore.getInstance(type);
			if (ks == null)
				return null;

			is = new FileInputStream(new File(Environment.expandSystemProperties(path)));
			ks.load(is, password);
			return ks;
		} catch (Exception e) {
			logger.warn("getKeyStore() error: ", e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
		}

		return null;
	}

	@Override
	public void registerKeyStore(String alias, String type, String path, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		File file = new File(Environment.expandSystemProperties(path));
		if (!file.exists())
			throw new FileNotFoundException();

		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(KeyStoreConfig.class, Predicates.field("alias", alias));
		if (c != null)
			throw new RuntimeException("duplicated key store alias");

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
			registerKeyStore(alias, type, fs, password);

			// add file path property
			Properties props = keyStoreProps.get(alias);
			props.put("path", path);
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
				}
			}
		}

		KeyStoreConfig ksc = new KeyStoreConfig();
		ksc.setAlias(alias);
		ksc.setType(type);
		ksc.setPath(path);
		ksc.setPassword(new String(password));
		db.add(ksc);
	}

	@Deprecated
	@Override
	public void registerKeyStore(String alias, String type, File file, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		registerKeyStore(alias, type, file.getAbsolutePath(), password);
	}

	@Override
	public void registerKeyStore(String alias, String type, InputStream is, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		if (keyStoreMap.containsKey(alias))
			throw new RuntimeException("duplicated key store alias");

		Properties props = newProperties(alias, type, password);
		keyStoreProps.put(alias, props);

		KeyStore ks = KeyStore.getInstance(type);
		ks.load(is, password);
		keyStoreMap.put(alias, ks);
	}

	private Properties newProperties(String alias, String type, char[] password) {
		Properties props = new Properties();
		props.put("alias", alias);
		props.put("type", type);
		if (password != null)
			props.put("password", new String(password));
		return props;
	}

	@Override
	public void unregisterKeyStore(String alias) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(KeyStoreConfig.class, Predicates.field("alias", alias));

		if (c == null)
			throw new IllegalStateException("keystore alias [" + alias + "] not found");

		c.remove();

		keyStoreMap.remove(alias);
	}

	@Override
	public KeyManagerFactory getKeyManagerFactory(String alias, String algorithm) throws NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyStoreException {
		KeyStore keystore = getKeyStore(alias);
		if (keystore == null)
			return null;

		char[] password = getKeyStorePassword(alias);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
		kmf.init(keystore, password);
		return kmf;
	}

	@Override
	public TrustManagerFactory getTrustManagerFactory(String alias, String algorithm) throws KeyStoreException,
			NoSuchAlgorithmException {
		KeyStore keystore = getKeyStore(alias);
		if (keystore == null)
			return null;

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
		tmf.init(keystore);
		return tmf;
	}

	private char[] getKeyStorePassword(String alias) {
		ConfigDatabase db = conf.ensureDatabase("kraken-core");
		Config c = db.findOne(KeyStoreConfig.class, Predicates.field("alias", alias));
		if (c == null)
			return null;

		KeyStoreConfig ksc = c.getDocument(KeyStoreConfig.class);
		if (ksc.getPassword() == null)
			return null;

		return ksc.getPassword().toCharArray();
	}

	private void migrateKeyStores() {
		Preferences prefs = getKeyStorePreferences();
		try {
			String[] names = prefs.childrenNames();
			if (names.length == 0)
				return;

			for (String alias : names) {
				Preferences p = prefs.node(alias);
				String type = p.get("type", null);

				String pp = p.get("password", null);
				char[] password = null;
				if (pp != null)
					password = pp.toCharArray();
				String path = p.get("path", null);

				try {
					registerKeyStore(alias, type, path, password);
				} catch (Exception e) {
					logger.warn("kraken core: cannot migrate keystore entry", e);
				}
				p.removeNode();
			}

			prefs.flush();
			prefs.sync();
		} catch (BackingStoreException e) {
			logger.warn("kraken core: cannot migrate keystore preferences", e);
		}

		keyStoreMap.clear();
		keyStoreProps.clear();
	}

	private Preferences getKeyStorePreferences() {
		return prefs.node("/keystore");
	}

}
