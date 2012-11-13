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

import java.net.URL;

public class PackageRepository {
	private String alias;
	private URL url;

	// for http basic authentication
	private boolean isAuthRequired;
	private String account;
	private String password;

	// for https authentication
	private String trustStoreAlias;
	private String keyStoreAlias;

	public PackageRepository() {
	}

	public static PackageRepository create(String alias, URL url) {
		PackageRepository r = new PackageRepository();
		r.alias = alias;
		r.url = url;
		return r;
	}

	public static PackageRepository createHttpAuth(String alias, URL url, String account, String password) {
		PackageRepository r = create(alias, url);
		r.isAuthRequired = true;
		r.account = account;
		r.password = password;
		return r;
	}

	public static PackageRepository createHttps(String alias, URL url, String trustStoreAlias, String keyStoreAlias) {
		PackageRepository r = create(alias, url);
		r.trustStoreAlias = trustStoreAlias;
		r.keyStoreAlias = keyStoreAlias;
		return r;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public boolean isAuthRequired() {
		return isAuthRequired;
	}

	public void setAuthRequired(boolean isAuthRequired) {
		this.isAuthRequired = isAuthRequired;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTrustStoreAlias() {
		return trustStoreAlias;
	}

	public void setTrustStoreAlias(String trustStoreAlias) {
		this.trustStoreAlias = trustStoreAlias;
	}

	public String getKeyStoreAlias() {
		return keyStoreAlias;
	}

	public void setKeyStoreAlias(String keyStoreAlias) {
		this.keyStoreAlias = keyStoreAlias;
	}

	public boolean isHttps() {
		return url.getProtocol().equals("https");
	}

	@Override
	public String toString() {
		if (isHttps())
			return String
					.format("{alias: %s, url: %s, truststore: %s, keystore: %s}", alias, url, trustStoreAlias, keyStoreAlias);

		if (isAuthRequired)
			return String.format("{alias: %s, url: %s, account: %s, password: %s}", alias, url, account, password);

		return String.format("{alias: %s, url: %s}", alias, url);
	}

	public boolean isLocalFilesystem() {
		return url.getProtocol().equals("file");
	}

}
