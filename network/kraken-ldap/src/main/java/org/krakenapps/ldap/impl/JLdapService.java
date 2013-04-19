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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
import org.krakenapps.ldap.LdapOrgUnit;
import org.krakenapps.ldap.LdapUser;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapServerType;
import org.krakenapps.ldap.LdapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;

@Component(name = "ldap-service")
@Provides
public class JLdapService implements LdapService {
	private static final int DEFAULT_TIMEOUT = 5000;
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
	public Collection<LdapUser> getUsers(LdapProfile profile) {
		List<LdapUser> users = new ArrayList<LdapUser>();

		LDAPConnection lc = openLdapConnection(profile, null);
		int count = 0;
		try {
			String filter = "(&(userPrincipalName=*))";
			if (profile.getServerType() != LdapServerType.ActiveDirectory)
				filter = "(&(objectClass=inetOrgPerson))";

			String idAttr = profile.getIdAttr() == null ? "uid" : profile.getIdAttr();
			LDAPSearchConstraints cons = new LDAPSearchConstraints();
			cons.setTimeLimit(20000);
			cons.setMaxResults(0);
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false, cons);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch entry [{}]", entry.getDN());
					users.add(new LdapUser(entry, idAttr));
					count++;
				} catch (LDAPReferralException e) {
				}
			}

			logger.info("kraken-ldap: profile [{}], total {} ldap entries", profile.getName(), count);
		} catch (LDAPException e) {
			if (e.getResultCode() == LDAPException.SIZE_LIMIT_EXCEEDED)
				logger.error("kraken-ldap: profile [{}], size limit, fetched only {} ldap entries", profile.getName(), count);
			throw new IllegalStateException(e);
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

		return users;
	}

	@Override
	public LdapUser findUser(LdapProfile profile, String uid) {
		LDAPConnection lc = openLdapConnection(profile, null);
		try {
			String filter = "(&(sAMAccountName=" + uid + "))";
			if (profile.getServerType() != LdapServerType.ActiveDirectory)
				filter = buildUserFilter(profile, uid);

			String idAttr = profile.getIdAttr() == null ? "uid" : profile.getIdAttr();
			LDAPSearchConstraints cons = new LDAPSearchConstraints();
			cons.setTimeLimit(20000);
			cons.setMaxResults(1);
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false, cons);
			if (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					if (logger.isDebugEnabled())
						logger.debug("kraken-ldap: fetch entry [{}]", entry);
					return new LdapUser(entry, idAttr);
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

		return null;
	}

	@Override
	public Collection<LdapOrgUnit> getOrgUnits(LdapProfile profile) {
		List<LdapOrgUnit> ous = new ArrayList<LdapOrgUnit>();

		LDAPConnection lc = openLdapConnection(profile, null);
		try {
			String filter = "(objectClass=organizationalUnit)";
			LDAPSearchResults r = lc.search(buildBaseDN(profile), LDAPConnection.SCOPE_SUB, filter, null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch org unit entry [{}]", entry.getDN());
					ous.add(new LdapOrgUnit(entry));
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
	public boolean verifyPassword(LdapProfile profile, String uid, String password) {
		return verifyPassword(profile, uid, password, 0);
	}

	@Override
	public boolean verifyPassword(LdapProfile profile, String uid, String password, int timeout) {
		boolean bindStatus = false;
		if (password == null || password.isEmpty())
			return false;

		LDAPConnection lc = openLdapConnection(profile, timeout);

		try {
			String filter = null;
			if (profile.getServerType() == LdapServerType.ActiveDirectory) {
				filter = "(sAMAccountName=" + uid + ")";
			} else {
				filter = buildUserFilter(profile, uid);
			}

			String baseDn = buildBaseDN(profile);
			LDAPSearchResults r = lc.search(baseDn, LDAPConnection.SCOPE_SUB, filter, null, false);

			bindStatus = true;

			// query for verification
			LDAPEntry entry = r.next();
			logger.trace("kraken ldap: verify password for {}", entry);

			// try bind
			logger.trace("kraken ldap: trying to bind using dn [{}]", entry.getDN());
			lc.bind(LDAPConnection.LDAP_V3, entry.getDN(), password.getBytes("utf-8"));
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

	private String buildUserFilter(LdapProfile profile, String uid) {
		String filter;
		String idAttr = "uid";
		if (profile.getIdAttr() != null)
			idAttr = profile.getIdAttr();

		filter = "(" + idAttr + "=" + uid + ")";
		return filter;
	}

	@Override
	public void testLdapConnection(LdapProfile profile, Integer timeout) {
		if (timeout == null)
			timeout = DEFAULT_TIMEOUT;

		LDAPConnection conn = null;
		try {
			conn = openLdapConnection(profile, timeout);
		} finally {
			if (conn != null && conn.isConnected())
				try {
					conn.disconnect();
				} catch (LDAPException e) {
				}
		}

	}

	@Override
	public void changePassword(LdapProfile profile, String uid, String newPassword) {
		changePassword(profile, uid, newPassword, DEFAULT_TIMEOUT);
	}

	@Override
	public void changePassword(LdapProfile profile, String uid, String newPassword, int timeout) {

		// newPassword null check
		if (newPassword == null || newPassword.isEmpty())
			throw new IllegalArgumentException("password should be not null and not empty");

		// connection server
		LDAPConnection lc = openLdapConnection(profile, timeout);

		try {
			// set filter
			String filter = "(sAMAccountName=" + uid + ")";
			if (profile.getServerType() != LdapServerType.ActiveDirectory)
				filter = buildUserFilter(profile, uid);

			String baseDn = buildBaseDN(profile);

			LDAPSearchResults r = lc.search(baseDn, LDAPConnection.SCOPE_SUB, filter, null, false);

			// query for verification
			LDAPEntry entry = r.next();
			logger.trace("kraken ldap: change password for {}", entry);

			// set mod
			LDAPModification mod = null;
			if (profile.getServerType() == LdapServerType.ActiveDirectory) {
				// ActiveDirectory - newPassword enclosed in quotation marks and
				// UTF-16LE encoding
				byte[] quotedPasswordBytes = null;

				String tmpPassword = ldapPasswordEscape(newPassword);

				try {
					String quotedPassword = '"' + tmpPassword + '"';
					quotedPasswordBytes = quotedPassword.getBytes("UTF-16LE");
				} catch (UnsupportedEncodingException e) {
					throw new IllegalStateException(e);
				}

				mod = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute("unicodePwd", quotedPasswordBytes));
				logger.debug("kraken ldap: active directory modify request [{}] for dn [{}]", mod.toString(), entry.getDN());
			} else {
				mod = new LDAPModification(LDAPModification.REPLACE, new LDAPAttribute("userPassword", newPassword));
				logger.debug("kraken ldap: sun one modify request [{}] for dn [{}]", mod.toString(), entry.getDN());
			}

			// modify
			lc.modify(entry.getDN(), mod);
		} catch (LDAPException e) {
			throw new IllegalStateException("cannot change password, profile=" + profile.getName() + ", uid=" + uid, e);
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

	public static String ldapPasswordEscape(String s) {

		for (int i = 0; i < s.length(); i++) {
			s.replaceAll("\"", "\\\"");
		}

		return s;
	}

	@Override
	public LDAPConnection openLdapConnection(LdapProfile profile, Integer timeout) {
		if (profile.getDc() == null)
			throw new IllegalArgumentException("ldap domain controller should be not null");

		if (profile.getPort() == null)
			throw new IllegalArgumentException("ldap port should be not null");

		if (profile.getAccount() == null)
			throw new IllegalArgumentException("ldap account should be not null");

		if (profile.getPassword() == null)
			throw new IllegalArgumentException("ldap password should be not null");

		try {
			X509Certificate cert = profile.getX509Certificate();
			KeyStore ks = null;
			if (cert != null) {
				ks = KeyStore.getInstance("JKS");
				ks.load(null, null);
				ks.setCertificateEntry("mykey", cert);
			}

			if (ks != null) {
				SSLContext ctx = SSLContext.getInstance("SSL");
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(ks);
				ctx.init(null, tmf.getTrustManagers(), new SecureRandom());
				LDAPConnection.setSocketFactory(new LDAPJSSESecureSocketFactory(ctx.getSocketFactory()));
			} else
				LDAPConnection.setSocketFactory(new JLDAPSocketFactory(timeout));

			logger.trace("kraken ldap: connect to {}:{}, user [{}]",
					new Object[] { profile.getDc(), profile.getPort(), profile.getAccount() });

			LDAPConnection conn = new LDAPConnection();
			conn.connect(profile.getDc(), profile.getPort());
			conn.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));

			return conn;
		} catch (UnsupportedEncodingException e) {
			logger.error("kraken ldap: JVM unsupported utf-8 encoding");
			throw new IllegalStateException("invalid profile [" + profile.getName() + "]", e);
		} catch (Exception e) {
			logger.error("kraken ldap: ldap profile [" + profile.getName() + "] connection failed", e);
			throw new IllegalStateException("invalid profile [" + profile.getName() + "]", e);
		}
	}

	private String buildBaseDN(LdapProfile profile) {
		if (profile.getBaseDn() != null)
			return profile.getBaseDn();

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
