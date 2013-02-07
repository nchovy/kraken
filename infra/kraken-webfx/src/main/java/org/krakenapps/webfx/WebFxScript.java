/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
