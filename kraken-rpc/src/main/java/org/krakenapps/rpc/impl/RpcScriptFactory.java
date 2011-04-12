package org.krakenapps.rpc.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.rpc.RpcAgent;

public class RpcScriptFactory implements ScriptFactory {
	private RpcAgent agent;

	@Override
	public Script createScript() {
		return new RpcScript(agent);
	}

}
