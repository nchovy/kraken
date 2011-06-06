package org.krakenapps.grid.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptFactory;
import org.krakenapps.grid.control.GridNode;
import org.krakenapps.grid.control.GridNodeMaster;
import org.krakenapps.rpc.RpcAgent;

@Component(name = "grid-script-factory")
@Provides
public class GridScriptFactory implements ScriptFactory {
	@Requires
	private GridNode localNode;
	@Requires
	private GridNodeMaster nodeMaster;
	@Requires
	private RpcAgent rpcAgent;

	@Override
	public Script createScript() {
		return new GridScript(rpcAgent, localNode, nodeMaster);
	}

}
