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
import java.util.Iterator;
import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.api.UserExtensionProvider;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-user-plugin")
@MsgbusPlugin
public class UserPlugin {
	@Requires
	private ConfigManager conf;

	@Requires
	private UserApi userApi;

	@Requires
	private AdminApi adminApi;

	@Requires
	private OrganizationUnitApi orgApi;

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "config_edit")
	public void moveUsers(Request req, Response resp) {
		if (!req.has("org_unit_guid"))
			throw new DOMException("null-org-unit");

		if (!req.has("login_names"))
			throw new DOMException("null-login-names");

		Admin admin = adminApi.findAdmin(req.getOrgDomain(), req.getAdminLoginName());
		if (admin == null)
			throw new DOMException("admin-not-found");

		String orgUnitGuid = req.getString("org_unit_guid");

		// org unit guid can be null (for root node)
		OrganizationUnit orgUnit = null;
		if (orgUnitGuid != null)
			orgUnit = orgApi.findOrganizationUnit("localhost", orgUnitGuid);

		Collection<String> loginNames = (Collection<String>) req.get("login_names");
		Collection<User> users = userApi.getUsers(req.getOrgDomain(), loginNames);

		List<String> failures = new ArrayList<String>();

		for (User u : users) {
			// try to check role
			String loginName = u.getLoginName();
			Admin targetAdmin = adminApi.findAdmin(req.getOrgDomain(), loginName);
			if (targetAdmin != null && !loginName.equals(req.getAdminLoginName())
					&& targetAdmin.getRole().getLevel() >= admin.getRole().getLevel()) {
				failures.add(loginName);
				continue;
			}

			u.setOrgUnit(orgUnit);
		}

		userApi.updateUsers("localhost", users, false);

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
	public void createUser(Request req, Response resp) {
		User user = (User) PrimitiveConverter.overwrite(new User(), req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		userApi.createUser(req.getOrgDomain(), user);
	}

	@MsgbusMethod
	public void updateUser(Request req, Response resp) {
		Admin request = adminApi.findAdmin(req.getOrgDomain(), req.getAdminLoginName());
		if (request == null)
			throw new DOMException("admin-not-found");

		String loginName = req.getString("login_name");
		User before = userApi.getUser(req.getOrgDomain(), loginName);

		// try to check role
		Admin targetAdmin = adminApi.findAdmin(req.getOrgDomain(), loginName);
		if (targetAdmin != null && !loginName.equals(req.getAdminLoginName())
				&& targetAdmin.getRole().getLevel() >= request.getRole().getLevel())
			throw new DOMException("no-permission");

		User user = (User) PrimitiveConverter.overwrite(before, req.getParams(),
				conf.getParseCallback(req.getOrgDomain()));
		userApi.updateUser(req.getOrgDomain(), user, req.has("password"));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void removeUsers(Request req, Response resp) {
		Collection<String> loginNames = (Collection<String>) req.get("login_names");
		userApi.removeUsers(req.getOrgDomain(), loginNames);
	}

	@MsgbusMethod
	public void removeUser(Request req, Response resp) {
		String loginName = req.getString("login_name");
		userApi.removeUser(req.getOrgDomain(), loginName);
	}

	@MsgbusMethod
	public void getExtensionSchemas(Request req, Response resp) {
		List<String> schemas = new ArrayList<String>();
		for (UserExtensionProvider provider : userApi.getExtensionProviders())
			schemas.add(provider.getExtensionName());
		resp.put("schemas", schemas);
	}
}
