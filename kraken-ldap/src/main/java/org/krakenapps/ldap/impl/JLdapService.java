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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.LdapOrganizationalUnitApi;
import org.krakenapps.dom.api.OrganizationParameterApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.LdapOrganizationalUnit;
import org.krakenapps.dom.model.OrganizationParameter;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.User;
import org.krakenapps.ldap.DomainOrganizationalUnit;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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

				LdapProfile profile = new LdapProfile(name, dc, account, password);
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

			Preferences p = root.node(profile.getName());
			p.put("dc", profile.getDc());
			p.put("account", profile.getAccount());
			p.put("password", profile.getPassword());

			p.flush();
			p.sync();

		} catch (BackingStoreException e) {
			throw new IllegalStateException("io error", e);
		}
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
			lc.connect(profile.getDc(), LDAPConnection.DEFAULT_PORT);

			lc.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));
			LDAPSearchResults r = lc.search(buildBaseDN(profile.getDc()), LDAPConnection.SCOPE_SUB,
					"(&(sAMAccountName=*)(givenName=*))", null, false);

			while (r.hasMore()) {
				try {
					LDAPEntry entry = r.next();
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
					DomainOrganizationalUnit ou = parseOrganizationUnit(entry);
					if (ou != null)
						ous.add(ou);
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

		return ous;
	}
	
	

	@Override
	public boolean verifyPassword(LdapProfile profile, String account, String password) {
		return verifyPassword(profile, account, password, 0);
	}

	@Override
	public boolean verifyPassword(LdapProfile profile, String account, String password, int timeout) {
		LDAPConnection lc = new LDAPConnection();
		try {
			if (timeout > 0)
				lc.setSocketTimeOut(timeout);
			
			lc.connect(profile.getDc(), LDAPConnection.DEFAULT_PORT);

			lc.bind(LDAPConnection.LDAP_V3, profile.getAccount(), profile.getPassword().getBytes("utf-8"));
			LDAPSearchResults r = lc.search(buildBaseDN(profile.getDc()), LDAPConnection.SCOPE_SUB, "(sAMAccountName="
					+ account + ")", null, false);

			// query for verification
			LDAPEntry entry = r.next();
			logger.trace("kraken ldap: verify password for {}", entry);

			// try bind
			String dn = entry.getAttribute("distinguishedName").getStringValue();
			lc.bind(LDAPConnection.LDAP_V3, dn, password.getBytes("utf-8"));
			return true;
		} catch (Exception e) {
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

	@Override
	public void sync(BundleContext bc, LdapProfile profile) {
		ServiceReference ldapOrgUnitApiRef = bc.getServiceReference(LdapOrganizationalUnitApi.class.getName());
		ServiceReference userApiRef = bc.getServiceReference(UserApi.class.getName());
		if (ldapOrgUnitApiRef == null || userApiRef == null)
			throw new IllegalStateException("kraken-dom not ready");

		try {
			Map<String, LdapOrganizationalUnit> ldapOrgUnits = syncLdapOrgUnit(bc, ldapOrgUnitApiRef, profile);
			syncUser(bc, userApiRef, profile, ldapOrgUnits);
		} catch (NullPointerException e) {
			throw new IllegalStateException("check database character set");
		}
	}

	private Map<String, LdapOrganizationalUnit> syncLdapOrgUnit(BundleContext bc, ServiceReference ref,
			LdapProfile profile) {
		LdapOrganizationalUnitApi api = (LdapOrganizationalUnitApi) bc.getService(ref);
		Map<String, LdapOrganizationalUnit> before = new HashMap<String, LdapOrganizationalUnit>();

		for (LdapOrganizationalUnit ldapOrgUnit : api.getLdapOrganizationalUnits())
			before.put(ldapOrgUnit.getDistinguishedName(), ldapOrgUnit);

		Collection<DomainOrganizationalUnit> domainOrgUnits = getOrganizationUnits(profile);
		for (DomainOrganizationalUnit domainOrgUnit : domainOrgUnits) {
			String dn = domainOrgUnit.getDistinguishedName();

			if (before.containsKey(dn))
				before.remove(dn);
			else {
				LdapOrganizationalUnit ldapOrgUnit = new LdapOrganizationalUnit();
				ldapOrgUnit.setDistinguishedName(domainOrgUnit.getDistinguishedName());
				ldapOrgUnit.setProfile(profile.getDc());
				api.createLdapOrganizationalUnit(ldapOrgUnit);
			}
		}

		for (LdapOrganizationalUnit dn : before.values())
			api.removeLdapOrganizationalUnit(dn.getId());

		api.sync();

		Map<String, LdapOrganizationalUnit> after = new HashMap<String, LdapOrganizationalUnit>();
		for (LdapOrganizationalUnit ldapOrgUnit : api.getLdapOrganizationalUnits())
			after.put(ldapOrgUnit.getOrganizationUnit().getName(), ldapOrgUnit);

		return after;
	}

	private void syncUser(BundleContext bc, ServiceReference ref, LdapProfile profile,
			Map<String, LdapOrganizationalUnit> ldapOrgUnits) {
		UserApi api = (UserApi) bc.getService(ref);

		Collection<User> users = api.getUsers(profile.getDc());
		Map<String, User> before = new HashMap<String, User>();
		for (User user : users)
			before.put(user.getLoginName(), user);

		Collection<DomainUserAccount> domainUsers = getDomainUserAccounts(profile);
		for (DomainUserAccount domainUser : domainUsers) {
			User user = null;
			boolean isUpdate = false;

			if (before.containsKey(domainUser.getAccountName())) {
				user = before.get(domainUser.getAccountName());
				before.remove(domainUser.getAccountName());
				isUpdate = true;
			} else {
				user = new User();
				user.setLoginName(domainUser.getAccountName());
				user.setDomainController(profile.getDc());
				user.setCreateDateTime(new Date());
			}

			user.setName(domainUser.getDisplayName());
			user.setOrganizationUnit(ldapOrgUnits.get(domainUser.getOrganizationUnitName()).getOrganizationUnit());
			user.setOrganization(user.getOrganizationUnit().getOrganization());
			user.setTitle(domainUser.getTitle());
			user.setDepartment(domainUser.getDepartment());
			user.setEmail(domainUser.getMail());
			user.setPhone(domainUser.getMobile());
			user.setUpdateDateTime(new Date());

			if (isUpdate)
				api.updateUser(user);
			else
				api.createUser(user);

			if (domainUser.isDomainAdmin())
				syncAdmin(bc, user, domainUser);
		}

		for (User user : before.values())
			api.removeUser(user.getId());
	}

	private void syncAdmin(BundleContext bc, User user, DomainUserAccount domainUser) {
		ServiceReference adminApiRef = bc.getServiceReference(AdminApi.class.getName());
		ServiceReference roleApiRef = bc.getServiceReference(RoleApi.class.getName());
		AdminApi adminApi = (AdminApi) bc.getService(adminApiRef);
		RoleApi roleApi = (RoleApi) bc.getService(roleApiRef);

		Admin admin = null;
		boolean isUpdate = false;

		if (user.getAdmin() == null) {
			admin = new Admin();
			admin.setRole(roleApi.getRole("admin"));
			admin.setUser(user);
		} else {
			admin = user.getAdmin();
			isUpdate = true;
		}
		admin.setLastLoginDateTime(domainUser.getLastLogon());
		admin.setEnabled(true);

		ProgramProfile programProfile = getProgramProfile(bc, admin);
		if (programProfile == null)
			return;

		admin.setProgramProfile(programProfile);

		if (isUpdate)
			adminApi.updateAdmin(admin.getUser().getOrganization().getId(), null, admin);
		else
			adminApi.createAdmin(admin.getUser().getOrganization().getId(), null, admin);
	}

	private ProgramProfile getProgramProfile(BundleContext bc, Admin admin) {
		ServiceReference orgParameterApiRef = bc.getServiceReference(OrganizationParameterApi.class.getName());
		OrganizationParameterApi orgParameterApi = (OrganizationParameterApi) bc.getService(orgParameterApiRef);
		ServiceReference programApiRef = bc.getServiceReference(ProgramApi.class.getName());
		ProgramApi programApi = (ProgramApi) bc.getService(programApiRef);

		OrganizationParameter op = orgParameterApi.getOrganizationParameter(admin.getUser().getOrganization().getId(),
				"default_program_profile_id");
		ProgramProfile profile = programApi.getProgramProfile(admin.getUser().getOrganization().getId(),
				Integer.parseInt(op.getValue()));

		return profile;
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
			d.setLastLogon(getDate(attrs, "lastLogon"));
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
}
