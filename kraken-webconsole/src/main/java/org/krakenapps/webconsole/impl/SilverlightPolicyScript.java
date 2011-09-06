package org.krakenapps.webconsole.impl;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.webconsole.SilverlightPolicyServer;

public class SilverlightPolicyScript implements Script {
	private SilverlightPolicyServer server;
	private ScriptContext context;

	public SilverlightPolicyScript(SilverlightPolicyServer server) {
		this.server = server;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void open(String[] args) {
		try {
			server.open();
			context.println("silverlight policy server opened");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	public void close(String[] args) {
		try {
			server.close();
			context.println("silverlight policy server closed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}
}
