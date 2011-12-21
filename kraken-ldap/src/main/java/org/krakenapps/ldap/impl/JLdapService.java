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
package org.krakenapps.ldap.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.util.Base64;

@Component(name = "ldap-service")
@Provides
public class JLdapService implements LdapService {
	private final Logger logger = LoggerFactory.getLogger(JLdapService.class);

	@Requires
	private ConfigService conf;

	private ConfigDatabase getDatabase() {
		return conf.ensureDatabase("kraken-ldap");
	}

	@Override
	public Collection<LdapProfile> getProfiles() {
		return getDatabase().findAll(LdapProfile.class).getDocuments(LdapProfile.class);
	}

	@Override
	public LdapProfile getProfile(String name) {
		Config c = getDatabase().findOne(LdapProfile.class, Predicates.field("name", name));
		if (c == null)
			return null;
		return c.getDocument(LdapProfile.class);
	}

	@Override
	public void createProfile(LdapProfile profile) {
		if (getProfile(profile.getName()) != null)
			throw new IllegalStateException("already exist");
		getDatabase().add(profile);
	}

	@Override
	public void updateProfile(LdapProfile profile) {
		ConfigDatabase db = getDatabase();
		Config c = db.findOne(LdapProfile.class, Predicates.field("name", profile.getName()));
		if (c == null)
			throw new IllegalStateException("not exist");
		db.update(c, profile);
	}

	@Override
	public void removeProfile(String name) {
		ConfigDatabase db = getDatabase();
		Config c = db.findOne(LdapProfile.class, Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("not exist");
		db.remove(c);
	}

	@Override
	public Collection<DomainUserAccount> getDomainUserAccounts(LdapProfile profile) {
		List<DomainUserAccount> accounts = new ArrayList<DomainUserAccount>();

		LDAPConnection lc = openLdapConnection(profile, null);
		try {
			String filter = "(&(userPrincipalName=*))";
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch entry [{}]", entry);
					accounts.add(new DomainUserAccount(entry));
				} catch (LDAPReferralException e) {
				}
			}
		} catch (Exception e) {
			logger.error("kraken-ldap: cannot fetch domain users", e);
			throw new IllegalStateException(e);
		} finally {
			try {
				if (lc != null && lc.isConnected())
					lc.disconnect();
			} catch (LDAPException e) {
				logger.error("kraken ldap: disconnect failed", e);
			}
		}

		return accounts;
	}

	@Override
	public Collection<DomainOrganizationalUnit> getOrganizationUnits(LdapProfile profile) {
		List<DomainOrganizationalUnit> ous = new ArrayList<DomainOrganizationalUnit>();

		LDAPConnection lc = openLdapConnection(profile, null);
		try {
			String filter = "(&(objectClass=organizationalUnit)(!(isCriticalSystemObject=*)))";
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch org unit entry [{}]", entry);
					ous.add(new DomainOrganizationalUnit(entry));
				} catch (LDAPReferralException e) {
				}
			}
		} catch (Exception e) {
			logger.error("kraken-ldap: cannot fetch domain users");
			throw new IllegalStateException(e);
		} finally {
			try {
				if (lc.isConnected())
					lc.disconnect();
			} catch (LDAPException e) {
				logger.error("kraken ldap: disconnect failed", e);
			}
		}

		return ous;
	}

	@Override
	public boolean verifyPassword(LdapProfile profile, String account, String password) {
		return verifyPassword(profile, account, password, 0);
	}

	@Override
	public boolean verifyPassword(LdapProfile profile, String account, String password, int timeout) {
		boolean bindStatus = false;
		if (password == null || password.isEmpty())
			return false;

		LDAPConnection lc = openLdapConnection(profile, timeout);

		try {
			String filter = "(sAMAccountName=" + account + ")";
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false);

			bindStatus = true;

			// query for verification
			LDAPEntry entry = r.next();
			logger.trace("kraken ldap: verify password for {}", entry);

			// try bind
			String dn = entry.getAttribute("distinguishedName").getStringValue();
			lc.bind(LDAPConnection.LDAP_V3, dn, password.getBytes("utf-8"));
			return true;
		} catch (Exception e) {
			if (!bindStatus)
				throw new IllegalArgumentException("check ldap profile: " + profile.getName(), e);

			return false;
		} finally {
			if (lc.isConnected()) {
				try {
					lc.disconnect();
				} catch (LDAPException e) {
					logger.error("kraken ldap: disconnect failed", e);
				}
			}
		}
	}

	@Override
	public KeyStore getKeyStore(String base64Encoded) {
		return getKeyStore(KeyStore.getDefaultType(), base64Encoded);
	}

	@Override
	public KeyStore getKeyStore(String keyStoreType, String base64Encoded) {
		if (base64Encoded == null || base64Encoded.isEmpty())
			return null;

		byte[] b = Base64.decode(base64Encoded);
		return getKeyStore(new ByteArrayInputStream(b));
	}

	@Override
	public KeyStore getKeyStore(InputStream is) {
		return getKeyStore(KeyStore.getDefaultType(), is);
	}

	@Override
	public KeyStore getKeyStore(String keyStoreType, InputStream is) {
		if (is == null)
			return null;

		try {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(is, LdapProfile.DEFAULT_TRUSTSTORE_PASSWORD);
			return keyStore;
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid keystore");
		}
	}

	@Override
	public KeyStore x509ToJKS(String base64Encoded) {
		if (base64Encoded == null || base64Encoded.isEmpty())
			return null;
		byte[] b = Base64.decode(base64Encoded);
		return x509ToJKS(new ByteArrayInputStream(b));
	}

	@Override
	public KeyStore x509ToJKS(InputStream is) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(is);

			KeyStore jks = KeyStore.getInstance("JKS");
			jks.load(null, null);
			jks.setCertificateEntry("mykey", cert);

			return jks;
		} catch (Exception e) {
			throw new IllegalArgumentException("invalid x509");
		}
	}

	private LDAPConnection openLdapConnection(LdapProfile profile, Integer timeout) {
		try {
			if (profile.getTrustStore() != null) {
				SSLContext ctx = SSLContext.getInstance("SSL");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(profile.getTrustStore());
				ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
				LDAPConnection.setSocketFactory(new LDAPJSSESecureSocketFactory(ctx.getSocketFactory()));
			} else
				LDAPConnection.setSocketFactory(new JLDAPSocketFactory(timeout));

			LDAPConnection conn = new LDAPConnection();
			conn.connect(profile.getDc(), profile.getPort());
			conn.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));

			return conn;
		} catch (UnsupportedEncodingException e) {
			logger.error("kraken ldap: JVM unsupported utf-8 encoding");
			throw new IllegalStateException("invalid profile");
		} catch (Exception e) {
			logger.error("kraken ldap: ldap profile [" + profile.getName() + "] connection failed", e);
			throw new IllegalStateException("invalid profile");
		}
	}

	private String buildBaseDN(LdapProfile profile) {
		String domain = profile.getDc();
		StringTokenizer t = new StringTokenizer(domain, ".");
		String dn = "";
		int i = 0;
		while (t.hasMoreTokens()) {
			if (i++ != 0)
				dn += ",";

			dn += "dc=" + t.nextToken();
		}

		return dn;
	}

	private static class JLDAPSocketFactory implements LDAPSocketFactory {
		private Integer timeout;

		public JLDAPSocketFactory(Integer timeout) {
			this.timeout = timeout;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = new Socket(host, port);
			if (timeout != null)
				socket.setSoTimeout(timeout);
			return socket;
		}
	}
}
