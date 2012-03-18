package org.krakenapps.webfx;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;

public class WebFxScript implements Script {
	private ScriptContext context;
	private WebApplicationRegistry appRegistry;

	public WebFxScript(WebApplicationRegistry appRegistry) {
		this.appRegistry = appRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void apps(String[] args) {
		context.println("Web Applications");
		context.println("------------------");
		for (WebApplication app : appRegistry.getWebApplications())
			context.println(app);
	}

	@ScriptUsage(description = "create web app", arguments = {
			@ScriptArgument(name = "http context name", type = "string", description = "http context name"),
			@ScriptArgument(name = "root path", type = "string", description = "root filesystem path"),
			@ScriptArgument(name = "resource", type = "string", description = "one or more resources") })
	public void createApp(String[] args) {
		WebApplication app = new WebApplication();
		app.setContext(args[0]);
		app.setRootPath(args[1]);

		for (int i = 2; i < args.length; i++)
			app.getResources().add(new Resource(args[i]));

		appRegistry.createWebApplication(app);
		context.println("created");
	}

	@ScriptUsage(description = "create web app", arguments = { @ScriptArgument(name = "http context name", type = "string", description = "http context name") })
	public void removeApp(String[] args) {
		appRegistry.removeWebApplication(args[0]);
		context.println("removed");
	}
}
