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
package org.krakenapps.ldap;

import java.security.KeyStore;
import java.util.Date;

import org.krakenapps.api.DateFormat;
import org.krakenapps.confdb.CollectionName;

import com.novell.ldap.LDAPConnection;

@CollectionName("profile")
public class LdapProfile {
	public static final long DEFAULT_SYNC_INTERVAL = 10 * 60 * 1000; // 10minute
	public static final int DEFAULT_PORT = LDAPConnection.DEFAULT_PORT; // 389
	public static final int DEFAULT_SSL_PORT = LDAPConnection.DEFAULT_SSL_PORT; // 636
	public static final char[] DEFAULT_TRUSTSTORE_PASSWORD = "kraken".toCharArray();

	private String name;
	private String targetDomain = "localhost";
	private String dc;
	private Integer port;
	private String account;
	private String password;
	private KeyStore trustStore;
	private long syncInterval = DEFAULT_SYNC_INTERVAL;
	private Date lastSync = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTargetDomain() {
		return targetDomain;
	}

	public void setTargetDomain(String targetDomain) {
		this.targetDomain = targetDomain;
	}

	public String getDc() {
		return dc;
	}

	public void setDc(String dc) {
		this.dc = dc;
	}

	public Integer getPort() {
		if (port != null)
			return port;
		return (trustStore == null) ? DEFAULT_PORT : DEFAULT_SSL_PORT;
	}

	public void setPort(Integer port) {
		this.port = port;
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

	public KeyStore getTrustStore() {
		return trustStore;
	}

	public void setTrustStore(KeyStore trustStore) {
		this.trustStore = trustStore;
	}

	public long getSyncInterval() {
		return syncInterval;
	}

	public void setSyncInterval(long syncInterval) {
		this.syncInterval = syncInterval;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	@Override
	public String toString() {
		return String.format("name=%s, target=%s, host=%s:%d, account=%s, sync interval=%dms, last sync=%s, keystore=%s", name,
				targetDomain, dc, port, account, syncInterval, DateFormat.format("yyyy-MM-dd HH:mm:ss", lastSync), (trustStore != null));
	}
}
