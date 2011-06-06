package org.krakenapps.grid.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.grid.client.GridClient;
import org.krakenapps.grid.control.GridNode;
import org.krakenapps.grid.control.GridNodeMaster;
import org.krakenapps.rpc.RpcAgent;

public class GridScript implements Script {
	private RpcAgent rpcAgent;
	private GridNode localNode;
	private GridNodeMaster nodeMaster;
	private ScriptContext context;

	public GridScript(RpcAgent rpcAgent, GridNode localNode, GridNodeMaster nodeMaster) {
		this.rpcAgent = rpcAgent;
		this.localNode = localNode;
		this.nodeMaster = nodeMaster;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "call grid service", arguments = { @ScriptArgument(name = "service locator address", type = "string", description = "ip address of service locator") })
	public void call(String[] args) throws IOException {
		GridClient client = null;
		try {
			String method = args[0];
			
			// get sub-array
			Object[] params = new Object[args.length - 1];
			for (int i = 0; i < params.length; i++)
				params[i] = args[i + 1];
			
			InetAddress ip = InetAddress.getByName(method);
			client = new GridClient(rpcAgent, ip);

			context.println(client.call(method, params));
		} catch (Exception e) {
			context.println(e.getMessage());
		} finally {
			if (client != null)
				client.close();
		}
	}
}
