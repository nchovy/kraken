/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.sentry.Base;
import org.krakenapps.sentry.ConnectionWatchdog;
import org.krakenapps.sentry.Sentry;
import org.krakenapps.sentry.SentryRpcService;

public class SentryScript implements Script {
	private Sentry sentry;
	private SentryRpcService rpc;
	private ConnectionWatchdog watchdog;

	private ScriptContext context;

	public SentryScript(Sentry sentry, SentryRpcService rpc, ConnectionWatchdog watchdog) {
		this.sentry = sentry;
		this.rpc = rpc;
		this.watchdog = watchdog;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "print current guid")
	public void guid(String[] args) {
		if (sentry.getGuid() == null)
			context.println("guid not found. sentry.setGuid first");
		else
			context.println(sentry.getGuid());
	}

	@ScriptUsage(description = "set sentry guid", arguments = { @ScriptArgument(name = "guid", type = "string", description = "sentry guid") })
	public void setGuid(String[] args) {
		sentry.setGuid(args[0]);
		context.println("ok");
	}

	@ScriptUsage(description = "list all sentry methods")
	public void methods(String[] args) {
		context.println("Sentry Methods");
		context.println("----------------------");
		for (String alias : rpc.getMethods()) {
			context.println(alias);
		}
	}

	@ScriptUsage(description = "list all live connections")
	public void connections(String[] args) {
		context.println("Connections");
		context.println("--------------------");

		for (String baseName : sentry.getCommandSessionNames()) {
			RpcSession commandSession = sentry.getCommandSession(baseName);
			RpcConnection conn = commandSession.getConnection();
			context.println("[" + baseName + "] " + conn.toString());
		}
	}

	@ScriptUsage(description = "list all bases")
	public void bases(String[] args) {
		context.println("Base List");
		context.println("-----------------");

		for (Base base : sentry.getBases()) {
			context.println(base.toString());
		}
	}

	@ScriptUsage(description = "add base", arguments = {
			@ScriptArgument(name = "name", type = "string", description = "base name"),
			@ScriptArgument(name = "ip", type = "string", description = "base ip"),
			@ScriptArgument(name = "port", type = "int", description = "base port"),
			@ScriptArgument(name = "key alias", type = "string", description = "the key alias of pkcs#12 key"),
			@ScriptArgument(name = "trust alias", type = "string", description = "the trust alias of x509 ca key") })
	public void addBase(String[] args) {
		try {
			String name = args[0];
			InetAddress ip = InetAddress.getByName(args[1]);
			int port = Integer.parseInt(args[2]);
			String keyAlias = args[3];
			String trustAlias = args[4];

			InetSocketAddress address = new InetSocketAddress(ip, port);
			Base base = new BaseConfig(name, address, keyAlias, trustAlias);

			sentry.addBase(base);
			watchdog.checkNow();
			context.println("base added");
		} catch (UnknownHostException e) {
			context.println("erorr: invalid ip address");
		} catch (IllegalStateException e) {
			context.println("error: duplicated base name");
		}
	}

	@ScriptUsage(description = "remove base", arguments = { @ScriptArgument(name = "name", type = "string", description = "base name") })
	public void removeBase(String[] args) {
		try {
			String baseName = args[0];
			sentry.removeBase(baseName);
			watchdog.checkNow();
			context.println("base removed");
		} catch (IllegalStateException e) {
			context.println("base not found");
		}
	}
}
