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
package org.krakenapps.fluxmon.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.fluxmon.FluxDomain;
import org.krakenapps.fluxmon.FluxHost;
import org.krakenapps.fluxmon.FluxMonitor;
import org.krakenapps.geoip.GeoIpService;

public class FluxScript implements Script {
	private ScriptContext context;
	private FluxMonitor monitor;
	private GeoIpService geoip;

	public FluxScript(FluxMonitor monitor, GeoIpService geoip) {
		this.monitor = monitor;
		this.geoip = geoip;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "all tracking domains", arguments = { @ScriptArgument(name = "filter", type = "string", description = "domain filter text", optional = true) })
	public void trackings(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("Tracking domains");
		context.println("-------------------");

		for (String domain : monitor.getTrackingDomains()) {
			if (filter != null && !domain.contains(filter))
				continue;

			int size = monitor.getDomain(domain).getHosts().size();
			context.printf("%s: %d\n", domain, size);
		}
	}

	@ScriptUsage(description = "all flux domains", arguments = { @ScriptArgument(name = "filter", type = "string", description = "domain filter text", optional = true) })
	public void domains(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("All flux domain stats");
		context.println("-----------------------");

		for (FluxDomain fluxDomain : monitor.getAllDomains()) {
			String domain = fluxDomain.getName();
			
			if (filter != null && !domain.contains(filter))
				continue;

			int size = fluxDomain.getHosts().size();
			context.printf("%s: %d\n", domain, size);
		}
	}

	@ScriptUsage(description = "add domain to flux tracker", arguments = { @ScriptArgument(name = "domain", type = "string", description = "domain to track") })
	public void addTracking(String[] args) {
		monitor.addTrackingDomain(args[0]);
		context.println("domain added");
	}

	@ScriptUsage(description = "remove domain from flux tracker", arguments = { @ScriptArgument(name = "domain", type = "string", description = "untracking domain") })
	public void removeTracking(String[] args) {
		monitor.removeTrackingDomain(args[0]);
		context.println("domain removed");
	}

	@ScriptUsage(description = "print all flux hosts", arguments = {
			@ScriptArgument(name = "domain", type = "string", description = "flux domain name"),
			@ScriptArgument(name = "country code", type = "string", description = "country code", optional = true) })
	public void hosts(String[] args) {
		FluxDomain domain = monitor.getDomain(args[0]);
		if (domain == null) {
			context.println("record not found");
			return;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (FluxHost host : domain.getHosts()) {
			context.printf("ip=%s, created=%s, updated=%s\n", host.getAddress().getHostAddress(), dateFormat.format(host
					.getCreateDateTime()), dateFormat.format(host.getUpdateDateTime()));
		}
	}

	@ScriptUsage(description = "country statistics for past 24 hours", arguments = {
			@ScriptArgument(name = "limit", type = "int", description = "top limit count"),
			@ScriptArgument(name = "domain", type = "string", description = "flux domain name", optional = true) })
	public void topCountry(String[] args) {
		String domainFilter = null;
		int limit = 0;

		try {
			limit = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			context.println("invalid limit number format");
			return;
		}

		if (args.length > 1)
			domainFilter = args[1];

		Date activeFilter = new Date(new Date().getTime() - 86400000); // 1day before
		List<FluxCountryStat> stats = FluxStatistics.getSortedCountryStats(monitor, geoip, activeFilter, domainFilter);

		int i = 0;
		for (FluxCountryStat stat : stats) {
			if (i++ >= limit)
				break;

			context.println(stat.toString());
		}
	}

	@ScriptUsage(description = "force run")
	public void run(String[] args) {
		monitor.run();
		context.println("completed");
	}

}
