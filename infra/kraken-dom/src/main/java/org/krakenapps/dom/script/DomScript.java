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

import java.util.Map;
import java.util.Random;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.ConfigManager;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.HostUpdateApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.OrganizationUnitApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.Host;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.dom.model.OrganizationUnit;
import org.krakenapps.dom.model.Program;
import org.krakenapps.dom.model.ProgramPack;
import org.krakenapps.dom.model.ProgramProfile;
import org.krakenapps.dom.model.User;
import org.krakenapps.dom.model.Vendor;

public class DomScript implements Script {
	private ScriptContext context;

	private GlobalConfigApi globalConfigApi;
	private OrganizationApi orgApi;
	private OrganizationUnitApi orgUnitApi;
	private UserApi userApi;
	private RoleApi roleApi;
	private ProgramApi programApi;
	private AreaApi areaApi;
	private HostApi hostApi;
	private ApplicationApi appApi;
	private HostUpdateApi updateApi;
	private ConfigService conf;

	public DomScript(GlobalConfigApi globalConfigApi, OrganizationApi orgApi, OrganizationUnitApi orgUnitApi, UserApi userApi,
			RoleApi roleApi, ProgramApi programApi, AreaApi areaApi, HostApi hostApi, ApplicationApi appApi,
			HostUpdateApi updateApi, ConfigService conf) {
		this.globalConfigApi = globalConfigApi;
		this.orgApi = orgApi;
		this.orgUnitApi = orgUnitApi;
		this.userApi = userApi;
		this.roleApi = roleApi;
		this.programApi = programApi;
		this.areaApi = areaApi;
		this.hostApi = hostApi;
		this.appApi = appApi;
		this.updateApi = updateApi;
		this.conf = conf;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	// can remove master user
	@ScriptUsage(description = "remove user", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "domain"),
			@ScriptArgument(name = "login name", type = "string", description = "target login name") })
	public void removeUser(String[] args) {
		String domain = args[0];
		String loginName = args[1];

		try {
			ConfigDatabase db = conf.getDatabase("kraken-dom-" + domain);
			Config c = db.findOne(User.class, Predicates.field("login_name", loginName));

			if (c == null) {
				context.println("user not found");
				return;
			}

			db.remove(c, true);
			context.println("done");
		} catch (RuntimeException e) {
			context.println("[kraken-dom-" + domain + "] not found");
		}
	}

	@ScriptUsage(description = "reset user password", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "domain"),
			@ScriptArgument(name = "login name", type = "string", description = "target login name") })
	public void resetPassword(String[] args) {
		User user = userApi.findUser(args[0], args[1]);
		if (user == null) {
			context.println("admin not found");
			return;
		}

		try {
			context.print("New Password: ");
			String password = context.readPassword();
			user.setPassword(password);
			userApi.updateUser(args[0], user, true);
		} catch (InterruptedException e) {
			context.println("");
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "reset password", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "login name", type = "string", description = "login name"),
			@ScriptArgument(name = "password", type = "string", description = "new password"),
			@ScriptArgument(name = "password as hash", type = "string", description = "true if hash input or false. false by default", optional = false) })
	public void passwd(String[] args) {
		String domain = args[0];
		String loginName = args[1];
		String password = args[2];
		boolean updatePassword = true;
		if (args.length >= 4) {
			updatePassword = !Boolean.parseBoolean(args[3]);
		}

		User user = userApi.getUser(domain, loginName);
		user.setPassword(password);

		userApi.updateUser(domain, user, updatePassword);
		context.println("password reset");
	}

	public void install(String[] args) {
		InitialSchema.generate(context, globalConfigApi, orgApi, roleApi, programApi, areaApi, userApi);
		context.println("install complete");
	}

	public void hostTypes(String[] args) {
		context.println("Host Types");
		context.println("------------");

		for (HostType t : hostApi.getHostTypes("localhost")) {
			context.println(t);
			for (HostExtension ext : t.getDefaultExtensions()) {
				context.println("\t" + ext);
			}
		}
	}

	@ScriptUsage(description = "print all hosts in org domain", arguments = { @ScriptArgument(name = "org domain", type = "string", description = "org domain name. 'localhost' by default", optional = true) })
	public void hosts(String[] args) {
		String orgDomain = "localhost";
		if (args.length > 0)
			orgDomain = args[0];

		context.println("Hosts");
		context.println("--------");

		for (Host h : hostApi.getHosts(orgDomain)) {
			context.println(h);
		}
	}

	@ScriptUsage(description = "add vendor", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "domain"),
			@ScriptArgument(name = "name", type = "string", description = "name") })
	public void addVendor(String[] args) {
		Vendor vendor = new Vendor();
		vendor.setName(args[1]);
		appApi.createVendor(args[0], vendor);
		context.println("added " + vendor.getGuid());
	}

	@ScriptUsage(description = "add host type", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = ""),
			@ScriptArgument(name = "vendor guid", type = "string", description = ""),
			@ScriptArgument(name = "name", type = "string", description = ""),
			@ScriptArgument(name = "version", type = "string", description = "") })
	public void addHostType(String[] args) {
		Vendor vendor = appApi.getVendor(args[0], args[1]);

		HostType t = new HostType();
		t.setVendor(vendor);
		t.setName(args[2]);
		t.setVersion(args[3]);
		hostApi.createHostType(args[0], t);
		context.println("added " + t.getGuid());
	}

	@ScriptUsage(description = "add host extension", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = ""),
			@ScriptArgument(name = "host type guid", type = "string", description = ""),
			@ScriptArgument(name = "type", type = "string", description = "host extension type"),
			@ScriptArgument(name = "ordinal", type = "string", description = "ordinal") })
	public void addHostExtension(String[] args) {
		String domain = args[0];
		String hostTypeGuid = args[1];
		String type = args[2];
		int ordinal = Integer.valueOf(args[3]);

		HostExtension ext = new HostExtension();
		ext.setType(type);
		ext.setOrd(ordinal);

		HostType t = hostApi.getHostType(domain, hostTypeGuid);
		t.getDefaultExtensions().add(ext);

		hostApi.updateHostType(domain, t);
		context.println("added");
	}

	@ScriptUsage(description = "list all programs", arguments = { @ScriptArgument(name = "domain", type = "string", description = "") })
	public void programs(String[] args) {
		context.println("Programs");
		context.println("----------");
		for (Program p : programApi.getPrograms(args[0])) {
			context.println(p);
		}
	}

	@ScriptUsage(description = "add new program", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "package name", type = "string", description = "package name"),
			@ScriptArgument(name = "name", type = "string", description = "program name"),
			@ScriptArgument(name = "type", type = "string", description = "program type") })
	public void addProgram(String[] args) {
		String domain = args[0];
		String packName = args[1];
		String name = args[2];
		String type = args[3];

		Program p = new Program();
		p.setPack(packName);
		p.setName(name);
		p.setPath(type);
		p.setVisible(true);
		programApi.createProgram(domain, p);

		ProgramProfile pp = programApi.getProgramProfile(domain, "all");
		pp.getPrograms().add(p);
		programApi.updateProgramProfile(domain, pp);

		context.println("added");
	}

	@ScriptUsage(description = "add new program", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "package name", type = "string", description = "package name"),
			@ScriptArgument(name = "name", type = "string", description = "program name") })
	public void updateProgram(String[] args) {
		String domain = args[0];
		String packName = args[1];
		String name = args[2];
		Program program = programApi.findProgram(domain, packName, name);
		if (program == null) {
			context.println("not found");
			return;
		}

		try {
			context.print("description: " + program.getDescription() + " -> ");
			String description = context.readLine();
			if (!description.isEmpty())
				program.setDescription(description);

			context.print("path: " + program.getDescription() + " -> ");
			String path = context.readLine();
			if (!path.isEmpty())
				program.setPath(path);

			context.print("visible: " + program.getDescription() + " -> ");
			String visible = context.readLine();
			if (!visible.isEmpty())
				program.setVisible(Boolean.parseBoolean(visible));

			context.print("seq: " + program.getDescription() + " -> ");
			String seq = context.readLine();
			if (!seq.isEmpty())
				program.setSeq(Integer.parseInt(seq));

			programApi.updateProgram(domain, program);
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "remove program", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "package name", type = "string", description = "package name"),
			@ScriptArgument(name = "program name", type = "string", description = "program name") })
	public void removeProgram(String[] args) {
		String domain = args[0];
		String packName = args[1];
		String programName = args[2];

		ProgramProfile pp = programApi.getProgramProfile(domain, "all");
		Program target = null;
		for (Program p : pp.getPrograms())
			if (p.getPack().equals(packName) && p.getName().equals(programName))
				target = p;

		pp.getPrograms().remove(target);
		programApi.updateProgramProfile(domain, pp);

		programApi.removeProgram(domain, packName, programName);
		context.println("removed");
	}

	@ScriptUsage(description = "add new program pack", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "org domain"),
			@ScriptArgument(name = "pack name", type = "string", description = "pack name"),
			@ScriptArgument(name = "dll", type = "string", description = "dll") })
	public void addProgramPack(String[] args) {
		String domain = args[0];
		String packName = args[1];
		String dll = args[2];

		ProgramPack pack = new ProgramPack();
		pack.setName(packName);
		pack.setDll(dll);

		programApi.createProgramPack(domain, pack);
	}

	@ScriptUsage(description = "set organization parameter", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "organization domain"),
			@ScriptArgument(name = "name", type = "string", description = "organization parameter name"),
			@ScriptArgument(name = "value", type = "string", description = "organization parameter value"),
			@ScriptArgument(name = "type", type = "string", description = "string or int") })
	public void setOrgParam(String[] args) {
		Object value = null;
		if (args[3].equalsIgnoreCase("string"))
			value = args[2];
		else if (args[3].equalsIgnoreCase("int"))
			value = Integer.valueOf(args[2]);

		orgApi.setOrganizationParameter(args[0], args[1], value);
		context.println("set");
	}

	@ScriptUsage(description = "remove organization parameter", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "organization domain"),
			@ScriptArgument(name = "name", type = "string", description = "organization parameter name") })
	public void unsetOrgParam(String[] args) {
		orgApi.unsetOrganizationParameter(args[0], args[1]);
		context.println("unset");
	}

	@ScriptUsage(description = "set organization parameter", arguments = { @ScriptArgument(name = "domain", type = "string", description = "organization domain") })
	public void orgParams(String[] args) {
		Map<String, Object> m = orgApi.getOrganizationParameters(args[0]);
		context.println("Organization Parameters");
		for (String key : m.keySet())
			context.println(key + "=" + m.get(key));
	}

	@ScriptUsage(description = "current waiting host count")
	public void pendingUpdates(String[] args) {
		context.println("Waiting host count: " + updateApi.getPendingCount());
	}

	@ScriptUsage(description = "update hosts", arguments = { @ScriptArgument(name = "count", type = "string", description = "host count") })
	public void updateHosts(String[] args) {
		int count = Integer.parseInt(args[0]);
		if (count <= 0) {
			context.println("host count should be larger than 0");
			return;
		}

		Random random = new Random();
		while (count > 0) {
			Host host = new Host();
			int id = random.nextInt(20000);
			host.setGuid(String.valueOf(id));
			host.setName(String.valueOf(id));
			HostType type = new HostType();
			type.setGuid("0dd73318-6db5-49fd-aae4-7deff285da6b");
			host.setType(type);

			updateApi.update(host);
			count--;
		}
	}

	@ScriptUsage(description = "print organization units", arguments = { @ScriptArgument(name = "domain", type = "string", description = "organization domain") })
	public void orgUnits(String[] args) {
		context.println("DOM Organization Units");
		context.println("-------------------------");
		for (OrganizationUnit orgUnit : orgUnitApi.getOrganizationUnits(args[0])) {
			context.println(orgUnit);
		}
	}

	@ScriptUsage(description = "print users", arguments = { @ScriptArgument(name = "domain", type = "string", description = "organization domain") })
	public void users(String[] args) {
		context.println("DOM Users");
		context.println("-------------");
		for (User user : userApi.getUsers(args[0])) {
			context.println(user);
		}
	}
}
