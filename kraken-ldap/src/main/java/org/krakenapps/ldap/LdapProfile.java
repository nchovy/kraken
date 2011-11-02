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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.novell.ldap.LDAPConnection;

public class LdapProfile {
	public static final int DEFAULT_SYNC_INTERVAL = 10 * 60 * 1000; // 10 minute
	public static final int DEFAULT_PORT = LDAPConnection.DEFAULT_PORT; // 389
	public static final int DEFAULT_SSL_PORT = LDAPConnection.DEFAULT_SSL_PORT; // 636
	public static final char[] DEFAULT_TRUSTSTORE_PASSWORD = "kraken".toCharArray();

	private String name;
	private String dc;
	private int port;
	private String account;
	private String password;
	private KeyStore trustStore;
	private int syncInterval = DEFAULT_SYNC_INTERVAL;
	private Date lastSync = null;

	public LdapProfile(String name, String dc, String account, String password) {
		this(name, dc, DEFAULT_PORT, account, password, null, DEFAULT_SYNC_INTERVAL);
	}

	public LdapProfile(String name, String dc, String account, String password, KeyStore trustStore) {
		this(name, dc, (trustStore != null) ? DEFAULT_SSL_PORT : DEFAULT_PORT, account, password, trustStore,
				DEFAULT_SYNC_INTERVAL);
	}

	public LdapProfile(String name, String dc, int port, String account, String password) {
		this(name, dc, port, account, password, null, DEFAULT_SYNC_INTERVAL);
	}

	public LdapProfile(String name, String dc, int port, String account, String password, KeyStore trustStore) {
		this(name, dc, port, account, password, trustStore, DEFAULT_SYNC_INTERVAL);
	}

	public LdapProfile(String name, String dc, int port, String account, String password, int syncInterval) {
		this(name, dc, port, account, password, null, syncInterval);
	}

	public LdapProfile(String name, String dc, int port, String account, String password, KeyStore trustStore,
			int syncInterval) {
		this(name, dc, port, account, password, trustStore, syncInterval, null);
	}

	public LdapProfile(String name, String dc, int port, String account, String password, KeyStore trustStore,
			int syncInterval, Date lastSync) {
		this.name = name;
		this.dc = dc;
		this.port = port;
		this.account = account;
		this.password = password;
		this.trustStore = trustStore;
		this.syncInterval = syncInterval;
		this.lastSync = lastSync;
	}

	public String getName() {
		return name;
	}

	public String getDc() {
		return dc;
	}

	public int getPort() {
		return port;
	}

	public String getAccount() {
		return account;
	}

	public String getPassword() {
		return password;
	}

	public KeyStore getTrustStore() {
		return trustStore;
	}

	public int getSyncInterval() {
		return syncInterval;
	}

	public Date getLastSync() {
		return lastSync;
	}

	@Override
	public String toString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return String.format("name=%s, host=%s:%d, account=%s, sync interval=%dms, last sync=%s, keystore=%s", name,
				dc, port, account, syncInterval, (lastSync == null) ? "none" : dateFormat.format(lastSync),
				(trustStore != null));
	}
}
