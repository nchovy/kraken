package org.krakenapps.dom.script;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.dom.api.AreaApi;
import org.krakenapps.dom.api.GlobalConfigApi;
import org.krakenapps.dom.api.OrganizationApi;
import org.krakenapps.dom.api.ProgramApi;
import org.krakenapps.dom.api.RoleApi;
import org.krakenapps.dom.api.UserApi;

@Component(name = "dom-script-factory")
@Provides
public class DomScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "dom")
	private String alias;

	@Requires
	private GlobalConfigApi globalConfigApi;

	@Requires
	private OrganizationApi orgApi;

	@Requires
	private UserApi userApi;

	@Requires
	private RoleApi roleApi;

	@Requires
	private ProgramApi programApi;

	@Requires
	private AreaApi areaApi;

	@Override
	public Script createScript() {
		return new DomScript(globalConfigApi, orgApi, userApi, roleApi, programApi, areaApi);
	}
}
