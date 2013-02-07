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
package org.krakenapps.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public interface KeyStoreManager {
	/**
	 * Return all key store names
	 */
	Collection<String> getKeyStoreNames();

	/**
	 * Return all properties of the key store.
	 */
	Properties getKeyStoreProperties(String alias);

	/**
	 * Find and return key store.
	 */
	KeyStore getKeyStore(String name);

	/**
	 * Register file keystore. it is preserved permanently.
	 * 
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 */
	void registerKeyStore(String alias, String type, String path, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException;

	@Deprecated
	void registerKeyStore(String name, String type, File file, char[] password) throws KeyStoreException,
			FileNotFoundException, NoSuchAlgorithmException, CertificateException, IOException;

	/**
	 * Register generic keystore. it is not preserved when kraken core reboots.
	 * 
	 * @throws IOException
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 */
	void registerKeyStore(String name, String type, InputStream is, char[] password) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException;

	/**
	 * Unregister keystore.
	 */
	void unregisterKeyStore(String name);

	/**
	 * Return new key manager factory using specified alias.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws UnrecoverableKeyException
	 */
	KeyManagerFactory getKeyManagerFactory(String name, String algorithm) throws NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyStoreException;

	/**
	 * Return new trust manager factory using specified alias.
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 */
	TrustManagerFactory getTrustManagerFactory(String name, String algorithm) throws KeyStoreException,
			NoSuchAlgorithmException;
}
