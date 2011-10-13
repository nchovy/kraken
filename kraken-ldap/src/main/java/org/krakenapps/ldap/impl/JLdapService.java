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
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;

@Component(name = "ldap-service")
@Provides
public class JLdapService implements LdapService {
	private final Logger logger = LoggerFactory.getLogger(JLdapService.class.getName());

	@Requires
	private PreferencesService prefsvc;

	@Override
	public Collection<LdapProfile> getProfiles() {
		List<LdapProfile> profiles = new ArrayList<LdapProfile>();
		try {
			Preferences root = prefsvc.getSystemPreferences();
			for (String name : root.childrenNames()) {
				Preferences p = root.node(name);
				String dc = p.get("dc", null);
				String account = p.get("account", null);
				String password = p.get("password", null);
				int syncInterval = p.getInt("sync_interval", 10 * 60 * 1000);
				Date lastSync = new Date(p.getLong("last_sync", 0));

				LdapProfile profile = new LdapProfile(name, dc, LdapProfile.DEFAULT_PORT, account, password, syncInterval,
						lastSync);
				profiles.add(profile);
			}
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}

		return profiles;
	}

	@Override
	public void createProfile(LdapProfile profile) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (root.nodeExists(profile.getName()))
				throw new IllegalStateException("duplicated profile name");

			setPreference(profile, root);

		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	@Override
	public void updateProfile(LdapProfile profile) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(profile.getName()))
				throw new IllegalStateException("profile not found");

			setPreference(profile, root);

		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	private void setPreference(LdapProfile profile, Preferences root) throws BackingStoreException {
		Preferences p = root.node(profile.getName());
		p.put("dc", profile.getDc());
		p.put("account", profile.getAccount());
		p.put("password", profile.getPassword());
		p.putInt("sync_interval", profile.getSyncInterval());
		p.putLong("last_sync", profile.getLastSync() == null ? 0 : profile.getLastSync().getTime());

		p.flush();
		p.sync();
	}

	@Override
	public void removeProfile(String name) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(name))
				throw new IllegalStateException("profile not found");

			root.node(name).removeNode();

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	@Override
	public LdapProfile getProfile(String name) {
		try {
			Preferences root = prefsvc.getSystemPreferences();
			if (!root.nodeExists(name))
				return null;

			Preferences p = root.node(name);
			String dc = p.get("dc", null);
			String account = p.get("account", null);
			String password = p.get("password", null);

			return new LdapProfile(name, dc, account, password);
		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
	}

	@Override
	public Collection<DomainUserAccount> getDomainUserAccounts(LdapProfile profile) {
		List<DomainUserAccount> accounts = new ArrayList<DomainUserAccount>();

		LDAPConnection lc = new LDAPConnection();
		try {
			lc.connect(profile.getDc(), profile.getPort());

			lc.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));
			LDAPSearchResults r = lc.search(buildBaseDN(profile.getDc()), LDAPConnection.SCOPE_SUB, "(&(userPrincipalName=*))",
					null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch entry [{}]", entry);

					DomainUserAccount account = parseUserAccount(entry);
					if (account != null)
						accounts.add(account);
				} catch (LDAPReferralException e) {
					logger.trace("kraken-ldap: skip referer", e);
				}
			}

		} catch (Exception e) {
			logger.error("kraken-ldap: cannot fetch domain users", e);
			throw new IllegalStateException(e);
		} finally {
			try {
				if (lc.isConnected())
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

		LDAPConnection lc = new LDAPConnection();
		try {
			lc.connect(profile.getDc(), LDAPConnection.DEFAULT_PORT);

			lc.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));
			LDAPSearchResults r = lc.search(buildBaseDN(profile.getDc()), LDAPConnection.SCOPE_SUB,
					"(&(objectClass=organizationalUnit)(!(isCriticalSystemObject=*)))", null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
					logger.debug("kraken-ldap: fetch org unit entry [{}]", entry);

					DomainOrganizationalUnit ou = parseOrganizationUnit(entry);
					if (ou != null)
						ous.add(ou);
				} catch (LDAPReferralException e) {
					logger.trace("kraken-ldap: skip referer", e);
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

		LDAPConnection.setSocketFactory(new JLdapSocketFactory(timeout));
		LDAPConnection lc = new LDAPConnection();

		try {
			lc.connect(profile.getDc(), profile.getPort());

			lc.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));
			LDAPSearchResults r = lc.search(buildBaseDN(profile.getDc()), LDAPConnection.SCOPE_SUB, "(sAMAccountName=" + account
					+ ")", null, false);

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
			if (lc.isConnected())
				try {
					lc.disconnect();
				} catch (LDAPException e) {
					logger.error("kraken ldap: disconnect failed", e);
				}
		}
	}

	private String buildBaseDN(String domain) {
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

	private DomainUserAccount parseUserAccount(LDAPEntry entry) {
		try {
			LDAPAttributeSet attrs = entry.getAttributeSet();

			DomainUserAccount d = new DomainUserAccount();
			d.setAccountName(getString(attrs, "sAMAccountName"));
			d.setDomainAdmin(getInt(attrs, "adminCount") > 0);
			String dialin = getString(attrs, "msNPAllowDialin");
			if (dialin != null && dialin.equals("TRUE"))
				d.setAllowDialIn(true);

			d.setLoginCount(getInt(attrs, "loginCount"));
			// d.setMemberOf(memberOf);
			d.setDistinguishedName(getString(attrs, "distinguishedName"));
			d.setUserPrincipalName(getString(attrs, "userPrincipalName"));
			d.setDisplayName(getString(attrs, "displayName"));
			d.setSurname(getString(attrs, "sn"));
			d.setGivenName(getString(attrs, "givenName"));
			d.setTitle(getString(attrs, "title"));
			d.setDepartment(getString(attrs, "department"));
			d.setMail(getString(attrs, "mail"));
			d.setMobile(getString(attrs, "mobile"));
			d.setLastLogon(new Date(getLong(attrs, "lastLogon") / 10000L - 11644473600000L));
			d.setWhenCreated(getDate(attrs, "whenCreated"));
			d.setLastPasswordChange(getTimestamp(attrs, "pwdLastSet"));
			d.setAccountExpires(getTimestamp(attrs, "accountExpires"));

			for (String token : d.getDistinguishedName().split("(?<!\\\\),")) {
				String attr = token.split("=")[0];
				String value = token.split("=")[1];
				if (attr.equals("OU")) {
					d.setOrganizationUnitName(value);
					break;
				}
			}

			return d;
		} catch (Exception e) {
			logger.trace("kraken ldap: cannot parse entry", e);
			return null;
		}
	}

	private DomainOrganizationalUnit parseOrganizationUnit(LDAPEntry entry) {
		try {
			LDAPAttributeSet attrs = entry.getAttributeSet();

			DomainOrganizationalUnit ou = new DomainOrganizationalUnit();
			ou.setDistinguishedName(getString(attrs, "distinguishedName"));
			ou.setName(getString(attrs, "name"));
			ou.setWhenCreated(getDate(attrs, "whenCreated"));
			ou.setWhenChanged(getDate(attrs, "whenChanged"));

			return ou;
		} catch (Exception e) {
			logger.trace("kraken ldap: cannot parse entry", e);
			return null;
		}
	}

	private int getInt(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? 0 : Integer.parseInt(attr.getStringValue());
	}

	private long getLong(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? 0L : Long.parseLong(attr.getStringValue());
	}

	private Date getDate(LDAPAttributeSet attrs, String attrName) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			LDAPAttribute attr = attrs.getAttribute(attrName);
			return (attr == null) ? null : dateFormat.parse(attr.getStringValue());
		} catch (Exception e) {
			return null;
		}
	}

	private Date getTimestamp(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : new Date(Long.parseLong(attr.getStringValue()));
	}

	private String getString(LDAPAttributeSet attrs, String attrName) {
		LDAPAttribute attr = attrs.getAttribute(attrName);
		return (attr == null) ? null : attr.getStringValue();
	}

	private static class JLdapSocketFactory implements LDAPSocketFactory {
		private int timeout;

		public JLdapSocketFactory(int timeout) {
			this.timeout = timeout;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
			Socket socket = new Socket(host, port);
			socket.setSoTimeout(timeout);
			return socket;
		}
	}
}
