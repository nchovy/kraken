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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.Predicate;
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
		Map<String, String> failedList = new HashMap<String, String>();
		for (User u : users) {
			if (u.getLoginName().equals("admin")) {
				failedList.put(u.getLoginName(), "no-permission");
				continue;
			}
			if (adminLoginName.equals(u.getLoginName())) {
				failedList.put(u.getLoginName(), "no-permission");
				continue;
			}
			if (!adminApi.canManage(domain, admin, u)) {
				failedList.put(u.getLoginName(), "no-permission");
				continue;
			}

			loginNames.add(u.getLoginName());
		}

		userApi.removeUsers(domain, loginNames);
		long end = new Date().getTime();
		if (logger.isTraceEnabled())
			logger.trace("kraken dom: remove [{}] users, [{}] milliseconds elapsed", loginNames.size(), end - start);
		resp.put("failed_list", failedList);
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
		String domain = req.getOrgDomain();

		// org unit guid can be null (for root node)
		OrganizationUnit orgUnit = null;
		if (orgUnitGuid != null)
			orgUnit = orgUnitApi.findOrganizationUnit(domain, orgUnitGuid);

		@SuppressWarnings("unchecked")
		HashSet<String> loginNames = new HashSet<String>((Collection<String>) req.get("login_names"));
		List<Config> configs = userApi.getConfigs(req.getOrgDomain(), null, true, Predicates.in("login_name", loginNames), 0,
				Integer.MAX_VALUE);

		Map<String, String> failedList = new HashMap<String, String>();
		List<ConfigUpdateRequest<User>> updates = new ArrayList<ConfigUpdateRequest<User>>();
		for (Config c : configs) {
			// try to check role
			User u = c.getDocument(User.class, conf.getParseCallback(domain));
			if (!adminApi.canManage(req.getOrgDomain(), admin, u)) {
				failedList.put(u.getLoginName(), "no-permission");
				continue;
			}
			u.setOrgUnit(orgUnit);
			updates.add(new ConfigUpdateRequest<User>(c, u));
		}

		userApi.updateUsers(domain, updates);

		resp.put("failed_list", failedList);
	}

	@MsgbusMethod
	public void getUsers(Request req, Response resp) {
		String orgUnitGuid = req.getString("ou_guid");

		Predicate pred = null;
		if (req.has("filter_name") || req.has("filter_login_name")) {
			String name = req.getString("filter_name");
			String loginName = req.getString("filter_login_name");
			pred = new Matched(name, loginName);
		}

		int offset = 0;
		int limit = Integer.MAX_VALUE;

		if (req.has("offset"))
			offset = req.getInteger("offset");
		if (req.has("limit"))
			limit = req.getInteger("limit");

		int total = userApi.countUsers(req.getOrgDomain(), orgUnitGuid, true, pred);
		Collection<User> users = userApi.getUsers(req.getOrgDomain(), orgUnitGuid, true, pred, offset, limit);

		resp.put("users", PrimitiveConverter.serialize(users));
		resp.put("total", total);
	}

	private static class Matched implements Predicate {
		private String userName;
		private String loginName;

		public Matched(String userName, String loginName) {
			this.userName = userName;
			this.loginName = loginName;
		}

		@Override
		public boolean eval(Config c) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) c.getDocument();

			String name = (String) m.get("name");
			String login = (String) m.get("login_name");

			if (name.contains(userName))
				return true;
			if (login.contains(loginName))
				return true;

			return false;
		}
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
		Map<String, String> failedList = new HashMap<String, String>();

		Collection<User> users = userApi.getUsers(req.getOrgDomain(), (List<String>) req.get("login_names"));
		for (User user : users) {
			if (user.getLoginName().equals("admin")) {
				failedList.put(user.getLoginName(), "no-permission");
				continue;
			}
			if (adminLoginName.equals(user.getLoginName())) {
				failedList.put(user.getLoginName(), "cannot-remove-self");
				continue;
			}
			if (!adminApi.canManage(req.getOrgDomain(), admin, user)) {
				failedList.put(user.getLoginName(), "no-permission");
				continue;
			}

			loginNames.add(user.getLoginName());
		}

		userApi.removeUsers(req.getOrgDomain(), loginNames);

		// return failed users
		resp.put("failed_list", failedList);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "user_edit")
	public void removeUser(Request req, Response resp) {
		String loginName = req.getAdminLoginName();
		String domain = req.getOrgDomain();
		Admin admin = adminApi.getAdmin(req.getOrgDomain(), req.getAdminLoginName());
		if (admin == null)
			throw new MsgbusException("dom", "admin-not-found");

		User target = userApi.findUser(domain, req.getString("login_name"));
		if (target == null)
			throw new MsgbusException("dom", "user-not-found");

		if (loginName.equals(target.getLoginName()))
			throw new MsgbusException("dom", "cannot-remove-self");

		if (!adminApi.canManage(domain, admin, target))
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
}
