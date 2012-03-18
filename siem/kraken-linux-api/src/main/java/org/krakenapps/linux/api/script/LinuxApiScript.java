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
package org.krakenapps.linux.api.script;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.linux.api.ArpCache;
import org.krakenapps.linux.api.ArpEntry;
import org.krakenapps.linux.api.ConnectionInformation;
import org.krakenapps.linux.api.CpuStat;
import org.krakenapps.linux.api.CpuUsage;
import org.krakenapps.linux.api.DnsConfig;
import org.krakenapps.linux.api.EthernetToolInformation;
import org.krakenapps.linux.api.KernelStat;
import org.krakenapps.linux.api.NicStat;
import org.krakenapps.linux.api.TcpConnectionInformation;
import org.krakenapps.linux.api.DnsConfig.Sortlist;
import org.krakenapps.linux.api.MemoryStat;
import org.krakenapps.linux.api.Process;
import org.krakenapps.linux.api.RoutingEntry;
import org.krakenapps.linux.api.RoutingEntry.Flag;
import org.krakenapps.linux.api.RoutingTable;
import org.krakenapps.linux.api.UdpConnectionInformation;
import org.krakenapps.linux.api.Wtmp;
import org.krakenapps.linux.api.WtmpEntry;

public class LinuxApiScript implements Script {
	private ScriptContext context;

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "cpu usage", arguments = { @ScriptArgument(name = "core number", type = "integer", description = "core number", optional = true) })
	public void cpu(String[] args) {
		CpuUsage usage = null;

		try {
			if (args.length > 0) {
				int core = Integer.parseInt(args[0]);
				usage = CpuStat.getCpuUsage(200, core);
			} else
				usage = CpuStat.getCpuUsage();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		context.println(String.format("Idle   : %d%%", usage.getIdle()));
		context.println(String.format("System : %d%%", usage.getSystem()));
		context.println(String.format("User   : %d%%", usage.getUser()));
	}

	public void memory(String[] args) {
		try {
			context.println(MemoryStat.getMemoryStat());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void process(String[] args) {
		List<Process> procs = Process.getProcesses();

		context.println("   pid  |       name      |    memory   ");
		context.println("--------+-----------------+-------------");
		for (Process proc : procs)
			context.println(String.format(" %6d | %-15s | %8d kb", proc.getPid(), proc.getName(), proc.getVmSize()));
	}

	public void kernel(String[] args) {
		try {
			KernelStat stat = KernelStat.getKernelStat();

			context.println("paged in              : " + stat.getPagedIn());
			context.println("paged out             : " + stat.getPagedOut());
			context.println("swap in               : " + stat.getSwapIn());
			context.println("swap out              : " + stat.getSwapOut());
			context.println("total intterupts      : " + stat.getTotalInterrupts());
			context.println("particular interrupts : " + stat.getParticularInterrupts());
			context.println("context               : " + stat.getContext());
			context.println("boot time             : " + stat.getBootTime());
			context.println("processes             : " + stat.getProcesses());
			context.println("running process       : " + stat.getRunningProcess());
			context.println("blocked process       : " + stat.getBlockedProcess());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void wtmp(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			List<WtmpEntry> entries = Wtmp.getEntries();

			context.println("     type     |         date        |  pid  |    user    |          host        ");
			context.println("--------------+---------------------+-------+------------+----------------------");
			for (WtmpEntry entry : entries) {
				context.println(String.format(" %-12s | %19s | %5d | %-10s | %-20s", entry.getType(),
						dateFormat.format(entry.getDate()), entry.getPid(), entry.getUser(), entry.getHost()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void tcpConnections(String[] args) {
		try {
			List<ConnectionInformation> infos = TcpConnectionInformation.getAllTcpInformations();

			context.println("                local                      port  |                    remote                 port  |  pid  ");
			context.println("-----------------------------------------+-------+-----------------------------------------+-------+-------");
			for (ConnectionInformation info : infos) {
				context.println(String.format(" %-39s | %-5d | %-39s | %-5d | %-5d", info.getLocal().getHostName(),
						info.getLocal().getPort(), info.getRemote().getHostName(), info.getRemote().getPort(),
						info.getPid()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void udpConnections(String[] args) {
		try {
			List<ConnectionInformation> infos = UdpConnectionInformation.getAllUdpInformations();

			context.println("                local                      port  |  pid  ");
			context.println("-----------------------------------------+-------+-------");
			for (ConnectionInformation info : infos) {
				context.println(String.format(" %-39s | %-5d | %-5d", info.getLocal().getHostName(), info.getLocal()
						.getPort(), info.getPid()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void nicStat(String[] args) {
		try {
			List<NicStat> stats = NicStat.getNicStats();

			context.println(" interface | RX |    bytes     |  packets  | err | drop | fifo | comp | frame | multicast ");
			context.println("           | TX |    bytes     |  packets  | err | drop | fifo | comp | colls |  carrier  ");
			for (NicStat stat : stats) {
				context.println("-----------+----+--------------+-----------+-----+------+------+------+-------+-----------");
				context.println(String.format(" %-9s | RX | %12d | %9d | %3d | %4d | %4d | %4d | %5d | %9d",
						stat.getName(), stat.getRxBytes(), stat.getRxPackets(), stat.getRxErrors(), stat.getRxDrops(),
						stat.getRxFifo(), stat.getRxCompressed(), stat.getRxFrames(), stat.getRxMulticast()));
				context.println(String.format("           | TX | %12d | %9d | %3d | %4d | %4d | %4d | %5d | %9d",
						stat.getTxBytes(), stat.getTxPackets(), stat.getTxErrors(), stat.getTxDrops(),
						stat.getTxFifo(), stat.getTxCompressed(), stat.getTxColls(), stat.getTxCarrier()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void arp(String[] args) throws FileNotFoundException {
		List<ArpEntry> entries = ArpCache.getEntries();

		context.println("       ip        |  device  |  HW  |  flags  |    mac address    | mask ");
		context.println("-----------------+----------+------+---------+-------------------+------");
		for (ArpEntry entry : entries) {
			context.println(String.format(" %-15s | %-8s | 0x%-2x | %-7s | %-17s | %s", entry.getIp(),
					entry.getDevice(), entry.getHwType(), entry.getFlags(), entry.getMac(), entry.getMask()));
		}
	}

	public void routingTable(String[] args) throws IOException {
		List<RoutingEntry> entries = RoutingTable.getRoutingEntries();

		context.println("   destination   |     gateway     |     genmask     | flags | metric |  ref  | use | iface | mss | window | irtt ");
		context.println("-----------------+-----------------+-----------------+-------+--------+-------+-----+-------+-----+--------+------");
		for (RoutingEntry entry : entries) {
			context.println(String.format(
					" %-15s | %-15s | %-15s | %-5s | %-6d | %-5d | %3d | %-5s | %-3d | %-6d | %-4d",
					(entry.getDestination() != null) ? entry.getDestination().getHostAddress() : "default",
					(entry.getGateway() != null) ? entry.getGateway().getHostAddress() : "*",
					(entry.getGenmask() != null) ? entry.getGenmask().getHostAddress() : "*", entry.getFlags(),
					entry.getMetric(), entry.getRef(), entry.getUse(), entry.getIface(), entry.getMss(),
					entry.getWindow(), entry.getIrtt()));
		}
	}

	@ScriptUsage(description = "add entry to routing table\n\nSynopsis\n\n\tlinux.addRoutingEntry {net|host} target [netmask Nm] [gw Gw] [metric N] [mss M] [window W] [irtt I] [reject] [mod] [dyn] [reinstate] [dev If]", arguments = {
			@ScriptArgument(name = "destination type", type = "string", description = "net or host"),
			@ScriptArgument(name = "destination", type = "string", description = "destination") })
	public void addRoutingEntry(String[] args) {
		if (!args[0].equalsIgnoreCase("host") && !args[0].equalsIgnoreCase("net")) {
			context.println("destination type is host or net.");
			return;
		}

		try {
			InetAddress destination = InetAddress.getByName(args[1]);
			InetAddress gateway = null;
			InetAddress netmask = null;
			String flagStr = "";
			Integer metric = null;
			String iface = null;
			Integer mss = null;
			Integer window = null;
			Integer irtt = null;

			for (int i = 2; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("netmask"))
					netmask = InetAddress.getByName(args[++i]);
				else if (args[i].equalsIgnoreCase("gw"))
					gateway = InetAddress.getByName(args[++i]);
				else if (args[i].equalsIgnoreCase("metric"))
					metric = Integer.parseInt(args[++i]);
				else if (args[i].equalsIgnoreCase("reject"))
					flagStr += "!";
				else if (args[i].equalsIgnoreCase("mod"))
					flagStr += "M";
				else if (args[i].equalsIgnoreCase("dyn"))
					flagStr += "D";
				else if (args[i].equalsIgnoreCase("reinstate"))
					flagStr += "R";
				else if (args[i].equalsIgnoreCase("dev"))
					iface = args[++i];
				else if (args[i].equalsIgnoreCase("mss"))
					mss = Integer.parseInt(args[++i]);
				else if (args[i].equalsIgnoreCase("window"))
					window = Integer.parseInt(args[++i]);
				else if (args[i].equalsIgnoreCase("irtt"))
					irtt = Integer.parseInt(args[++i]);
				else {
					context.println("invalid argument: " + args[i]);
					return;
				}
			}

			RoutingEntry entry = new RoutingEntry(destination, gateway, netmask, new Flag(flagStr), metric, null, null,
					iface, mss, window, irtt);
			context.println(RoutingTable.addRoutingEntries(entry, args[0].equalsIgnoreCase("host")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@ScriptUsage(description = "delete entry to routing table\n\nSynopsis\n\n\tlinux.deleteRoutingEntry {net|host} target [gw Gw] [netmask Nm] [metric N] [dev If]", arguments = {
			@ScriptArgument(name = "destination type", type = "string", description = "net or host"),
			@ScriptArgument(name = "destination", type = "string", description = "destination") })
	public void deleteRoutingEntry(String[] args) {
		if (!args[0].equalsIgnoreCase("host") && !args[0].equalsIgnoreCase("net")) {
			context.println("destination type is host or net.");
			return;
		}

		try {
			InetAddress destination = InetAddress.getByName(args[1]);
			InetAddress gateway = null;
			InetAddress netmask = null;
			Integer metric = null;
			String iface = null;

			for (int i = 2; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("netmask"))
					netmask = InetAddress.getByName(args[++i]);
				else if (args[i].equalsIgnoreCase("gw"))
					gateway = InetAddress.getByName(args[++i]);
				else if (args[i].equalsIgnoreCase("metric"))
					metric = Integer.parseInt(args[++i]);
				else if (args[i].equalsIgnoreCase("dev"))
					iface = args[++i];
				else {
					context.println("invalid argument: " + args[i]);
					return;
				}
			}

			RoutingEntry entry = new RoutingEntry(destination, gateway, netmask, null, metric, null, null, iface, null,
					null, null);
			context.println(RoutingTable.deleteRoutingEntries(entry, args[0].equalsIgnoreCase("host")));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void dnsConfig(String[] args) throws IOException {
		DnsConfig dns = DnsConfig.getConfig();

		if (dns.getNameserver().size() > 0) {
			context.println("nameserver:");
			for (InetAddress inet : dns.getNameserver())
				context.println("\t" + inet.getHostAddress());
		}

		if (dns.getDomain() != null)
			context.println("domain: " + dns.getDomain());

		if (dns.getSearch().size() > 0) {
			context.println("search: ");
			for (String str : dns.getSearch())
				context.println("\t" + str);
		}

		if (dns.getSortlist().size() > 0) {
			context.println("sortlist: ");
			for (Sortlist list : dns.getSortlist())
				context.println("\t" + list);
		}
	}

	@ScriptUsage(description = "setting nameserver", arguments = {
			@ScriptArgument(name = "first nameserver", type = "string", description = "first nameserver"),
			@ScriptArgument(name = "second nameserver", type = "string", description = "second nameserver", optional = true),
			@ScriptArgument(name = "third nameserver", type = "string", description = "third nameserver", optional = true) })
	public void setNameserver(String[] args) throws IOException {
		DnsConfig dns = DnsConfig.getConfig();
		Object[] nameserver = dns.getNameserver().toArray();

		dns.removeAllNameserver();
		try {
			for (int i = 0; i < args.length; i++) {
				if (i == 3)
					break;
				if (args[i].equals("."))
					dns.addNameserver((InetAddress) nameserver[i]);
				else
					dns.addNameserver(InetAddress.getByName(args[i]));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		dns.save();
	}

	@ScriptUsage(description = "ethtool device information", arguments = { @ScriptArgument(name = "name", type = "string", description = "device name") })
	public void ethtoolInfo(String[] args) {
		for (String name : args) {
			context.println("Settings for " + name + ":");
			try {
				EthernetToolInformation info = EthernetToolInformation.getEthtoolInformation(name);
				context.println("\tSpeed: " + info.getSpeed() + "Mb/s");
				context.println("\tDuplex: " + info.getDuplex());
				context.println("\tAuto-negotiation: " + (info.getAutoNegotiation() ? "on" : "off"));
				context.println("\tLink Detected: " + (info.getLinkDetected() ? "yes" : "no"));
			} catch (IOException e) {
				context.println(e.getMessage());
			}
		}
	}
}
