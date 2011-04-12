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
package org.krakenapps.syslog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.filter.ComponentDescription;
import org.krakenapps.filter.Filter;
import org.krakenapps.filter.FilterManager;
import org.krakenapps.filter.Message;
import org.krakenapps.filter.MessageBuilder;
import org.krakenapps.filter.DefaultMessageSpec;
import org.krakenapps.filter.exception.AlreadyBoundException;
import org.krakenapps.filter.exception.ConfigurationException;
import org.krakenapps.filter.exception.DuplicatedFilterNameException;
import org.krakenapps.filter.exception.FilterNotBoundException;
import org.krakenapps.filter.exception.FilterNotFoundException;
import org.krakenapps.filter.exception.FilterFactoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(SyslogScript.class.getName());
	private ScriptContext context;
	private FilterManager filterManager;

	public SyslogScript(FilterManager filterManager) {
		this.filterManager = filterManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "load new syslog sender", arguments = {
			@ScriptArgument(name = "alias", type = "string", description = "new filter name"),
			@ScriptArgument(name = "address", type = "string", description = "remote host ip address"),
			@ScriptArgument(name = "port", type = "int", description = "remote host port"),
			@ScriptArgument(name = "facility", type = "int", description = "facility number (default: local0)", optional = true),
			@ScriptArgument(name = "encoding", type = "string", description = "encoding name (default: utf-8)", optional = true) })
	public void loadSender(String[] args) {
		try {
			String name = args[0];
			String address = args[1];
			InetAddress.getByName(address);

			String port = args[2];
			Integer.parseInt(port);

			String facility = "16";
			if (args.length >= 4)
				facility = args[3];

			String encoding = "utf-8";
			if (args.length >= 5)
				encoding = args[4];

			filterManager.loadFilter("org.krakenapps.syslog.SyslogSender", name);
			filterManager.setProperty(name, "address", address);
			filterManager.setProperty(name, "port", port);
			filterManager.setProperty(name, "facility", facility);
			filterManager.setProperty(name, "encoding", encoding);
			filterManager.runFilter(name, 0);
			context.println("sender loaded");
		} catch (UnknownHostException e) {
			context.println("check address format");
		} catch (NumberFormatException e) {
			context.println("port should be number");
		} catch (Exception e) {
			context.println("load failed: " + e.getMessage());
			logger.warn("kraken-syslog: sender load failed", e);
		}

	}

	public void list(String[] args) {
		context.println("==========================");
		context.println(" Syslog Receiver List");
		context.println("==========================");

		List<ComponentDescription> filterDescs = filterManager.getFilterInstanceDescriptions();
		for (ComponentDescription filterDesc : filterDescs) {
			String pid = filterDesc.getInstanceName();
			Filter filter = filterManager.getFilter(pid);
			if (filter == null)
				continue;

			if (filter instanceof SyslogReceiver) {
				String address = (String) filter.getProperty("address");
				String port = (String) filter.getProperty("port");
				String encoding = (String) filter.getProperty("charset");
				context.print("filter name = " + pid);
				if (address != null && port != null)
					context.print(", listen = " + address + ":" + port);

				if (encoding != null)
					context.print(", encoding = " + encoding);

				context.println("");
			}
		}
	}

	@ScriptUsage(description = "send a syslog message. (utf-8 encoded)", arguments = {
			@ScriptArgument(name = "address", type = "string", description = "the name of remote address"),
			@ScriptArgument(name = "port", type = "string", description = "the remote port number"),
			@ScriptArgument(name = "message", type = "string", description = "the message") })
	public void send(String[] args) {
		try {
			SyslogSender sender = new SyslogSender();
			sender.setProperty("address", args[0]);
			sender.setProperty("port", args[1]);
			sender.open();

			StringBuilder buffer = new StringBuilder(1024);
			for (int i = 2; i < args.length; i++) {
				buffer.append(args[i]);
				if (i != args.length - 1)
					buffer.append(" ");
			}

			MessageBuilder builder = new MessageBuilder(new DefaultMessageSpec("kraken.syslog.sender", 1, 0));
			builder.set("message", buffer.toString());
			Message message = builder.build();
			sender.process(message);
			sender.run();
			sender.close();
		} catch (ConfigurationException e) {
			context.printf("config error: %s %s\n", e.getConfigurationName(), e.getErrorMessage());
		} catch (InterruptedException e) {
			// ignore
		}
	}

	@ScriptUsage(description = "load a syslog receiver instance.")
	public void load(String[] args) {
		try {
			context.print("syslog receiver name: ");
			String pid = context.readLine();
			if (pid.trim().length() == 0) {
				context.println("filter name required.");
				return;
			}

			if (filterManager.getFilter(pid) != null) {
				context.println("duplicated name. use other name.");
				return;
			}

			context.print("bind address (default to localhost): ");
			String host = context.readLine();
			if (host.trim().length() == 0)
				host = "localhost";

			context.print("bind port (default to 514): ");
			String port = context.readLine();
			if (port.trim().length() == 0)
				port = "514";

			context.print("charset (default to utf-8): ");
			String charsetName = context.readLine();
			if (charsetName.trim().length() == 0)
				charsetName = "utf-8";

			InetAddress.getByName(host);

			int portNumber = Integer.parseInt(port);
			if (portNumber < 1 || portNumber > 65535) {
				context.println("invalid port number.");
				return;
			}

			Charset.forName(charsetName);

			filterManager.loadFilter("org.krakenapps.syslog.SyslogReceiver", pid);
			SyslogReceiver receiver = (SyslogReceiver) filterManager.getFilter(pid);
			receiver.setProperty("address", host);
			receiver.setProperty("port", port);
			receiver.setProperty("charset", charsetName);
			filterManager.runFilter(pid, 0);
		} catch (InterruptedException e) {
			context.println("interrupted.");
		} catch (UnknownHostException e) {
			context.println("invalid syslog bind address.");
		} catch (NumberFormatException e) {
			context.println("invalid port format. integer required.");
		} catch (IllegalCharsetNameException e) {
			context.println("illegal charset name.");
		} catch (UnsupportedCharsetException e) {
			context.println("unsupported charset name.");
		} catch (FilterFactoryNotFoundException e) {
			context.println("syslog receiver class not found.");
		} catch (DuplicatedFilterNameException e) {
			context.println("duplicated filter name. use other name.");
		} catch (IllegalThreadStateException e) {
			context.println("thread already started.");
		} catch (FilterNotFoundException e) {
			context.println("syslog receiver not found");
		}
	}

	@ScriptUsage(description = "unload a syslog receiver instance.", arguments = { @ScriptArgument(name = "filter name", type = "string", description = "the name of syslog receiver instance") })
	public void unload(String[] args) {
		String pid = args[0];
		Filter filter = filterManager.getFilter(pid);
		if (filter == null) {
			context.println("syslog receiver not found.");
			return;
		}

		if (filter instanceof SyslogReceiver)
			filterManager.unloadFilter(pid);
		else
			context.println(pid + " is not syslog receiver.");
	}

	@ScriptUsage(description = "trace a syslog receiver.", arguments = { @ScriptArgument(name = "filter name", type = "string", description = "the name of syslog receiver instance") })
	public void trace(String[] args) {
		String filterName = args[0];
		Filter filter = filterManager.getFilter(filterName);
		if (filter == null) {
			context.println("filter not found.");
			return;
		}

		if ((filter instanceof SyslogReceiver) == false) {
			context.println("filter is not syslog receiver");
			return;
		}

		try {
			filterManager.loadFilter("org.krakenapps.syslog.SyslogTracer", "syslog-tracer");
			SyslogTracer tracer = (SyslogTracer) filterManager.getFilter("syslog-tracer");
			tracer.setScriptContext(context);
			filterManager.bindFilter(filterName, "syslog-tracer");
		} catch (FilterFactoryNotFoundException e) {
			context.println("syslog tracer type not found.");
		} catch (DuplicatedFilterNameException e) {
			context.println("filter name conflict occurred: syslog-tracer");
		} catch (AlreadyBoundException e) {
			// ignore
		} catch (Exception e) {
			context.println("trace failed.");
		}

		while (true) {
			try {
				context.readLine();
			} catch (InterruptedException e) {
				break;
			}
		}

		try {
			filterManager.unbindFilter(filterName, "syslog-tracer");
			filterManager.unloadFilter("syslog-tracer");
		} catch (FilterNotFoundException e) {
			context.println("tracer not found. shutdown failed.");
		} catch (FilterNotBoundException e) {
			// ignore
		}
	}
	
	public void status(String[] args) {
		context.println("==========================");
		context.println(" Syslog Receiver status");
		context.println("==========================");

		List<ComponentDescription> filterDescs = filterManager.getFilterInstanceDescriptions();
		for (ComponentDescription filterDesc : filterDescs) {
			String pid = filterDesc.getInstanceName();
			Filter filter = filterManager.getFilter(pid);
			if (filter == null)
				continue;

			if (filter instanceof SyslogReceiver) {
				String address = (String) filter.getProperty("address");
				String port = (String) filter.getProperty("port");
				String encoding = (String) filter.getProperty("charset");
				context.print("filter name = " + pid);
				if (address != null && port != null)
					context.print(", listen = " + address + ":" + port);

				if (encoding != null)
					context.print(", encoding = " + encoding);
				
				SyslogReceiver sr = (SyslogReceiver) filter;
				context.print(", queue size = " + sr.getQueueSize());

				context.println("");
			}
		}
	}
}
