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
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.User;
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

	@MsgbusMethod
	public void getAdmins(Request req, Response resp) {
		resp.put("admins", PrimitiveConverter.serialize(adminApi.getAdmins(req.getOrgDomain())));
	}

	@MsgbusMethod
	public void getAdminByUser(Request req, Response resp) {
		User user = userApi.getUser(req.getOrgDomain(), req.getString("login_name"));
		Admin admin = adminApi.getAdmin(user);
		resp.put("admin", PrimitiveConverter.serialize(admin));
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void setAdmin(Request req, Response resp) {
		Admin admin = PrimitiveConverter.parse(Admin.class, req.getParams());
		String loginName = req.getString("login_name");
		adminApi.setAdmin(req.getOrgDomain(), req.getAdminLoginName(), loginName, admin);
	}

	@MsgbusMethod
	@MsgbusPermission(group = "dom", code = "admin_grant")
	public void updateAdmin(Request req, Response resp) {
		Admin admin = PrimitiveConverter.parse(Admin.class, req.getParams());
		String loginName = req.getString("login_name");
		adminApi.updateAdmin(req.getOrgDomain(), req.getAdminLoginName(), loginName, admin);
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
	public void updateOtpSeed(Request req, Response resp) {
		String newSeed = adminApi.updateOtpSeed(req.getOrgDomain(), req.getAdminLoginName(), req.getString("login_name"));
		resp.put("new_seed", newSeed);
	}
}
