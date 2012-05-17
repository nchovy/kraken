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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.DOMException;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPermission;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-admin-plugin")
@MsgbusPlugin
public class AdminPlugin {
	@Requires
	private AdminApi adminApi;

	@Requires
	private UserApi userApi;

	@Requires
	private RoleApi roleApi;

	@Requires
	private ConfigManager conf;

	@MsgbusMethod
	public void getAdmins(Request req, Response resp) {
		resp.put("admins", PrimitiveConverter.serialize(adminApi.getAdmins(req.getOrgDomain())));
	}

	@MsgbusMethod
	public void getAdmin(Request req, Response resp) {
		User user = userApi.getUser(req.getOrgDomain(), req.getString("login_name"));
		Admin admin = adminApi.getAdmin(req.getOrgDomain(), user);
		resp.put("admin", PrimitiveConverter.serialize(admin));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void setAdmin(Request req, Response resp) {
		String loginName = req.getString("login_name");
		Admin before = adminApi.findAdmin(req.getOrgDomain(), loginName);
		if (before == null)
			before = new Admin();
		Admin admin = (Admin) PrimitiveConverter.overwrite(before, req.getParams(), conf.getParseCallback(req.getOrgDomain()));
		adminApi.setAdmin(req.getOrgDomain(), req.getAdminLoginName(), loginName, admin);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void unsetAdmin(Request req, Response resp) {
		String loginName = req.getString("login_name");
		adminApi.unsetAdmin(req.getOrgDomain(), req.getAdminLoginName(), loginName);
	}

	@MsgbusMethod
	public void getPermissions(Request req, Response resp) {
		Admin admin = adminApi.getAdmin(req.getOrgDomain(), req.getAdminLoginName());
		resp.put("permissions", PrimitiveConverter.serialize(admin.getRole().getPermissions()));
	}

	@MsgbusMethod
	public void hasPermission(Request req, Response resp) {
		String group = req.getString("group");
		String permission = req.getString("permission");
		resp.put("result", roleApi.hasPermission(req.getOrgDomain(), req.getAdminLoginName(), group, permission));
	}

	@MsgbusMethod
	public void updateOtpSeed(Request req, Response resp) {
		String loginName = req.getString("login_name");

		String domain = req.getOrgDomain();
		Admin admin = adminApi.findAdmin(domain, req.getAdminLoginName());
		if (admin == null)
			throw new MsgbusException("dom", "admin-not-found");

		User target = userApi.findUser(domain, req.getString("login_name"));

		if (target == null)
			throw new MsgbusException("dom", "user-not-found");

		if (admin.getRole().getLevel() == 2) {
			if (req.getAdminLoginName().equals(loginName)) {
				String newSeed = adminApi.updateOtpSeed(domain, req.getAdminLoginName(), loginName);
				resp.put("otp_seed", newSeed);
				return;
			} else if (adminApi.canManage(domain, admin, target)) {
				String newSeed = adminApi.updateOtpSeed(domain, req.getAdminLoginName(), loginName);
				resp.put("otp_seed", newSeed);
				return;
			} else {
				throw new DOMException("no-permission");
			}
		}

		if (!adminApi.canManage(domain, admin, target))
			throw new DOMException("no-permission");

		String newSeed = adminApi.updateOtpSeed(domain, req.getAdminLoginName(), loginName);
		resp.put("otp_seed", newSeed);
	}
}
