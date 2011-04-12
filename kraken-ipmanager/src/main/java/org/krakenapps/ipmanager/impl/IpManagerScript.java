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
package org.krakenapps.ipmanager.impl;

import java.util.List;

import org.krakenapps.api.DefaultScript;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.ipmanager.ArpScanner;
import org.krakenapps.ipmanager.IpManager;
import org.krakenapps.ipmanager.IpMonitor;
import org.krakenapps.ipmanager.IpQueryCondition;
import org.krakenapps.ipmanager.LogQueryCondition;
import org.krakenapps.ipmanager.model.Agent;
import org.krakenapps.ipmanager.model.HostEntry;
import org.krakenapps.ipmanager.model.IpEntry;
import org.krakenapps.ipmanager.model.IpEventLog;
import org.krakenapps.lookup.mac.MacLookupService;
import org.krakenapps.lookup.mac.Vendor;

public class IpManagerScript extends DefaultScript {
	private IpManager ipManager;
	private IpMonitor ipMonitor;
	private ArpScanner arpScanner;
	private MacLookupService macLookup;

	public IpManagerScript(IpManager ipManager, IpMonitor ipMonitor, ArpScanner arpScanner, MacLookupService macLookup) {
		this.ipManager = ipManager;
		this.ipMonitor = ipMonitor;
		this.arpScanner = arpScanner;
		this.macLookup = macLookup;
	}

	@ScriptUsage(description = "list all agents", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void agents(String[] args) {
		int orgId = Integer.valueOf(args[0]);
		List<Agent> agents = ipManager.getAgents(orgId);

		context.println("Agents");
		context.println("--------");

		for (Agent agent : agents)
			context.println(agent);
	}

	@ScriptUsage(description = "list all hosts", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void hosts(String[] args) {
		int orgId = Integer.valueOf(args[0]);
		List<HostEntry> hosts = ipManager.getHosts(orgId);

		context.println("Hosts");
		context.println("-------");

		for (HostEntry host : hosts)
			context.println(host);
	}

	@ScriptUsage(description = "list all ip entries", arguments = { @ScriptArgument(type = "int", name = "org id", description = "organization id") })
	public void iplist(String[] args) {
		int orgId = Integer.valueOf(args[0]);
		List<IpEntry> ipEntries = ipManager.getIpEntries(new IpQueryCondition(orgId));

		context.println("IP Entries");
		context.println("------------");

		for (IpEntry ip : ipEntries) {
			Vendor vendor = macLookup.findByMac(ip.getCurrentMac());
			String line = ip.toString();
			if (vendor != null)
				line += ", vendor=" + vendor.getName();

			context.println(line);
		}
	}

	public void arptimeout(String[] args) {
		if (args.length == 0) {
			int timeout = arpScanner.getTimeout();
			context.println(timeout + "msec");
		} else if (args.length == 1) {
			int timeout = Integer.valueOf(args[1]);
			arpScanner.setTimeout(timeout);
			context.println("set");
		}
	}

	public void arpscan(String[] args) {
		arpScanner.run();
		context.println("arp scan completed");
	}

	@ScriptUsage(description = "list all logs", arguments = {
			@ScriptArgument(type = "int", name = "org id", description = "organization id "),
			@ScriptArgument(type = "int", name = "page", description = "page number"),
			@ScriptArgument(type = "int", name = "page size", description = "page size") })
	public void logs(String[] args) {
		int orgId = Integer.valueOf(args[0]);
		int page = Integer.valueOf(args[1]);
		int pageSize = Integer.valueOf(args[2]);

		LogQueryCondition condition = new LogQueryCondition(orgId, page, pageSize);
		List<IpEventLog> logs = ipManager.getLogs(condition);

		context.println("IP Event Logs");
		context.println("--------------");

		for (IpEventLog log : logs)
			context.println(log);
	}
}
