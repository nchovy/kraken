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
package org.krakenapps.dom.script;

import java.util.ArrayList;
import java.util.List;

import org.krakenapps.api.ScriptContext;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Admin;
import org.krakenapps.dom.model.Area;
import org.krakenapps.dom.model.Organization;
import org.krakenapps.dom.model.Permission;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.Role;
import org.krakenapps.dom.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialSchema {
	private static final Logger logger = LoggerFactory.getLogger(InitialSchema.class);

	private static final String SCHEMA_VERSION = "1";
	private static final String DEFAULT_DOMAIN = "localhost";

	public static void generate(ScriptContext context, GlobalConfigApi globalConfigApi, OrganizationApi orgApi, RoleApi roleApi,
			ProgramApi programApi, AreaApi areaApi, UserApi userApi) {
		// TODO: check schema version, add vendor & applications
		Object schemaVersion = globalConfigApi.getConfig("initial_schema_version");
		if (!SCHEMA_VERSION.equals(schemaVersion)) {
			logger.info("kraken dom: begin initialize schema");
			createOrganization(context, orgApi);
			createArea(context, areaApi);
			createPrograms(context, orgApi, programApi);
			createRoles(context, roleApi);
			createAdmin(context, userApi, roleApi, programApi);
			try {
				globalConfigApi.setConfig("initial_schema_version", SCHEMA_VERSION, true);
			} catch (Exception e) {
				logger.error("kraken dom: initial schema version setting failed", e);
				context.println("initial schema version setting failed");
			}
		}
	}

	public static void createOrganization(ScriptContext context, OrganizationApi orgApi) {
		if (orgApi.findOrganization(DEFAULT_DOMAIN) != null)
			return;

		Organization organization = new Organization();
		organization.setDomain(DEFAULT_DOMAIN);
		organization.setName(DEFAULT_DOMAIN);
		organization.setEnabled(true);
		try {
			orgApi.createOrganization(organization);
		} catch (Exception e) {
			logger.error("kraken dom: organization initialize failed", e);
			context.println("organization initialize failed");
		}
	}

	public static void createArea(ScriptContext context, AreaApi areaApi) {
		if (areaApi.findArea(DEFAULT_DOMAIN, "abb0de7a-eccb-4b51-a2e1-68d193e5e391") != null)
			return;

		Area area = new Area();
		area.setGuid("abb0de7a-eccb-4b51-a2e1-68d193e5e391");
		area.setName("/");
		try {
			areaApi.createArea(DEFAULT_DOMAIN, area);
		} catch (Exception e) {
			logger.error("kraken dom: area initialize failed", e);
			context.println("area initialize failed");
		}
	}

	public static void createPrograms(ScriptContext context, OrganizationApi orgApi, ProgramApi programApi) {
		if (programApi.findProgramPack(DEFAULT_DOMAIN, "System") == null) {
			ProgramPack pack = new ProgramPack();
			pack.setName("System");
			pack.setDll("system");
			pack.setSeq(1);
			try {
				programApi.createProgramPack(DEFAULT_DOMAIN, pack);
			} catch (Exception e) {
				logger.error("kraken dom: program pack initialize failed", e);
				context.println("program pack initialize failed");
			}
		}

		List<Program> programs = new ArrayList<Program>();
		programs.add(createProgram(context, programApi, "Dashboard", "dashboard", 1));
		programs.add(createProgram(context, programApi, "Account", "orgchart", 2));
		programs.add(createProgram(context, programApi, "Task Manager", "taskmanager", 3));
		programs.add(createProgram(context, programApi, "Run", "run", 4));
		programs.add(createProgram(context, programApi, "Developer Console", "devconsole", 5));

		if (programApi.findProgramProfile(DEFAULT_DOMAIN, "all") == null) {
			ProgramProfile profile = new ProgramProfile();
			profile.setName("all");
			profile.setPrograms(programs);
			try {
				programApi.createProgramProfile(DEFAULT_DOMAIN, profile);
			} catch (Exception e) {
				logger.error("kraken dom: program profile initialize failed", e);
				context.println("program profile pack initialize failed");
			}
			orgApi.setOrganizationParameter(DEFAULT_DOMAIN, "default_program_profile_id", "all");
		}
	}

	private static Program createProgram(ScriptContext context, ProgramApi programApi, String name, String type, int seq) {
		if (programApi.findProgram(DEFAULT_DOMAIN, "System", name) != null)
			return programApi.findProgram(DEFAULT_DOMAIN, "System", name);

		Program program = new Program();
		program.setPack("System");
		program.setName(name);
		program.setPath(type);
		program.setSeq(seq);
		program.setVisible(true);
		try {
			programApi.createProgram(DEFAULT_DOMAIN, program);
		} catch (Exception e) {
			logger.error("kraken dom: program initialize failed", e);
			context.println("program initialize failed");
		}
		return program;
	}

	public static void createRoles(ScriptContext context, RoleApi roleApi) {
		Role master = createRole(context, roleApi, "master", 4);
		Permission permission = new Permission();
		permission.setGroup("dom");
		permission.setPermission("admin_grant");
		master.getPermissions().add(permission);
		try {
			roleApi.updateRole(DEFAULT_DOMAIN, master);
		} catch (Exception e) {
			logger.error("kraken dom: master role initialize failed", e);
			context.println("master role initialize failed");
		}

		createRole(context, roleApi, "admin", 3);
		createRole(context, roleApi, "member", 2);
		createRole(context, roleApi, "guest", 1);
	}

	private static Role createRole(ScriptContext context, RoleApi roleApi, String name, int level) {
		if (roleApi.findRole(DEFAULT_DOMAIN, name) != null)
			return roleApi.findRole(DEFAULT_DOMAIN, name);

		Role role = new Role();
		role.setName(name);
		role.setLevel(level);
		try {
			roleApi.createRole(DEFAULT_DOMAIN, role);
		} catch (Exception e) {
			logger.error("kraken dom: role initialize failed", e);
			context.println("role initialize failed");
		}
		return role;
	}

	public static void createAdmin(ScriptContext context, UserApi userApi, RoleApi roleApi, ProgramApi programApi) {
		if (userApi.findUser(DEFAULT_DOMAIN, "root") != null)
			return;

		User user = new User();
		user.setLoginName("root");
		user.setName("root");
		user.setPassword("kraken");

		Admin admin = new Admin();
		admin.setRole(roleApi.getRole(DEFAULT_DOMAIN, "master"));
		admin.setProfile(programApi.getProgramProfile(DEFAULT_DOMAIN, "all"));
		admin.setLang("en");
		admin.setEnabled(true);
		user.getExt().put("admin", admin);

		try {
			userApi.setSaltLength("localhost", 0);
			userApi.createUser(DEFAULT_DOMAIN, user);
		} catch (Exception e) {
			logger.error("kraken dom: admin initialize failed", e);
			context.println("admin initialize failed");
		}
	}
}
