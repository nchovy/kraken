package org.krakenapps.rpc.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.rpc.RpcAgent;

@Component(name = "rpc-script-factory")
@Provides
public class RpcScriptFactory implements ScriptFactory {
	@SuppressWarnings("unused")
	@ServiceProperty(name = "alias", value = "rpc")
	private String alias;

	@Requires
	private RpcAgent agent;

	@Requires
	private KeyStoreManager keyStoreManager;

	@Override
	public Script createScript() {
		return new RpcScript(agent, keyStoreManager);
	}

}
