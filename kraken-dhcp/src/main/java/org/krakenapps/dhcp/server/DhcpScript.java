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
package org.krakenapps.dhcp.server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.dhcp.DhcpOptionCode;
import org.krakenapps.dhcp.DhcpServer;
import org.krakenapps.dhcp.MacAddress;
import org.krakenapps.dhcp.model.DhcpFilter;
import org.krakenapps.dhcp.model.DhcpIpGroup;
import org.krakenapps.dhcp.model.DhcpIpLease;
import org.krakenapps.dhcp.model.DhcpIpReservation;
import org.krakenapps.dhcp.model.DhcpOptionConfig;

public class DhcpScript implements Script {
	private DhcpServer server;
	private ScriptContext context;

	public DhcpScript(DhcpServer server) {
		this.server = server;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void groups(String[] args) {
		context.println("IP Groups");
		context.println("------------");

		for (DhcpIpGroup group : server.getIpGroups()) {
			context.println(group);
		}
	}

	@ScriptUsage(description = "add new ip group", arguments = {
			@ScriptArgument(type = "string", name = "name", description = "group name"),
			@ScriptArgument(type = "string", name = "from", description = "ip range from"),
			@ScriptArgument(type = "string", name = "to", description = "ip range to") })
	public void createGroup(String[] args) {
		try {
			// adding basic options
			context.print("Server IP? ");
			InetAddress serverIp = InetAddress.getByName(context.readLine());
			context.print("Subnet Mask? ");
			InetAddress subnet = InetAddress.getByName(context.readLine());
			context.print("Router IP? ");
			InetAddress router = InetAddress.getByName(context.readLine());
			context.print("DNS IP? ");
			InetAddress dns = InetAddress.getByName(context.readLine());
			context.print("Lease Duration (secs)? ");
			int leaseDuration = Integer.valueOf(context.readLine());

			DhcpIpGroup group = buildIpGroup(args);
			server.createIpGroup(group);
			context.println("created group");

			server.createGroupOption(new DhcpOptionConfig(group.getName(), 1, subnet.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 54, serverIp.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 3, router.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 6, dns.getHostAddress()));
			server.createGroupOption(new DhcpOptionConfig(group.getName(), 51, Integer.toString(leaseDuration)));
			context.println("created required options");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "update ip group", arguments = {
			@ScriptArgument(type = "string", name = "name", description = "group name"),
			@ScriptArgument(type = "string", name = "from", description = "ip range from"),
			@ScriptArgument(type = "string", name = "to", description = "ip range to") })
	public void updateGroup(String[] args) {
		try {
			DhcpIpGroup group = buildIpGroup(args);
			server.updateIpGroup(group);
			context.println("updated");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private DhcpIpGroup buildIpGroup(String[] args) throws UnknownHostException {
		DhcpIpGroup group = new DhcpIpGroup();
		group.setName(args[0]);
		group.setFrom(Inet4Address.getByName(args[1]));
		group.setTo(Inet4Address.getByName(args[2]));
		return group;
	}

	@ScriptUsage(description = "remove ip group", arguments = { @ScriptArgument(type = "string", name = "group name", description = "group name") })
	public void removeGroup(String[] args) {
		try {
			server.removeIpGroup(args[0]);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "list dhcp options of ip group", arguments = { @ScriptArgument(type = "string", name = "group name", description = "ip group name") })
	public void groupConfigs(String[] args) {
		String groupName = args[0];

		context.println("DHCP Options");
		context.println("--------------");
		for (DhcpOptionConfig c : server.getGroupOptions(groupName)) {
			DhcpOptionCode code = DhcpOptionCode.from(c.getType());
			context.println("[" + c.getId() + "] " + code.name() + ": " + c.getValue());
		}
	}

	@ScriptUsage(description = "create dhcp option", arguments = {
			@ScriptArgument(type = "string", name = "group name", description = "ip group name"),
			@ScriptArgument(type = "int", name = "option type", description = "dhcp option code, try dhcp.options for available code list"),
			@ScriptArgument(type = "string", name = "option value", description = "dhcp option value") })
	public void createGroupConfig(String[] args) {
		try {
			DhcpOptionConfig config = new DhcpOptionConfig();
			config.setGroupName(args[0]);
			config.setType(Integer.valueOf(args[1]));
			config.setValue(args[2]);
			server.createGroupOption(config);
			context.println("added");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "remove dhcp option", arguments = { @ScriptArgument(type = "int", name = "option id", description = "dhcp option id") })
	public void removeGroupConfig(String[] args) {
		try {
			Integer id = Integer.valueOf(args[0]);
			server.removeGroupOption(id);
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	public void offers(String[] args) {
		context.println("IP Offers");
		context.println("------------");

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (DhcpIpLease offer : server.getIpOffers()) {
			context.println(offer.getMac() + ", " + offer.getIp().getHostAddress() + ", created="
					+ dateFormat.format(offer.getCreated()));
		}
	}

	public void leases(String[] args) {
		context.println("IP Leases");
		context.println("-----------");

		for (DhcpIpGroup group : server.getIpGroups()) {
			context.println(group.getName());
			List<DhcpIpLease> leases = server.getIpLeases(group.getName());
			for (DhcpIpLease lease : leases) {
				context.println(" * " + lease);
			}

			context.println("");
		}
	}

	@ScriptUsage(description = "purge ip lease", arguments = { @ScriptArgument(type = "string", name = "target", description = "'all' or multiple ip addresses") })
	public void purgeLease(String[] args) {
		try {
			if (args[0].equals("all"))
				server.purgeIpLease();

			for (String arg : args) {
				server.purgeIpLease(InetAddress.getByName(arg));
			}

			context.println("purged");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "list all ip reservations", arguments = { @ScriptArgument(type = "string", name = "group name", description = "group name") })
	public void reservations(String[] args) {
		List<DhcpIpReservation> reservations = server.getIpReservations(args[0]);

		context.println("IP Reservations");
		context.println("------------------");
		for (DhcpIpReservation reservation : reservations) {
			context.println(reservation);
		}
	}

	@ScriptUsage(description = "reserve ip", arguments = {
			@ScriptArgument(type = "string", name = "group name", description = "group name"),
			@ScriptArgument(type = "string", name = "ip", description = "ip entry"),
			@ScriptArgument(type = "string", name = "mac", description = "mac"),
			@ScriptArgument(type = "string", name = "host name", description = "host name") })
	public void reserve(String[] args) {
		try {
			String groupName = args[0];
			InetAddress ip = Inet4Address.getByName(args[1]);
			MacAddress mac = new MacAddress(args[2]);
			String hostName = args[3];

			DhcpIpReservation entry = new DhcpIpReservation(groupName, ip, mac, hostName);
			server.reserve(entry);
			context.println("ip reserved");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "unreserve ip", arguments = {
			@ScriptArgument(type = "string", name = "group name", description = "group name"),
			@ScriptArgument(type = "string", name = "ip", description = "ip entry") })
	public void unreserve(String[] args) {
		try {
			String groupName = args[0];
			InetAddress ip = Inet4Address.getByName(args[1]);

			DhcpIpReservation entry = new DhcpIpReservation(groupName, ip, null, null);
			server.unreserve(entry);
			context.println("unreserved");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	public void filters(String[] args) {
		context.println("Allowed Hosts");
		context.println("--------------");

		for (DhcpFilter f : server.getAllowFilters()) {
			context.println(f.getMac() + ", " + f.getDescription());
		}

		context.println("");
		context.println("Blocked Hosts");
		context.println("--------------");

		for (DhcpFilter f : server.getBlockFilters()) {
			context.println(f.getMac() + ", " + f.getDescription());
		}
	}

	@ScriptUsage(description = "allow mac", arguments = {
			@ScriptArgument(type = "string", name = "mac", description = "mac address (aa:bb:cc:dd:ee:ff format)"),
			@ScriptArgument(type = "string", name = "description", description = "filter description", optional = true) })
	public void allow(String[] args) {
		try {
			DhcpFilter filter = new DhcpFilter();
			filter.setMac(new MacAddress(args[0]));
			filter.setDescription(args.length > 1 ? args[1] : null);
			filter.setAllow(true);

			server.createFilter(filter);
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "remove mac from allow list", arguments = { @ScriptArgument(type = "string", name = "mac", description = "mac address") })
	public void disallow(String[] args) {
		try {
			server.removeFilter(new MacAddress(args[0]));
			context.println("removed");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "block mac", arguments = {
			@ScriptArgument(type = "string", name = "mac", description = "mac address (aa:bb:cc:dd:ee:ff format)"),
			@ScriptArgument(type = "string", name = "description", description = "filter description", optional = true) })
	public void block(String[] args) {
		try {
			DhcpFilter filter = new DhcpFilter();
			filter.setMac(new MacAddress(args[0]));
			filter.setDescription(args.length > 1 ? args[1] : null);
			filter.setAllow(false);

			server.createFilter(filter);
			context.println("blocked");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "remove mac from allow list", arguments = { @ScriptArgument(type = "string", name = "mac", description = "mac address") })
	public void unblock(String[] args) {
		try {
			server.removeFilter(new MacAddress(args[0]));
			context.println("unblocked");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "list all available options", arguments = { @ScriptArgument(type = "string", name = "filter", description = "filter", optional = true) })
	public void options(String[] args) {
		String filter = null;
		if (args.length > 0)
			filter = args[0];

		context.println("DHCP Option Codes");
		context.println("-------------------");

		for (DhcpOptionCode c : DhcpOptionCode.values()) {
			String valueType = "";
			if (c.getValueType() != null)
				valueType = ": " + c.getValueType().getSimpleName();

			if (filter != null && !c.name().toLowerCase().contains(filter.toLowerCase()))
				continue;

			context.println("(" + c.code() + ") " + c.name() + valueType);
		}
	}
}
