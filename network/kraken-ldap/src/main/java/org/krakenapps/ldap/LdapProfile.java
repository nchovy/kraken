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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.DateFormat;
import org.krakenapps.api.FieldOption;
import org.krakenapps.confdb.CollectionName;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.util.Base64;

@CollectionName("profile")
public class LdapProfile {
	public static final int DEFAULT_PORT = LDAPConnection.DEFAULT_PORT; // 389
	public static final int DEFAULT_SSL_PORT = LDAPConnection.DEFAULT_SSL_PORT; // 636
	public static final char[] DEFAULT_TRUSTSTORE_PASSWORD = "kraken".toCharArray();

	public static enum CertificateType {
		X509, JKS;
	}

	@FieldOption(nullable = false)
	private String name;
	private String targetDomain = "localhost";

	@FieldOption(nullable = false)
	private String dc;

	private Integer port;

	@FieldOption(nullable = false)
	private String account;

	@FieldOption(nullable = false)
	private String password;

	@FieldOption(nullable = true)
	private CertificateType type;

	private byte[] trustStore;

	private byte[] x509truststore;

	/**
	 * milliseconds unit
	 */
	private long syncInterval = 0;

	private String baseDn;

	private LdapServerType serverType = LdapServerType.ActiveDirectory;

	private String idAttr;

	private Date lastSync;

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

	public CertificateType getType() {
		return type;
	}

	public void setType(CertificateType type) {
		this.type = type;
	}

	public KeyStore getTrustStore() throws GeneralSecurityException, IOException {
		return getTrustStore(DEFAULT_TRUSTSTORE_PASSWORD);
	}

	public KeyStore getTrustStore(char[] password) throws GeneralSecurityException, IOException {
		if (trustStore == null)
			return null;

		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(new ByteArrayInputStream(trustStore), password);
		return ts;
	}

	public void setX509Certificate(byte[] x509) throws CertificateException {
		if (x509 == null) {
			this.trustStore = null;
			this.x509truststore = null;
			return;
		}

		setX509Certificate(new ByteArrayInputStream(x509));
	}

	public void setX509Certificate(InputStream is) throws CertificateException {
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
		setX509Certificate(cert);
	}

	public void setX509Certificate(X509Certificate cert) throws CertificateEncodingException {
		if (cert == null) {
			this.trustStore = null;
			this.x509truststore = null;
			return;
		}
		this.type = CertificateType.X509;
		this.x509truststore = cert.getEncoded();
	}

	public X509Certificate getX509Certificate() throws KeyStoreException, CertificateException, NoSuchAlgorithmException,
			IOException {
		if (x509truststore != null) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream is = new ByteArrayInputStream(x509truststore);
			return (X509Certificate) cf.generateCertificate(is);
		}

		if (trustStore != null) {
			KeyStore jks = KeyStore.getInstance("JKS");
			jks.load(new ByteArrayInputStream(trustStore), DEFAULT_TRUSTSTORE_PASSWORD);
			return (X509Certificate) jks.getCertificate("mykey");
		}

		return null;
	}

	@Deprecated
	public void setTrustStore(CertificateType type, String base64EncodedCert) throws GeneralSecurityException, IOException {
		setTrustStore(type, new ByteArrayInputStream(Base64.decode(base64EncodedCert)));
	}

	@Deprecated
	public void setTrustStore(CertificateType type, InputStream cert) throws GeneralSecurityException, IOException {
		setTrustStore(type, cert, DEFAULT_TRUSTSTORE_PASSWORD);
	}

	@Deprecated
	public void setTrustStore(CertificateType type, String base64EncodedCert, char[] password) throws GeneralSecurityException,
			IOException {
		setTrustStore(type, new ByteArrayInputStream(Base64.decode(base64EncodedCert)), password);
	}

	@Deprecated
	public void setTrustStore(CertificateType type, InputStream cert, char[] password) throws GeneralSecurityException,
			IOException {
		KeyStore jks = KeyStore.getInstance("JKS");

		if (CertificateType.X509.equals(type)) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate certificate = cf.generateCertificate(cert);

			jks.load(null, null);
			jks.setCertificateEntry("mykey", certificate);
		} else if (CertificateType.JKS.equals(type)) {
			jks.load(cert, password);
		} else {
			throw new IllegalArgumentException("type");
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		jks.store(bos, password);
		this.trustStore = bos.toByteArray();
	}

	@Deprecated
	public void unsetTrustStore() {
		this.trustStore = null;
		this.x509truststore = null;
	}

	public long getSyncInterval() {
		return syncInterval;
	}

	public void setSyncInterval(long syncInterval) {
		this.syncInterval = syncInterval;
	}

	public String getBaseDn() {
		return baseDn;
	}

	public void setBaseDn(String baseDn) {
		this.baseDn = baseDn;
	}

	public LdapServerType getServerType() {
		return serverType;
	}

	public void setServerType(LdapServerType serverType) {
		this.serverType = serverType;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public String getIdAttr() {
		return idAttr;
	}

	public void setIdAttr(String idAttr) {
		this.idAttr = idAttr;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	@Override
	public String toString() {
		return String.format(
				"%s [target=%s, host=%s:%d, account=%s, type=%s, base dn=%s, sync interval=%dms, last sync=%s, ca=%s]", name,
				targetDomain, dc, getPort(), account, serverType, baseDn, syncInterval,
				DateFormat.format("yyyy-MM-dd HH:mm:ss", lastSync), x509truststore != null);
	}

	public Map<String, Object> serialize() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", name);
		m.put("dc", dc);
		m.put("account", account);
		m.put("port", port);
		m.put("password", password);
		m.put("base_dn", baseDn);
		m.put("server_type", serverType.toString());
		m.put("sync_interval", syncInterval);
		m.put("cert_type", type == null ? null : type.toString());
		m.put("trust_store", getX509Certificate() != null);
		return m;
	}
}
