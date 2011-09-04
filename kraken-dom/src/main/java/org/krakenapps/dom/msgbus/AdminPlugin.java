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
import java.util.Random;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.AdminApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.exception.ProgramProfileNotFoundException;
import org.krakenapps.dom.exception.RoleNotFoundException;
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
	private OrganizationApi orgApi;

	@Requires
	private OrganizationUnitApi orgUnitApi;

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
	public void getAdminByUser(Request req, Response resp) {
		int userId = req.getInteger("id");

		Admin admin = adminApi.getAdminByUser(req.getOrgId(), userId);
		if (admin == null)
			resp.put("admin", null);
		else
			resp.put("admin", admin.marshal());
	}

	@MsgbusMethod
	public void createAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		User user = new User();
		user.setOrganization(orgApi.getOrganization(req.getOrgId()));
		if (req.get("org_unit_id") != null)
			user.setOrganizationUnit(orgUnitApi.getOrganizationUnit(req.getInteger("org_unit_id")));

		user.setName(req.getString("name"));
		user.setLoginName(req.getString("login_name"));
		user.setDescription(req.getString("description"));
		user.setTitle(req.getString("title"));
		user.setEmail(req.getString("email"));
		user.setPhone(req.getString("phone"));
		userApi.createUser(user);

		Admin admin = toAdmin(req);
		admin.setUser(user);
		adminApi.createAdmin(req.getOrgId(), req.getAdminId(), admin);
		resp.put("id", admin.getId());
	}

	@MsgbusMethod
	public void setUserToAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		Admin admin = toAdmin(req);

		int userId = req.getInteger("user_id");
		User user = userApi.getUser(userId);
		if (user == null)
			throw new IllegalArgumentException("user not found");
		admin.setUser(user);

		adminApi.createAdmin(req.getOrgId(), req.getAdminId(), admin);
		resp.put("id", admin.getId());
	}

	@MsgbusMethod
	public void updateAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		Admin admin = toAdmin(req);
		admin.setId(req.getInteger("id"));

		int userId = req.getInteger("user_id");
		User user = userApi.getUser(userId);
		if (user == null)
			throw new IllegalArgumentException("user not found");
		admin.setUser(user);

		adminApi.updateAdmin(req.getOrgId(), req.getAdminId(), admin);
	}

	@MsgbusMethod
	public void unsetAdmin(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		int adminId = req.getInteger("id");
		adminApi.removeAdmin(req.getOrgId(), req.getAdminId(), adminId);
	}

	@MsgbusMethod
	public void updateOtpSeed(Request req, Response resp) {
		if (req.getAdminId() == null)
			throw new SecurityException("not admin");

		Admin admin = adminApi.getAdmin(req.getOrgId(), req.getAdminId());
		if (admin == null)
			throw new IllegalArgumentException("admin not found");

		if (admin.isUseOtp())
			admin.setOtpSeed(createOtpSeed());
		else
			admin.setOtpSeed(null);

		adminApi.updateAdmin(req.getOrgId(), req.getAdminId(), admin);
		resp.put("otp_seed", admin.getOtpSeed());
	}

	private Admin toAdmin(Request req) throws RoleNotFoundException {
		Admin admin = new Admin();

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
		admin.setUseLoginLock(req.getBoolean("use_login_lock"));
		admin.setLoginLockCount(req.getInteger("login_lock_count"));
		admin.setLoginFailures(req.getInteger("login_failures"));
		admin.setUseIdleTimeout(req.getBoolean("use_idle_timeout"));
		admin.setIdleTimeout(req.getInteger("idle_timeout"));
		admin.setEnabled(req.getBoolean("is_enabled"));
		admin.setUseOtp(req.getBoolean("use_otp"));
		if (admin.isUseOtp()) {
			if (admin.getOtpSeed() == null)
				admin.setOtpSeed(createOtpSeed());
		} else
			admin.setOtpSeed(null);

		admin.validate();
		return admin;
	}

	private static final char[] chars = new char[62];
	static {
		int i = 0;
		char c = 'a';
		for (; i < 26; i++)
			chars[i] = c++;
		c = 'A';
		for (; i < 52; i++)
			chars[i] = c++;
		c = '0';
		for (; i < 62; i++)
			chars[i] = c++;
	}

	private String createOtpSeed() {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++)
			sb.append(chars[random.nextInt(62)]);
		return sb.toString();
	}
}
