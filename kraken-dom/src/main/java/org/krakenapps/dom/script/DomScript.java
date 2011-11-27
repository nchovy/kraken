package org.krakenapps.dom.script;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.dom.api.ApplicationApi;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.api.HostApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;
import org.krakenapps.dom.model.HostExtension;
import org.krakenapps.dom.model.HostType;
import org.krakenapps.dom.model.Vendor;

public class DomScript implements Script {
	private ScriptContext context;

	private GlobalConfigApi globalConfigApi;
	private OrganizationApi orgApi;
	private UserApi userApi;
	private RoleApi roleApi;
	private ProgramApi programApi;
	private AreaApi areaApi;
	private HostApi hostApi;
	private ApplicationApi appApi;

	public DomScript(GlobalConfigApi globalConfigApi, OrganizationApi orgApi, UserApi userApi, RoleApi roleApi,
			ProgramApi programApi, AreaApi areaApi, HostApi hostApi, ApplicationApi appApi) {
		this.globalConfigApi = globalConfigApi;
		this.orgApi = orgApi;
		this.userApi = userApi;
		this.roleApi = roleApi;
		this.programApi = programApi;
		this.areaApi = areaApi;
		this.hostApi = hostApi;
		this.appApi = appApi;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
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
			for (HostExtension ext : t.getExtensions()) {
				context.println("\t" + ext);
			}
		}
	}

	@ScriptUsage(description = "add host type", arguments = {
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

	@ScriptUsage(description = "add host type", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = ""),
			@ScriptArgument(name = "host type guid", type = "string", description = ""),
			@ScriptArgument(name = "class name", type = "string", description = "host extension class name"),
			@ScriptArgument(name = "ordinal", type = "string", description = "ordinal") })
	public void addHostExtension(String[] args) {
		String domain = args[0];
		String hostTypeGuid = args[1];
		String className = args[2];
		int ordinal = Integer.valueOf(args[3]);

		HostExtension ext = new HostExtension();
		ext.setClassName(className);
		ext.setOrd(ordinal);

		HostType t = hostApi.getHostType(domain, hostTypeGuid);
		t.getExtensions().add(ext);

		hostApi.updateHostType(domain, t);
		context.println("added");
	}
}
