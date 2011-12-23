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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.ldap.DomainUserAccount;
import org.krakenapps.ldap.LdapProfile;
import org.krakenapps.ldap.LdapProfile.CertificateType;
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

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void getProfiles(Request req, Response resp) {
		List<Object> profiles = new ArrayList<Object>();
		for (LdapProfile profile : ldap.getProfiles()) {
			Map<String, Object> serialize = (Map<String, Object>) PrimitiveConverter.serialize(profile);
			serialize.put("trust_store", (serialize.get("trust_store") != null));
			profiles.add(serialize);
		}
		resp.put("profiles", PrimitiveConverter.serialize(ldap.getProfiles()));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void getProfile(Request req, Response resp) {
		String name = req.getString("name");
		LdapProfile profile = ldap.getProfile(name);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		Map<String, Object> serialize = (Map<String, Object>) PrimitiveConverter.serialize(profile);
		serialize.put("trust_store", (serialize.get("trust_store") != null));
		resp.put("profile", serialize);
	}

	@MsgbusMethod
	public void createProfile(Request req, Response resp) {
		LdapProfile profile = PrimitiveConverter.parse(LdapProfile.class, req.getParams());

		if (req.has("cert_type") && req.has("cert_base64")) {
			try {
				CertificateType type = CertificateType.valueOf(req.getString("cert_type"));
				String base64 = req.getString("cert_base64");
				profile.setTrustStore(type, base64);
			} catch (Exception e) {
				throw new MsgbusException("ldap", "invalid cert");
			}
		}

		ldap.createProfile(profile);
	}

	@MsgbusMethod
	public void updateProfile(Request req, Response resp) {
		String name = req.getString("name");
		LdapProfile profile = ldap.getProfile(name);
		PrimitiveConverter.overwrite(profile, req.getParams());

		if (req.has("cert_type") && req.has("cert_base64")) {
			profile.unsetTrustStore();
			try {
				CertificateType type = CertificateType.valueOf(req.getString("cert_type"));
				if (type == null)
					profile.unsetTrustStore();
				else {
					String base64 = req.getString("cert_base64");
					profile.setTrustStore(type, base64);
				}
			} catch (Exception e) {
				throw new MsgbusException("ldap", "invalid cert");
			}
		}

		ldap.updateProfile(profile);
	}

	@MsgbusMethod
	public void removeProfile(Request req, Response resp) {
		String name = req.getString("name");
		ldap.removeProfile(name);
	}

	@MsgbusMethod
	public void getDomainUserAccounts(Request req, Response resp) {
		List<Object> users = new ArrayList<Object>();
		String name = req.getString("name");
		LdapProfile profile = ldap.getProfile(name);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		for (DomainUserAccount account : ldap.getDomainUserAccounts(profile))
			users.add(PrimitiveConverter.serialize(account));

		resp.put("users", users);
	}

	@MsgbusMethod
	public void verifyPassword(Request req, Response resp) {
		String name = req.getString("name");
		String account = req.getString("account");
		String testPassword = req.getString("test_password");

		LdapProfile profile = ldap.getProfile(name);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		boolean validity = ldap.verifyPassword(profile, account, testPassword);
		resp.put("result", validity);
	}

	@MsgbusMethod
	public void testConnection(Request req, Response resp) throws LDAPException, UnsupportedEncodingException {
		String dc = req.getString("dc");
		Integer port = req.getInteger("port");
		String account = req.getString("account");
		String password = req.getString("password");

		LDAPConnection lc = new LDAPConnection();
		try {
			lc.connect(dc, port);
			lc.bind(LDAPConnection.LDAP_V3, account, password.getBytes("utf-8"));
		} finally {
			if (lc.isConnected())
				lc.disconnect();
		}
	}

	@MsgbusMethod
	public void sync(Request req, Response resp) {
		ServiceReference ref = bc.getServiceReference(LdapSyncService.class.getName());
		if (ref == null)
			throw new MsgbusException("ldap", "kraken-dom not found");

		String name = req.getString("name");
		LdapProfile profile = ldap.getProfile(name);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		LdapSyncService ldapSync = (LdapSyncService) bc.getService(ref);
		ldapSync.sync(profile);
	}
}
