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
import java.net.InetSocketAddress;

import javax.servlet.http.HttpServlet;

import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.webconsole.FileResourceServlet;
import org.krakenapps.webconsole.Program;
import org.krakenapps.webconsole.ProgramApi;
import org.krakenapps.webconsole.WebSocketServer;
import org.krakenapps.webconsole.WebSocketServerParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebConsoleScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(WebConsoleScript.class.getName());

	private ServletRegistry staticResourceApi;
	private ScriptContext context;
	private WebSocketServer server;
	private ProgramApi programApi;
	private KeyStoreManager keyStoreManager;

	public WebConsoleScript(WebSocketServer server, ServletRegistry staticResourceApi, ProgramApi programApi,
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

	@ScriptUsage(description = "get port bindings")
	public void bindings(String[] args) {
		context.println("Port Bindings");
		context.println("---------------------");
		for (InetSocketAddress binding : server.getListenAddresses()) {
			String metadata = "";

			WebSocketServerParams p = server.getParameters(binding);
			if (p.isSsl()) {
				metadata = " => https: key [" + p.getKeyAlias() + "] trust [" + p.getTrustAlias() + "]";
			}

			context.println(binding + metadata);
		}
	}

	@ScriptUsage(description = "open port", arguments = { @ScriptArgument(name = "port", type = "int", description = "bind port") })
	public void open(String[] args) {
		try {
			int port = Integer.valueOf(args[0]);
			WebSocketServerParams params = new WebSocketServerParams(new InetSocketAddress(port));
			server.open(params);
			context.println("opened http server");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken webconsole: cannot open port", e);
		}
	}

	@ScriptUsage(description = "open https server", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "bind port"),
			@ScriptArgument(name = "key alias", type = "string", description = "keystore name"),
			@ScriptArgument(name = "trust alias", type = "string", description = "truststore name") })
	public void openSsl(String[] args) {
		try {
			int port = Integer.valueOf(args[0]);
			String keyAlias = args[1];
			String trustAlias = args[2];
			WebSocketServerParams params = new WebSocketServerParams(new InetSocketAddress(port), keyStoreManager,
					keyAlias, trustAlias);
			server.open(params);
			context.println("opened https server");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken webconsole: cannot open ssl port");
		}
	}

	@ScriptUsage(description = "close port", arguments = { @ScriptArgument(name = "port", type = "int", description = "bind port") })
	public void close(String[] args) {
		try {
			int port = Integer.valueOf(args[0]);
			server.close(new InetSocketAddress(port));
			context.println("closed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken webconsole: cannot close port", e);
		}
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

	@ScriptUsage(description = "add filesystem static resource context", arguments = {
			@ScriptArgument(name = "prefix", type = "string", description = "path prefix"),
			@ScriptArgument(name = "filesystem path", type = "string", description = "filesystem path") })
	public void addResourceContext(String[] args) {
		String prefix = args[0];
		String basePath = args[1];

		try {
			staticResourceApi.register(prefix, new FileResourceServlet(new File(basePath)));
			context.println("context added");
		} catch (Exception e) {
			context.println("cannot register filesystem context: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "remove static resource context", arguments = { @ScriptArgument(name = "prefix", type = "string", description = "path prefix") })
	public void removeResourceContext(String[] args) {
		String prefix = args[0];

		try {
			staticResourceApi.unregister(prefix);
			context.println("context removed");
		} catch (Exception e) {
			context.println("cannot remove prefix " + prefix);
		}
	}
}
