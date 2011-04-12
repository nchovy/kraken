package org.krakenapps.proxy.impl;

import java.net.InetSocketAddress;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.proxy.ForwardProxy;
import org.krakenapps.proxy.ForwardRoute;

public class ProxyScript implements Script {
	private ScriptContext context;
	private ForwardProxy forwardProxy;

	public ProxyScript(ForwardProxy forwardProxy) {
		this.forwardProxy = forwardProxy;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void forwardRoutes(String[] args) {
		context.println("Forward Routes");
		context.println("-------------------");
		for (String name : forwardProxy.getRouteNames()) {
			ForwardRoute route = forwardProxy.getRoute(name);
			context.printf("[%s] %s\n", name, route);
		}
	}

	@ScriptUsage(description = "add forward route", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "route name"),
			@ScriptArgument(name = "remote host", type = "string", description = "remote hostname or ip"),
			@ScriptArgument(name = "remote port", type = "int", description = "remote port"),
			@ScriptArgument(name = "local port", type = "int", description = "local port") })
	public void addForwardRoute(String[] args) {
		String name = args[0];
		String remoteHost = args[1];
		int remotePort = Integer.valueOf(args[2]);
		int localPort = Integer.valueOf(args[3]);
		ForwardRoute route = new ForwardRoute(new InetSocketAddress(localPort), new InetSocketAddress(remoteHost,
				remotePort));

		forwardProxy.addRoute(name, route);
		context.println("route added");
	}

	@ScriptUsage(description = "remove forward route", arguments = { @ScriptArgument(name = "name", type = "string", description = "route name") })
	public void removeForwardRoute(String[] args) {
		String name = args[0];
		forwardProxy.removeRoute(name);
		context.println("remote removed");
	}
}
