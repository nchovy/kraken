package org.krakenapps.servlet.xmlrpc.internal;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.servlet.xmlrpc.XmlRpcMethodRegistry;

public class XmlRpcServletScript implements Script {
	private ScriptContext context;
	private XmlRpcMethodRegistry registry;

	public XmlRpcServletScript(XmlRpcMethodRegistry registry) {
		this.registry = registry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void list(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("XMLRPC Methods");
		context.println("==============");

		for (String method : registry.getMethods()) {
			if (filter != null && !method.contains(filter))
				continue;

			context.println(method);
		}
	}

}
