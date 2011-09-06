package org.krakenapps.webconsole.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.webconsole.SilverlightPolicyServer;

@Component(name = "slpolicy-script-factory")
@Provides
public class SilverlightPolicyScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "slpolicy")
	private String alias;

	@Requires
	private SilverlightPolicyServer server;

	@Override
	public Script createScript() {
		return new SilverlightPolicyScript(server);
	}
}
