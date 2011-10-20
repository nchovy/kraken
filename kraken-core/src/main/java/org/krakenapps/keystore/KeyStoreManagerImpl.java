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
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreManagerImpl implements KeyStoreManager {
	private final Logger logger = LoggerFactory.getLogger(KeyStoreManagerImpl.class.getName());
	private Preferences prefs;
	private Map<String, KeyStore> keyStoreMap;
	private Map<String, Properties> keyStoreProps;

	public KeyStoreManagerImpl(Preferences prefs) {
		this.prefs = prefs;
		this.keyStoreMap = new ConcurrentHashMap<String, KeyStore>();
		this.keyStoreProps = new ConcurrentHashMap<String, Properties>();

		loadKeyStoreFiles();
	}

	private void loadKeyStoreFiles() {
		Preferences prefs = getKeyStorePreferences();
		try {
			String[] names = prefs.childrenNames();
			for (String alias : names) {
				Preferences p = prefs.node(alias);
				String type = p.get("type", null);

				String pp = p.get("password", null);
				char[] password = null;
				if (pp != null)
					password = pp.toCharArray();
				String path = p.get("path", null);

				FileInputStream fs = null;
				try {
					path = Environment.expandSystemProperties(path);
					fs = new FileInputStream(new File(path));
					registerKeyStore(alias, type, fs, password);
					Properties props = keyStoreProps.get(alias);
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
		} catch (BackingStoreException e) {
			logger.warn("key store load: ", e);
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
		Preferences prefs = getKeyStorePreferences();
		try {
			if (!prefs.nodeExists(alias)) {
				return null;
			}

			Preferences p = prefs.node(alias);
			String type = p.get("type", null);
			String path = p.get("path", null);
			String passwd = p.get("password", null);
			char[] password = null;
			if (passwd != null)
				password = passwd.toCharArray();

			KeyStore ks = KeyStore.getInstance(type);
			if (ks == null)
				return null;

			is = new FileInputStream(new File(path));
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

		Preferences prefs = getKeyStorePreferences();
		try {
			if (prefs.nodeExists(alias))
				throw new RuntimeException("duplicated key store alias");
		} catch (BackingStoreException e1) {
			throw new RuntimeException("node exists check failed", e1);
		}

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

		Preferences p = prefs.node(alias);
		p.put("alias", alias);
		p.put("type", type);
		p.put("path", file.getAbsolutePath());
		if (password != null)
			p.put("password", new String(password));

		try {
			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException("failed to save keystore preference", e);
		}
	}

	@Deprecated
	@Override
	public void registerKeyStore(String alias, String type, File file, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		Preferences prefs = getKeyStorePreferences();
		try {
			if (prefs.nodeExists(alias))
				throw new RuntimeException("duplicated key store alias");
		} catch (BackingStoreException e1) {
			throw new RuntimeException("node exists check failed", e1);
		}

		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
			registerKeyStore(alias, type, fs, password);

			// add file path property
			Properties props = keyStoreProps.get(alias);
			props.put("path", file.getAbsolutePath());
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
				}
			}
		}

		Preferences p = prefs.node(alias);
		p.put("alias", alias);
		p.put("type", type);
		p.put("path", file.getAbsolutePath());
		if (password != null)
			p.put("password", new String(password));

		try {
			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			throw new RuntimeException("failed to save keystore preference", e);
		}
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
		Preferences prefs = getKeyStorePreferences();
		try {
			if (!prefs.nodeExists(alias))
				return;

			Preferences p = prefs.node(alias);
			p.removeNode();
			prefs.flush();
			prefs.sync();

			keyStoreMap.remove(alias);
		} catch (BackingStoreException e) {
			throw new RuntimeException("failed to unregister key store", e);
		}
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

	private Preferences getKeyStorePreferences() {
		return prefs.node("/keystore");
	}

	private char[] getKeyStorePassword(String alias) {
		Preferences prefs = getKeyStorePreferences();
		try {
			if (!prefs.nodeExists(alias))
				return null;
		} catch (BackingStoreException e) {
		}

		Preferences p = prefs.node(alias);
		String password = p.get("password", null);
		return password.toCharArray();
	}
}
