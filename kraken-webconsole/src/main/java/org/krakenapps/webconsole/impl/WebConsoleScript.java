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
package org.krakenapps.webconsole.impl;

import java.io.File;

import javax.servlet.http.HttpServlet;

import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.httpd.FileResourceServlet;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.webconsole.Program;
import org.krakenapps.webconsole.ProgramApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebConsoleScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(WebConsoleScript.class.getName());

	private ServletRegistry staticResourceApi;
	private ScriptContext context;
	private HttpServer server;
	private ProgramApi programApi;
	private KeyStoreManager keyStoreManager;

	public WebConsoleScript(HttpServer server, ServletRegistry staticResourceApi, ProgramApi programApi,
			KeyStoreManager keyStoreManager) {
		this.server = server;
		this.staticResourceApi = staticResourceApi;
		this.programApi = programApi;
		this.keyStoreManager = keyStoreManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void programs(String[] args) {
		for (Program p : programApi.getPrograms()) {
			context.println(p.toString());
		}
	}

	public void prefixes(String[] args) {
		for (String prefix : staticResourceApi.getPrefixes()) {
			HttpServlet servlet = staticResourceApi.getServlet(prefix);
			context.println(prefix + ": [" + servlet + "]");
		}
	}
}
