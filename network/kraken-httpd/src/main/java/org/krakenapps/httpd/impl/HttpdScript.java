/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.httpd.impl;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.httpd.FileResourceServlet;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.httpd.VirtualHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpdScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(HttpdScript.class.getName());
	private ScriptContext context;
	private HttpService httpd;

	public HttpdScript(HttpService httpd) {
		this.httpd = httpd;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "print all http contexts")
	public void contexts(String[] args) {
		context.println("HTTP Contexts");
		context.println("---------------");
		HttpContextRegistry registry = httpd.getContextRegistry();
		for (String name : registry.getContextNames()) {
			HttpContext ctx = registry.findContext(name);
			context.println(ctx);
			context.println("");
		}
	}

	@ScriptUsage(description = "remove http contexts", arguments = { @ScriptArgument(name = "context name", type = "string", description = "http context name") })
	public void removeContext(String[] args) {
		HttpContextRegistry registry = httpd.getContextRegistry();
		registry.removeContext(args[0]);
	}

	@ScriptUsage(description = "get port bindings")
	public void bindings(String[] args) {
		context.println("Port Bindings");
		context.println("---------------------");
		for (InetSocketAddress binding : httpd.getListenAddresses()) {
			HttpServer server = httpd.getServer(binding);
			HttpConfiguration config = server.getConfiguration();
			context.println(config);
		}
	}

	@ScriptUsage(description = "open port", arguments = { @ScriptArgument(name = "port", type = "int", description = "bind port") })
	public void open(String[] args) {
		try {
			int port = Integer.valueOf(args[0]);

			HttpConfiguration config = new HttpConfiguration(new InetSocketAddress(port));
			HttpServer server = httpd.createServer(config);
			server.open();
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

			InetSocketAddress listen = new InetSocketAddress(port);

			HttpConfiguration config = new HttpConfiguration(listen, keyAlias, trustAlias);			
			HttpServer server = httpd.createServer(config);
			server.addVirtualHost(new VirtualHost());
			server.open();
			context.println("opened https server");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken webconsole: cannot open ssl port");
		}
	}

	@ScriptUsage(description = "add virtual host", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "bind port"),
			@ScriptArgument(name = "http context", type = "string", description = "http context name"),
			@ScriptArgument(name = "host name pattern", type = "string", description = "host name pattern", optional = true) })
	public void addVirtualHost(String[] args) {
		int port = Integer.valueOf(args[0]);
		InetSocketAddress listen = new InetSocketAddress(port);
		HttpServer server = httpd.getServer(listen);
		if (server == null) {
			context.println("http server not found");
			return;
		}

		String hostNamePattern = ".*";
		if (args.length > 2)
			hostNamePattern = args[2];

		VirtualHost v = new VirtualHost();
		v.setHttpContextName(args[1]);
		v.setHostNames(Arrays.asList(hostNamePattern));
		server.addVirtualHost(v);
		context.println("added");
	}

	@ScriptUsage(description = "remove virtual host", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "bind port"),
			@ScriptArgument(name = "http context", type = "string", description = "http context name") })
	public void removeVirtualHost(String[] args) {
		int port = Integer.valueOf(args[0]);
		InetSocketAddress listen = new InetSocketAddress(port);
		HttpServer server = httpd.getServer(listen);
		if (server == null) {
			context.println("http server not found");
			return;
		}

		server.removeVirtualHost(args[1]);
		context.println("removed");
	}

	@ScriptUsage(description = "close port", arguments = {
			@ScriptArgument(name = "listen port", type = "int", description = "bind port"),
			@ScriptArgument(name = "listen addr", type = "string", description = "bind address", optional = true) })
	public void close(String[] args) {
		try {
			int port = Integer.valueOf(args[0]);
			InetSocketAddress listen = null;
			if (args.length >= 2)
				listen = new InetSocketAddress(args[1], port);
			else
				listen = new InetSocketAddress(port);

			httpd.removeServer(listen);
			context.println("closed");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken webconsole: cannot close port", e);
		}
	}

	@ScriptUsage(description = "attach filesystem path", arguments = {
			@ScriptArgument(name = "context", type = "string", description = "http context name"),
			@ScriptArgument(name = "prefix", type = "string", description = "path prefix"),
			@ScriptArgument(name = "filesystem path", type = "string", description = "filesystem path") })
	public void attachDirectory(String[] args) {
		String prefix = args[1];
		String basePath = args[2];

		HttpContext ctx = httpd.ensureContext(args[0]);

		try {
			File target = new File(basePath);
			if (!target.exists()) {
				context.println("directory does not exists: " + target.getAbsolutePath());
				return;
			}

			if (!target.isDirectory()) {
				context.println("filesystem path is not a directory");
				return;
			}

			ServletContext servletContext = ctx.getServletContext();
			servletContext.addServlet("file", new FileResourceServlet(target));
			ServletRegistration r = servletContext.getServletRegistration("file");
			r.addMapping(prefix + "/*");
			context.println("attached");
		} catch (Throwable t) {
			context.println("cannot attach filesystem path: " + t.getMessage());
			logger.error("kraken httpd: cannot attach filesystem path", t);
		}
	}

	@ScriptUsage(description = "remove servlet", arguments = {
			@ScriptArgument(name = "context", type = "string", description = "context name"),
			@ScriptArgument(name = "servlet", type = "string", description = "servlet name") })
	public void removeServlet(String[] args) {
		String contextName = args[0];
		String servletName = args[1];

		if (httpd != null) {
			HttpContext ctx = httpd.ensureContext(contextName);
			if (ctx == null) {
				context.println("Context name" + contextName + "not Exist");
				return;
			}

			ctx.removeServlet(servletName);
		}
	}
}
