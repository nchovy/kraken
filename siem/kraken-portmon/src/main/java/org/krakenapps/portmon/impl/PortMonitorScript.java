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
package org.krakenapps.portmon.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.portmon.PortEventListener;
import org.krakenapps.portmon.PortMonitor;
import org.krakenapps.portmon.PortStatus;

public class PortMonitorScript implements Script {
	private PortMonitor monitor;
	private ScriptContext context;

	public PortMonitorScript(PortMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void tcpTargets(String[] args) {
		context.println("Tcp Monitoring Targets");
		context.println("------------------------");
		for (InetSocketAddress target : monitor.getTcpTargets()) {
			PortStatus status = monitor.getTcpPortStatus(target);
			context.println(target + " => " + status.toString());
		}
	}

	@ScriptUsage(description = "add tcp monitoring target", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "target hostname or ip"),
			@ScriptArgument(name = "port", type = "int", description = "target tcp port") })
	public void addTcpTarget(String[] args) {
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			int port = Integer.parseInt(args[1]);

			if (port < 1 || port > 65535)
				throw new NumberFormatException();

			monitor.addTcpTarget(new InetSocketAddress(ip, port));
			context.println("target added");
		} catch (NumberFormatException e) {
			context.println("invalid port number");
		} catch (UnknownHostException e) {
			context.println("unknown host");
		}
	}

	@ScriptUsage(description = "remove tcp monitoring target", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "target hostname or ip"),
			@ScriptArgument(name = "port", type = "int", description = "target tcp port") })
	public void removeTcpTarget(String[] args) {
		try {
			InetAddress ip = InetAddress.getByName(args[0]);
			int port = Integer.parseInt(args[1]);

			if (port < 1 || port > 65535)
				throw new NumberFormatException();

			monitor.removeTcpTarget(new InetSocketAddress(ip, port));
			context.println("target removed");
		} catch (NumberFormatException e) {
			context.println("invalid port number");
		} catch (UnknownHostException e) {
			context.println("unknown host");
		}
	}

	public void trace(String[] args) {
		PortScanTracer tracer = new PortScanTracer();
		monitor.addListener(tracer);
		try {
			while (true) {
				context.readLine();
			}
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			monitor.removeListener(tracer);
		}
	}

	private class PortScanTracer implements PortEventListener {
		@Override
		public void onConnect(InetSocketAddress target, int connectTime) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
			context.printf("%s %s connected, elapsed time %d\n", dateFormat.format(new Date()), target, connectTime);
		}

		@Override
		public void onConnectRefused(InetSocketAddress target, int timeout, IOException e) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
			context.printf("%s %s connect refused, timeout %d\n", dateFormat.format(new Date()), target, timeout);
		}
	}
}
