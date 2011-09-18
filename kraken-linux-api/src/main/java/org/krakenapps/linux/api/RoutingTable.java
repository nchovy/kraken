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
package org.krakenapps.linux.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.krakenapps.linux.api.RoutingEntry.Flag;

public class RoutingTable {
	public static List<RoutingEntry> getRoutingEntries() throws IOException {
		List<RoutingEntry> entries = new ArrayList<RoutingEntry>();
		java.lang.Process p = null;
		BufferedReader br = null;

		try {
			String line = null;

			p = Runtime.getRuntime().exec("route -een");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			br.readLine(); // ignore header line
			br.readLine(); // ignore column name line

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("[\t| ]+");
				if (tokens.length == 8) {
					InetAddress destination = (!tokens[0].equals("default")) ? InetAddress.getByName(tokens[0]) : null;
					InetAddress gateway = (!tokens[1].equals("*")) ? InetAddress.getByName(tokens[1]) : null;
					InetAddress genmask = InetAddress.getByName(tokens[2]);
					Flag flags = new RoutingEntry.Flag(tokens[3]);
					int metric = Integer.parseInt(tokens[4]);
					int ref = Integer.parseInt(tokens[5]);
					int use = Integer.parseInt(tokens[6]);
					String iface = tokens[7];
					entries.add(new RoutingEntry(destination, gateway, genmask, flags, metric, ref, use, iface, null, null, null));
				} else if (tokens.length == 11) {
					InetAddress destination = (!tokens[0].equals("default")) ? InetAddress.getByName(tokens[0]) : null;
					InetAddress gateway = (!tokens[1].equals("*")) ? InetAddress.getByName(tokens[1]) : null;
					InetAddress genmask = InetAddress.getByName(tokens[2]);
					Flag flags = new RoutingEntry.Flag(tokens[3]);
					int metric = Integer.parseInt(tokens[4]);
					int ref = Integer.parseInt(tokens[5]);
					int use = Integer.parseInt(tokens[6]);
					String iface = tokens[7];
					int mss = Integer.parseInt(tokens[8]);
					int window = Integer.parseInt(tokens[9]);
					int irtt = Integer.parseInt(tokens[10]);
					entries.add(new RoutingEntry(destination, gateway, genmask, flags, metric, ref, use, iface, mss, window, irtt));
				}
			}
		} finally {
			if (p != null)
				p.destroy();
			if (br != null)
				br.close();
		}

		return entries;
	}

	public static List<RoutingEntryV6> getIpv6RoutingEntries() throws IOException {
		List<RoutingEntryV6> entries = new ArrayList<RoutingEntryV6>();
		java.lang.Process p = null;
		BufferedReader br = null;

		try {
			String line = null;

			p = Runtime.getRuntime().exec("route -A inet6");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			br.readLine();
			br.readLine();

			while ((line = br.readLine()) != null) {
				String[] tokens = line.split("[\t| ]+");
				if (tokens.length != 7)
					continue;

				String destination = tokens[0];
				int mask = 0;
				int pos = 0;
				if ((pos = tokens[0].indexOf("/")) != -1) {
					destination = destination.substring(0, pos);
					mask = Integer.parseInt(tokens[0].substring(pos + 1));
				}

				String nextHop = (!tokens[1].equals("*")) ? tokens[1] : null;
				Flag flags = new RoutingEntry.Flag(tokens[2]);
				int metric = Integer.parseInt(tokens[3]);
				int ref = Integer.parseInt(tokens[4]);
				int use = Integer.parseInt(tokens[5]);
				String iface = tokens[6];
				entries.add(new RoutingEntryV6(destination, nextHop, mask, flags, metric, ref, use, iface));
			}
		} finally {
			if (p != null)
				p.destroy();
			if (br != null)
				br.close();
		}

		return entries;
	}

	public static String addRoutingEntries(RoutingEntry entry, boolean isHost) {
		String cmd = "route add";

		cmd += (isHost ? " -host " : " -net ") + entry.getDestination().getHostAddress();
		if (entry.getGenmask() != null)
			cmd += " netmask " + entry.getGenmask().getHostAddress();
		if (entry.getGateway() != null)
			cmd += " gw " + entry.getGateway().getHostAddress();
		if (entry.getMetric() != null)
			cmd += " metric " + entry.getMetric();
		if (entry.getFlags() != null) {
			if (entry.getFlags().isReject())
				cmd += " reject";
			if (entry.getFlags().isModified())
				cmd += " mod";
			if (entry.getFlags().isDynamically())
				cmd += " dyn";
			if (entry.getFlags().isReinstate())
				cmd += " reinstate";
		}
		if (entry.getIface() != null)
			cmd += " dev " + entry.getIface();
		if (entry.getMss() != null)
			cmd += " mss " + entry.getMss();
		if (entry.getWindow() != null)
			cmd += " window " + entry.getWindow();
		if (entry.getIrtt() != null)
			cmd += " irtt " + entry.getIrtt();

		return Util.run(cmd);
	}

	public static String deleteRoutingEntries(RoutingEntry entry, boolean isHost) {
		String cmd = "route del";

		cmd += (isHost ? " -host " : " -net ") + entry.getDestination().getHostAddress();
		if (entry.getGenmask() != null)
			cmd += " netmask " + entry.getGenmask().getHostAddress();
		if (entry.getGateway() != null)
			cmd += " gw " + entry.getGateway().getHostAddress();
		if (entry.getMetric() != null)
			cmd += " metric " + entry.getMetric();
		if (entry.getIface() != null)
			cmd += " dev " + entry.getIface();

		return Util.run(cmd);
	}

	public static RoutingEntry findRoute(InetAddress ip) throws IOException {
		int target = toInt((Inet4Address) ip);

		for (RoutingEntry entry : RoutingTable.getRoutingEntries()) {
			int dst = toInt((Inet4Address) entry.getDestination());
			int mask = toInt((Inet4Address) entry.getGenmask());

			if (dst == (target & mask))
				return entry;
		}

		return null;
	}

	private static int toInt(Inet4Address addr) {
		byte[] b = addr.getAddress();
		int l = 0;

		for (int i = 0; i < 4; i++) {
			l <<= 8;
			l |= b[i] & 0xff;
		}

		return l;
	}
}