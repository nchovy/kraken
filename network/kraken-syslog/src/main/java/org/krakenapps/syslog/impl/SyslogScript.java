/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.syslog.impl;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.syslog.SyslogProfile;
import org.krakenapps.syslog.SyslogServer;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(SyslogScript.class.getName());
	private ScriptContext context;
	private SyslogServerRegistry syslogRegistry;

	public SyslogScript(SyslogServerRegistry syslogRegistry) {
		this.syslogRegistry = syslogRegistry;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void servers(String[] args) {
		context.println("Syslog Servers");
		context.println("----------------");

		for (String name : syslogRegistry.getNames()) {
			SyslogServer server = syslogRegistry.getServer(name);
			context.println(server);
		}
	}

	@ScriptUsage(description = "open persistent syslog server", arguments = {
			@ScriptArgument(name = "server name", type = "string", description = "unique server name"),
			@ScriptArgument(name = "port", type = "int", description = "syslog port number", optional = true),
			@ScriptArgument(name = "address", type = "string", description = "syslog bind address. 0.0.0.0 by default", optional = true),
			@ScriptArgument(name = "charset", type = "string", description = "character set name. utf-8 by default", optional = true),
			@ScriptArgument(name = "queue size", type = "int", description = "buffering queue size. 20000 by default", optional = true) })
	public void open(String[] args) {
		SyslogProfile profile = new SyslogProfile();
		try {
			String name = args[0];
			if (syslogRegistry.getServer(name) != null) {
				context.println("duplicated name. use other name.");
				return;
			}

			int port = 514;
			if (args.length > 1)
				port = Integer.valueOf(args[1]);

			String host = "0.0.0.0";
			if (args.length > 2)
				host = args[2];

			String charsetName = "utf-8";
			if (args.length > 3)
				charsetName = args[3];

			int queueSize = 20000;
			if (args.length > 4)
				queueSize = Integer.valueOf(args[4]);

			InetAddress.getByName(host);

			if (port < 1 || port > 65535) {
				context.println("invalid port number.");
				return;
			}

			// check charset
			Charset.forName(charsetName);

			profile.setName(name);
			profile.setAddress(host);
			profile.setPort(port);
			profile.setCharset(charsetName);
			profile.setQueueSize(queueSize);

			syslogRegistry.open(profile);
			context.println("opened " + profile.getListenAddress());
		} catch (UnknownHostException e) {
			context.println("invalid syslog bind address.");
		} catch (NumberFormatException e) {
			context.println("invalid port format. integer required.");
		} catch (IllegalCharsetNameException e) {
			context.println("illegal charset name.");
		} catch (UnsupportedCharsetException e) {
			context.println("unsupported charset name.");
		} catch (IllegalThreadStateException e) {
			context.println("thread already started.");
		} catch (SocketException e) {
			context.println("cannot open server: " + e.getMessage());
			logger.error("kraken syslog: cannot open server with " + profile, e);
		}
	}

	@ScriptUsage(description = "close syslog server", arguments = { @ScriptArgument(name = "server name", type = "string", description = "the name of syslog server instance") })
	public void close(String[] args) {
		String name = args[0];
		SyslogServer server = syslogRegistry.getServer(name);
		if (server == null) {
			context.println("server not found");
			return;
		}

		syslogRegistry.close(name);
		context.println("closed");
	}

	@ScriptUsage(description = "trace a syslog receiver.", arguments = { @ScriptArgument(name = "server name", type = "string", description = "the name of syslog server instance") })
	public void trace(String[] args) {
		String name = args[0];
		SyslogServer server = syslogRegistry.getServer(name);
		if (server == null) {
			context.println("server not found.");
			return;
		}

		SyslogTracer tracer = new SyslogTracer();
		tracer.setScriptContext(context);
		server.addListener(tracer);

		context.println("press ctrl-c to stop");
		context.println("------------------------");

		try {
			while (true) {
				context.readLine();
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			server.removeListener(tracer);
		}
	}
}
