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
package org.krakenapps.ldap.msgbus;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapService;
import org.krakenapps.ldap.LdapSyncService;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;

@Component(name = "ldap-plugin")
@MsgbusPlugin
public class LdapPlugin {
	private BundleContext bc;

	@Requires
	private LdapService ldap;

	public LdapPlugin(BundleContext bc) {
		this.bc = bc;
	}

	@MsgbusMethod
	public void createProfile(Request req, Response resp) {
		String name = req.getString("profile_name");
		String dc = req.getString("dc");
		int port = LdapProfile.DEFAULT_PORT;
		String account = req.getString("account");
		String password = req.getString("password");
		int syncInterval = 10 * 60 * 1000;
		if (req.has("port"))
			port = req.getInteger("port");

		if (req.has("sync_interval"))
			syncInterval = req.getInteger("sync_interval");

		LdapProfile profile = new LdapProfile(name, dc, port, account, password, syncInterval, null);
		ldap.createProfile(profile);
	}

	@MsgbusMethod
	public void setSyncInterval(Request req, Response resp) {
		String name = req.getString("profile_name");
		int syncInterval = req.getInteger("sync_interval");

		LdapProfile p = ldap.getProfile(name);
		if (p == null)
			throw new MsgbusException("ldap", "profile-not-found");

		LdapProfile newProfile = new LdapProfile(name, p.getDc(), p.getPort(), p.getAccount(), p.getPassword(),
				syncInterval, p.getLastSync());

		ldap.updateProfile(newProfile);
	}

	@MsgbusMethod
	public void removeProfile(Request req, Response resp) {
		String name = req.getString("profile_name");
		ldap.removeProfile(name);
	}

	@MsgbusMethod
	public void getProfiles(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		for (LdapProfile profile : ldap.getProfiles())
			l.add(marshal(profile));

		resp.put("profiles", l);
	}

	@MsgbusMethod
	public void getProfile(Request req, Response resp) {
		String name = req.getString("profile_name");
		LdapProfile profile = ldap.getProfile(name);
		resp.put("profile", profile == null ? null : marshal(profile));
	}

	@MsgbusMethod
	public void getDomainUserAccounts(Request req, Response resp) {
		List<Object> l = new ArrayList<Object>();
		String profileName = req.getString("profile_name");
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		for (DomainUserAccount account : ldap.getDomainUserAccounts(profile))
			l.add(marshal(account));

		resp.put("users", l);
	}

	@MsgbusMethod
	public void verifyPassword(Request req, Response resp) {
		String profileName = req.getString("profile_name");
		String account = req.getString("account");
		String testPassword = req.getString("test_password");

		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		boolean validity = ldap.verifyPassword(profile, account, testPassword);
		resp.put("result", validity);
	}

	@MsgbusMethod
	public void testConnection(Request req, Response resp) throws LDAPException, UnsupportedEncodingException {
		String dc = req.getString("dc");
		String account = req.getString("account");
		String password = req.getString("password");

		LDAPConnection lc = new LDAPConnection();
		try {
			lc.connect(dc, LDAPConnection.DEFAULT_PORT);
			lc.bind(LDAPConnection.LDAP_V3, account, password.getBytes("utf-8"));
		} finally {
			if (lc.isConnected())
				lc.disconnect();
		}
	}

	@MsgbusMethod
	public void syncDom(Request req, Response resp) {
		ServiceReference ref = bc.getServiceReference(LdapSyncService.class.getName());
		if (ref == null) {
			throw new MsgbusException("ldap", "kraken-dom not found");
		}

		String profileName = req.getString("profile_name");
		LdapProfile profile = ldap.getProfile(profileName);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		LdapSyncService ldapSync = (LdapSyncService) bc.getService(ref);
		ldapSync.sync(profile);
	}

	private Map<String, Object> marshal(LdapProfile profile) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("name", profile.getName());
		m.put("dc", profile.getDc());
		m.put("port", profile.getPort());
		m.put("account", profile.getAccount());
		m.put("password", profile.getPassword());
		m.put("sync_interval", profile.getSyncInterval());
		m.put("last_sync", profile.getLastSync());
		return m;
	}

	private Map<String, Object> marshal(DomainUserAccount account) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		Map<String, Object> m = new HashMap<String, Object>();

		m.put("account_name", account.getAccountName());
		m.put("department", account.getDepartment());
		m.put("display_name", account.getDisplayName());
		m.put("distinguished_name", account.getDistinguishedName());
		m.put("given_name", account.getGivenName());
		m.put("last_logon", account.getLastLogon());
		m.put("last_password_change", account.getLastPasswordChange());
		m.put("login_count", account.getLoginCount());
		m.put("mail", account.getMail());
		m.put("mobile", account.getMobile());
		m.put("surname", account.getSurname());
		m.put("title", account.getTitle());
		m.put("user_principal_name", account.getUserPrincipalName());
		m.put("when_created", dateFormat.format(account.getWhenCreated()));
		m.put("account_expires", dateFormat.format(account.getAccountExpires()));

		return m;
	}
}
