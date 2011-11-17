package org.krakenapps.dom.script;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;

public class DomScript implements Script {
	private ScriptContext context;

	private GlobalConfigApi globalConfigApi;
	private OrganizationApi orgApi;
	private UserApi userApi;
	private RoleApi roleApi;
	private ProgramApi programApi;
	private AreaApi areaApi;

	public DomScript(GlobalConfigApi globalConfigApi, OrganizationApi orgApi, UserApi userApi, RoleApi roleApi, ProgramApi programApi,
			AreaApi areaApi) {
		this.globalConfigApi = globalConfigApi;
		this.orgApi = orgApi;
		this.userApi = userApi;
		this.roleApi = roleApi;
		this.programApi = programApi;
		this.areaApi = areaApi;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void install(String[] args) {
		InitialSchema.generate(context, globalConfigApi, orgApi, roleApi, programApi, areaApi, userApi);
		context.println("install complete");
	}
}
