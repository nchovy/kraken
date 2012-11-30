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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.Base64;
import org.krakenapps.ldap.LdapServerType;
import org.krakenapps.ldap.LdapUser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novell.ldap.LDAPConnection;

@Component(name = "ldap-plugin")
@MsgbusPlugin
public class LdapPlugin {
	private final Logger logger = LoggerFactory.getLogger(LdapPlugin.class.getName());
	private BundleContext bc;

	@Requires
	private LdapService ldap;

	public LdapPlugin(BundleContext bc) {
		this.bc = bc;
	}

	@MsgbusMethod
	public void getProfiles(Request req, Response resp) {
		List<Object> profiles = new ArrayList<Object>();
		try {
			for (LdapProfile profile : ldap.getProfiles())
				profiles.add(profile.serialize());

			resp.put("profiles", PrimitiveConverter.serialize(profiles));
		} catch (Exception e) {
			logger.error("kraken ldap: cannot obtain trust store", e);
			throw new MsgbusException("ldap", "cannot obtain trust store");
		}
	}

	@MsgbusMethod
	public void getProfile(Request req, Response resp) {
		try {
			String name = req.getString("name");
			LdapProfile profile = ldap.getProfile(name);
			if (profile == null)
				throw new MsgbusException("ldap", "profile not found");

			resp.put("profile", profile.serialize());
		} catch (Exception e) {
			logger.error("kraken ldap: cannot obtain trust store", e);
			throw new MsgbusException("ldap", "cannot obtain trust store");
		}
	}

	@MsgbusMethod
	public void getTrustStore(Request req, Response resp) {
		String name = req.getString("name");
		if (name == null)
			throw new MsgbusException("ldap", "name not found");

		LdapProfile profile = ldap.getProfile(name);
		try {
			X509Certificate cert = profile.getX509Certificate();

			if (cert == null)
				throw new MsgbusException("ldap", "trust store not found");

			resp.put("trust_store", new String(Base64.encode(cert.getEncoded())));
		} catch (Exception e) {
			logger.error("kraken ldap: cannot obtain trust store, name [{}]", name, e);
			throw new MsgbusException("ldap", "cannot obtain trust store");
		}
	}

	@MsgbusMethod
	public void createProfile(Request req, Response resp) {
		try {
			LdapProfile profile = toProfile(new LdapProfile(), req.getParams());

			if (req.has("cert_type") && req.has("cert_base64")) {
				String base64 = req.getString("cert_base64");
				profile.setX509Certificate(Base64.decode(base64));
			}

			ldap.createProfile(profile);
		} catch (Exception e) {
			throw new MsgbusException("ldap", "invalid cert");
		}
	}

	@MsgbusMethod
	public void updateProfile(Request req, Response resp) {
		try {
			String name = req.getString("name");
			LdapProfile profile = toProfile(ldap.getProfile(name), req.getParams());

			byte[] cert = null;
			if (req.has("cert_base64")) {
				String base64 = req.getString("cert_base64");
				cert = Base64.decode(base64);
			}

			profile.setX509Certificate(cert);

			ldap.updateProfile(profile);
		} catch (Exception e) {
			throw new MsgbusException("ldap", "invalid cert");
		}
	}

	public LdapProfile toProfile(LdapProfile profile, Map<String, Object> m) throws CertificateException {
		profile.setName((String) m.get("name"));
		profile.setDc((String) m.get("dc"));
		profile.setAccount((String) m.get("account"));
		profile.setPort((Integer) m.get("port"));
		profile.setPassword((String) m.get("password"));
		profile.setBaseDn((String) m.get("base_dn"));
		profile.setServerType(LdapServerType.valueOf((String) m.get("server_type")));

		if (m.containsKey("sync_interval"))
			profile.setSyncInterval((Integer) m.get("sync_interval"));

		if (m.containsKey("cert_type") && m.containsKey("trust_store")) {
			profile.setType(CertificateType.valueOf((String) m.get("cert_type")));
			profile.setX509Certificate(Base64.decode((String) m.get("trust_store")));
		}

		return profile;
	}

	@MsgbusMethod
	public void removeProfiles(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		List<String> names = (List<String>) req.get("names");
		for (String name : names)
			ldap.removeProfile(name);
	}

	@MsgbusMethod
	public void removeProfile(Request req, Response resp) {
		String name = req.getString("name");
		ldap.removeProfile(name);
	}

	@Deprecated
	@MsgbusMethod
	public void getDomainUserAccounts(Request req, Response resp) {
		getUsers(req, resp);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void getUsers(Request req, Response resp) {
		List<Object> users = new ArrayList<Object>();
		Set<String> fields = null;
		if (req.has("fields"))
			fields = new HashSet<String>((List<String>) req.get("fields"));

		String name = req.getString("name");
		LdapProfile profile = ldap.getProfile(name);
		if (profile == null)
			throw new MsgbusException("ldap", "profile not found");

		for (LdapUser account : ldap.getUsers(profile))
			users.add(filt((Map<String, Object>) PrimitiveConverter.serialize(account), fields));

		resp.put("users", users);
	}

	private Map<String, Object> filt(Map<String, Object> m, Set<String> fields) {
		if (fields == null)
			return m;

		Map<String, Object> filtered = new HashMap<String, Object>();
		for (String field : fields)
			filtered.put(field, m.get(field));
		return filtered;
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
	public void testConnection(Request req, Response resp) {
		String dc = req.getString("dc");
		Integer port = req.getInteger("port");
		String account = req.getString("account");
		String password = req.getString("password");
		String baseDn = req.getString("base_dn");

		LdapProfile profile = new LdapProfile();
		profile.setName("test_connection");
		profile.setDc(dc);
		profile.setPort(port);
		profile.setAccount(account);
		profile.setPassword(password);
		profile.setBaseDn(baseDn);

		try {
			if (req.has("cert_type") && req.has("cert_base64")) {
				String type = req.getString("cert_type");
				String base64 = req.getString("cert_base64");
				logger.debug("kraken-ldap: user certificate type [{}], base64 [{}]", type, base64);
				profile.setX509Certificate(Base64.decode(base64));
				if (port == null)
					profile.setPort(LDAPConnection.DEFAULT_SSL_PORT);
			} else {
				if (port == null)
					profile.setPort(LDAPConnection.DEFAULT_PORT);
			}
		} catch (Exception e) {
			logger.error("kraken ldap: cannot set trust store", e);
			throw new MsgbusException("ldap", "invalid cert");
		}

		logger.trace("kraken ldap: create test ldap profile [{}]", profile.toString());
		ldap.testLdapConnection(profile, req.getInteger("timeout"));
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
