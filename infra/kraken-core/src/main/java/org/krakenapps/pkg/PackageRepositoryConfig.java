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
package org.krakenapps.pkg;

import org.krakenapps.api.FieldOption;
import org.krakenapps.api.PackageRepository;
import org.krakenapps.confdb.CollectionName;

@CollectionName("pkg_repos")
public class PackageRepositoryConfig {

	@FieldOption(nullable = false)
	private String alias;

	@FieldOption(nullable = false)
	private String url;

	@FieldOption(nullable = false)
	private boolean isAuthRequired;

	@FieldOption(nullable = true)
	private String account;

	@FieldOption(nullable = true)
	private String password;

	@FieldOption(nullable = true)
	private String trustStoreAlias;

	@FieldOption(nullable = true)
	private String keyStoreAlias;

	public PackageRepositoryConfig() {
	}

	public PackageRepositoryConfig(PackageRepository repo) {
		this.alias = repo.getAlias();
		this.url = repo.getUrl().toString();
		this.isAuthRequired = repo.isAuthRequired();
		this.account = repo.getAccount();
		this.password = repo.getPassword();
		this.trustStoreAlias = repo.getTrustStoreAlias();
		this.keyStoreAlias = repo.getKeyStoreAlias();
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
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
}
