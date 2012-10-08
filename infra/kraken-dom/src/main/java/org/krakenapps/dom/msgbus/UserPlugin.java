/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.dom.msgbus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.ConfigUpdateRequest;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "dom-user-plugin")
@MsgbusPlugin
public class UserPlugin {
	private Logger logger = LoggerFactory.getLogger(UserPlugin.class);

	@Requires
	private ConfigManager conf;

	@Requires
	private UserApi userApi;

	@Requires
	private AdminApi adminApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void removeAllUsers(Request req, Response resp) {
		long start = new Date().getTime();
		String adminLoginName = req.getAdminLoginName();
		Admin admin = adminApi.findAdmin(req.getOrgDomain(), adminLoginName);
		String domain = req.getOrgDomain();
		Collection<User> users = userApi.getUsers(domain);

		Collection<String> loginNames = new ArrayList<String>();
		List<String> failedLoginNames = new ArrayList<String>();
		for (User u : users) {
			if (u.getLoginName().equals("admin")) {
				failedLoginNames.add(u.getLoginName());
				continue;
			}
			if (adminLoginName.equals(u.getLoginName())) {
				failedLoginNames.add(u.getLoginName());
				continue;
			}
			if (!adminApi.canManage(domain, admin, u)) {
				failedLoginNames.add(u.getLoginName());
				continue;
			}

			loginNames.add(u.getLoginName());
		}

		userApi.removeUsers(domain, loginNames);
		long end = new Date().getTime();
		if (logger.isTraceEnabled())
			logger.trace("kraken dom: remove [{}] users, [{}] milliseconds elapsed", loginNames.size(), end - start);
		resp.put("failed_login_names", failedLoginNames);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void moveUsers(Request req, Response resp) {
		if (!req.has("org_unit_guid"))
			throw new DOMException("null-org-unit");

		if (!req.has("login_names"))
			throw new DOMException("null-login-names");

		String adminLoginName = req.getAdminLoginName();
		Admin admin = adminApi.findAdmin(req.getOrgDomain(), adminLoginName);
		if (admin == null)
			throw new DOMException("admin-not-found");

		String orgUnitGuid = req.getString("org_unit_guid");

		// org unit guid can be null (for root node)
		OrganizationUnit orgUnit = null;
		if (orgUnitGuid != null)
			orgUnit = orgUnitApi.findOrganizationUnit("localhost", orgUnitGuid);

		@SuppressWarnings("unchecked")
		HashSet<String> loginNames = new HashSet<String>((Collection<String>) req.get("login_names"));
		List<Config> configs = userApi.getConfigs(req.getOrgDomain(), null, true, Predicates.in("login_name", loginNames), 0,
				Integer.MAX_VALUE);

		List<String> failures = new ArrayList<String>();
		List<ConfigUpdateRequest<User>> updates = new ArrayList<ConfigUpdateRequest<User>>();
		for (Config c : configs) {
			// try to check role
			User u = c.getDocument(User.class, conf.getParseCallback("localhost"));
			if (!adminApi.canManage(req.getOrgDomain(), admin, u)) {
				failures.add(u.getLoginName());
				continue;
			}
			u.setOrgUnit(orgUnit);
			updates.add(new ConfigUpdateRequest<User>(c, u));
		}

		userApi.updateUsers("localhost", updates);

		resp.put("failed_login_names", failures);
	}

	@MsgbusMethod
	public void getUsers(Request req, Response resp) {
		Collection<User> users = null;
		if (!req.has("ou_guid"))
			users = userApi.getUsers(req.getOrgDomain());
		else {
			String orgUnitGuid = req.getString("ou_guid");
			boolean includeChildren = req.has("inc_children") ? req.getBoolean("inc_children") : false;
			users = userApi.getUsers(req.getOrgDomain(), orgUnitGuid, includeChildren);
		}

		// search name or login_name
		if (req.has("filter_name") || req.has("filter_login_name")) {
			String name = req.getString("filter_name");
			String loginName = req.getString("filter_login_name");

			Iterator<User> it = users.iterator();
			while (it.hasNext()) {
				boolean remove = true;
				User user = it.next();
				if (name != null && user.getName().contains(name))
					remove = false;
				if (loginName != null && user.getLoginName().contains(loginName))
					remove = false;
				if (remove)
					it.remove();
			}
		}

		// paging
		int offset = 0;
		int limit = users.size();

		if (req.has("offset")) {
			offset = range(0, users.size(), req.getInteger("offset"));
			limit -= offset;
		}
		if (req.has("limit"))
			limit = range(0, users.size() - offset, req.getInteger("limit"));

		resp.put("users", PrimitiveConverter.serialize(new ArrayList<User>(users).subList(offset, offset + limit)));
		resp.put("total", users.size());
	}

	private int range(int min, int max, int value) {
		if (value < min)
			return min;
		if (value > max)
			return max;
		return value;
	}

	@MsgbusMethod
	public void getUser(Request req, Response resp) {
		String loginName = req.getString("login_name");
		User user = userApi.getUser(req.getOrgDomain(), loginName);
		resp.put("user", PrimitiveConverter.serialize(user));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void createUser(Request req, Response resp) {
		User user = (User) PrimitiveConverter.overwrite(new User(), req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		userApi.createUser(req.getOrgDomain(), user);
	}

	@MsgbusMethod
	public void updateUser(Request req, Response resp) {
		String domain = req.getOrgDomain();
		Admin request = adminApi.findAdmin(domain, req.getAdminLoginName());

		if (request == null)
			throw new DOMException("admin-not-found");

		String loginName = req.getString("login_name");
		User old = userApi.getUser(domain, loginName);
		if (request.getRole().getLevel() == 2) {
			if (req.getAdminLoginName().equals(loginName)) {
				old.setName(req.getString("name"));
				old.setDescription(req.getString("description"));
				old.setEmail(req.getString("email"));
				if (req.has("password"))
					old.setPassword(req.getString("password"));
				old.setTitle(req.getString("title"));
				old.setPhone(req.getString("phone"));

				@SuppressWarnings("unchecked")
				Map<String, Object> m = (Map<String, Object>) req.get("org_unit");
				if (m != null)
					old.setOrgUnit(orgUnitApi.findOrganizationUnit(domain, (String) m.get("guid")));
				else
					old.setOrgUnit(null);

				userApi.updateUser(domain, old, req.has("password"));
				return;
			} else if (adminApi.canManage(domain, request, old)) {
				User user = (User) PrimitiveConverter.overwrite(old, req.getParams(), conf.getParseCallback(domain));
				userApi.updateUser(domain, user, req.has("password"));
				return;
			} else
				throw new DOMException("no-permission");
		} else if (!adminApi.canManage(domain, request, old))
			throw new DOMException("no-permission");

		User user = (User) PrimitiveConverter.overwrite(old, req.getParams(), conf.getParseCallback(domain));
		userApi.updateUser(domain, user, req.has("password"));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void removeUsers(Request req, Response resp) {
		String adminLoginName = req.getAdminLoginName();
		Admin admin = adminApi.getAdmin(req.getOrgDomain(), adminLoginName);
		if (admin == null)
			throw new MsgbusException("dom", "admin-not-found");

		List<String> loginNames = new ArrayList<String>();
		List<String> failedLoginNames = new ArrayList<String>();

		Collection<User> users = userApi.getUsers(req.getOrgDomain(), (List<String>) req.get("login_names"));
		for (User user : users) {
			if (user.getLoginName().equals("admin")) {
				failedLoginNames.add(user.getLoginName());
				continue;
			}
			if (adminLoginName.equals(user.getLoginName())) {
				failedLoginNames.add(user.getLoginName());
				continue;
			}
			if (!adminApi.canManage(req.getOrgDomain(), admin, user)) {
				failedLoginNames.add(user.getLoginName());
				continue;
			}

			loginNames.add(user.getLoginName());
		}

		userApi.removeUsers(req.getOrgDomain(), loginNames);

		// return failed users
		resp.put("failed_login_names", failedLoginNames);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void removeUser(Request req, Response resp) {
		String loginName = req.getAdminLoginName();
		Admin admin = adminApi.getAdmin(req.getOrgDomain(), req.getAdminLoginName());
		if (admin == null)
			throw new MsgbusException("dom", "admin-not-found");

		User target = userApi.findUser("localhost", req.getString("login_name"));
		if (target == null)
			throw new MsgbusException("dom", "user-not-found");

		if (loginName.equals(target.getLoginName()))
			throw new MsgbusException("dom", "cannot-remove-self");

		if (!adminApi.canManage("localhost", admin, target))
			throw new MsgbusException("dom", "no-permission");

		userApi.removeUser(req.getOrgDomain(), target.getLoginName());
	}

	@MsgbusMethod
	public void getExtensionSchemas(Request req, Response resp) {
		List<String> schemas = new ArrayList<String>();
		for (UserExtensionProvider provider : userApi.getExtensionProviders())
			schemas.add(provider.getExtensionName());
		resp.put("schemas", schemas);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void setForcePasswordChanges(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		Collection<String> loginNames = (Collection<String>) req.get("login_names");
		setForcePasswordChanges(loginNames, true);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void cancelForcePasswordChanges(Request req, Response resp) {
		@SuppressWarnings("unchecked")
		Collection<String> loginNames = (Collection<String>) req.get("login_names");
		setForcePasswordChanges(loginNames, false);
	}

	private void setForcePasswordChanges(Collection<String> loginNames, boolean forcePasswordChange) {
		List<ConfigUpdateRequest<User>> updateUsers = new ArrayList<ConfigUpdateRequest<User>>();

		List<Config> userConfigs = null;
		if (loginNames == null)
			userConfigs = userApi.getConfigs("localhost", null, true, null, 0, Integer.MAX_VALUE);
		else
			userConfigs = userApi.getConfigs("localhost", null, true, Predicates.in("login_name", loginNames), 0,
					Integer.MAX_VALUE);

		if (userConfigs == null)
			throw new IllegalArgumentException("userConfigs are empty.");

		for (Config c : userConfigs) {
			User user = c.getDocument(User.class);
			user.setForcePasswordChange(forcePasswordChange);
			updateUsers.add(new ConfigUpdateRequest<User>(c, user));
		}

		// updateUser 실행
		if (updateUsers.size() > 0) {
			try {
				userApi.updateUsers("localhost", updateUsers);
				logger.trace("kraken dom: updated [{}] users", updateUsers.size());
			} catch (Throwable t) {
				logger.error("kraken dom: user update failed", t);
			}
		}
	}
}
