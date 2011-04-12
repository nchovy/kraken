package org.krakenapps.msgbus.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;

public class MsgbusScript implements Script {
	private MessageBus msgbus;
	private ScriptContext context;

	public MsgbusScript(MessageBus msgbus) {
		this.msgbus = msgbus;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void packages(String[] args) {
		context.println("Msgbus Packages");
		context.println("-------------------");

		for (String key : msgbus.getPackageKeys()) {
			context.println(key + ": " + msgbus.getPackageName(key));
		}
	}

	public void plugins(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("Msgbus Plugins");
		context.println("-------------------");
		for (String name : msgbus.getPluginNames()) {
			if (filter != null && !name.contains(filter))
				continue;

			context.println(name);

			for (String method : msgbus.getMethodNames(name)) {
				context.println("    " + method.substring(name.length() + 1));
			}

			context.println("");
		}

	}

	public void sessions(String[] args) {
		context.println("Msgbus Sessions");
		context.println("-------------------");
		for (Session session : msgbus.getSessions()) {
			context.println(session.toString());
		}
	}

}
