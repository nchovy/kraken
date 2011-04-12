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

import java.util.List;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.exception.ProgramProfileNotFoundException;
import org.krakenapps.dom.exception.RoleNotFoundException;
import org.krakenapps.dom.exception.AdminNotFoundException;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.Role;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.User;
import org.krakenapps.msgbus.Marshaler;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

@Component(name = "dom-admin-plugin")
@MsgbusPlugin
public class AdminPlugin {
	@Requires
	private AdminApi adminApi;

	@Requires
	private UserApi userApi;

	@Requires
	private ProgramApi programApi;

	@Requires
	private RoleApi roleApi;

	@MsgbusMethod
	public void getAdmins(Request req, Response resp) {
		List<Admin> admins = adminApi.getAdmins(req.getOrgId());
		resp.put("admins", Marshaler.marshal(admins));
	}

	@MsgbusMethod
	public void getAdmin(Request req, Response resp) {
		int adminId = req.getInteger("id");

		Admin admin = adminApi.getAdmin(req.getOrgId(), adminId);
		if (admin == null)
			throw new AdminNotFoundException(adminId);

		resp.put("admin", admin.marshal());
	}

	@MsgbusMethod
	public void setUserToAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		Admin admin = toAdmin(req);
		adminApi.createAdmin(req.getOrgId(), req.getAdminId(), admin);
		resp.put("id", admin.getId());
	}

	@MsgbusMethod
	public void updateAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		Admin admin = toAdmin(req);
		admin.setId(req.getInteger("id"));
		adminApi.updateAdmin(req.getOrgId(), req.getAdminId(), admin);
	}

	@MsgbusMethod
	public void unsetAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		int adminId = req.getInteger("id");
		adminApi.removeAdmin(req.getOrgId(), req.getAdminId(), adminId);
	}

	private Admin toAdmin(Request req) throws RoleNotFoundException {
		Admin admin = new Admin();

		int userId = req.getInteger("user_id");
		User user = userApi.getUser(userId);
		if (user == null)
			throw new IllegalArgumentException("user not found");
		admin.setUser(user);

		int roleId = req.getInteger("role_id");
		Role role = roleApi.getRole(roleId);
		if (role == null)
			throw new RoleNotFoundException();
		admin.setRole(role);

		int profileId = req.getInteger("profile_id");
		ProgramProfile profile = programApi.getProgramProfile(req.getOrgId(), profileId);
		if (profile == null)
			throw new ProgramProfileNotFoundException(profileId);
		admin.setProgramProfile(profile);

		admin.validate();
		return admin;
	}
}
