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

/**
 * @author xeraph
 * 
 */
public class BundleRepository {
	private String name;
	private URL url;
	private boolean isAuthRequired;
	private String account;
	private String password;
	private int priority;

	private String trustStoreAlias;
	private String keyStoreAlias;
	
	public BundleRepository(String name, URL url) {
		this(name, url, 0);
	}

	public BundleRepository(String name, URL url, int priority) {
		this.name = name;
		this.url = url;
		this.priority = priority;
	}

	public boolean isHttps() {
		return url.getProtocol().equals("https");
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return this.priority;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
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

	@Override
	public String toString() {
		return url.toString();
	}

}
