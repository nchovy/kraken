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
package org.krakenapps.webconsole;

import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.api.KeyStoreManager;

public class WebSocketServerParams {
	private InetSocketAddress listen;
	private SSLContext sslContext;
	private TrustManagerFactory tmf;
	private KeyManagerFactory kmf;
	private boolean isSsl;
	private String keyAlias;
	private String trustAlias;
	private int maxContentLength = Integer.MAX_VALUE; // default 2G

	public WebSocketServerParams(InetSocketAddress listen) {
		this.listen = listen;
	}

	public WebSocketServerParams(InetSocketAddress listen, KeyStoreManager keyStoreManager, String keyAlias,
			String trustAlias) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		this.listen = listen;

		TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustAlias, "SunX509");
		KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyAlias, "SunX509");

		this.isSsl = true;
		this.keyAlias = keyAlias;
		this.trustAlias = trustAlias;
		this.kmf = kmf;
		this.tmf = tmf;

		try {
			sslContext = SSLContext.getInstance("SSL");
			TrustManager[] trustManagers = null;
			KeyManager[] keyManagers = null;
			if (tmf != null)
				trustManagers = tmf.getTrustManagers();
			if (kmf != null)
				keyManagers = kmf.getKeyManagers();

			sslContext.init(keyManagers, trustManagers, new SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}

	public InetSocketAddress getListenAddress() {
		return listen;
	}

	public boolean isSsl() {
		return isSsl;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public TrustManagerFactory getTrustManagerFactory() {
		return tmf;
	}

	public KeyManagerFactory getKeyManagerFactory() {
		return kmf;
	}

	public String getKeyAlias() {
		return keyAlias;
	}

	public String getTrustAlias() {
		return trustAlias;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}
}
