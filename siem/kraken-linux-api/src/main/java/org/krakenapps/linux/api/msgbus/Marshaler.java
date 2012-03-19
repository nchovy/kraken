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
package org.krakenapps.linux.api.msgbus;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.linux.api.ArpEntry;
import org.krakenapps.linux.api.DnsConfig;
import org.krakenapps.linux.api.EthernetInterface;
import org.krakenapps.linux.api.EthernetToolInformation;
import org.krakenapps.linux.api.Ipv6NeighborEntry;
import org.krakenapps.linux.api.RoutingEntry;
import org.krakenapps.linux.api.RoutingEntryV6;
import org.krakenapps.linux.api.DnsConfig.Sortlist;
import org.krakenapps.linux.api.EthernetInterface.AddressBinding;

public class Marshaler {
	public static Map<String, Object> marshal(DnsConfig dns) {
		Map<String, Object> m = new HashMap<String, Object>();

		List<String> nameserver = new ArrayList<String>();
		for (InetAddress addr : dns.getNameserver())
			nameserver.add(addr.getHostAddress());
		m.put("nameserver", nameserver);
		m.put("domain", dns.getDomain());
		List<Object> sortlist = new ArrayList<Object>();
		for (Sortlist list : dns.getSortlist())
			sortlist.add(marshal(list));
		m.put("sortlist", sortlist);
		m.put("search", dns.getSearch());

		return m;
	}

	private static Map<String, Object> marshal(Sortlist sortlist) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("address", sortlist.getAddress() != null ? sortlist.getAddress().getHostAddress() : null);
		m.put("netmask", sortlist.getNetmask() != null ? sortlist.getNetmask().getHostAddress() : null);
		return m;
	}
	
	public static Map<String, Object> marshal(RoutingEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("destination", entry.getDestination() != null ? entry.getDestination().getHostAddress() : null);
		m.put("gateway", entry.getGateway() != null ? entry.getGateway().getHostAddress() : null);
		m.put("genmask", entry.getGenmask() != null ? entry.getGenmask().getHostAddress() : null);
		m.put("flag", entry.getFlags());
		m.put("metric", entry.getMetric());
		m.put("ref", entry.getRef());
		m.put("use", entry.getUse());
		m.put("interface", entry.getIface());
		return m;
	}
	
	public static Map<String, Object> marshal(RoutingEntryV6 entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("destination", entry.getDestination());
		m.put("gateway", entry.getNextHop());
		m.put("mask", entry.getMask());
		m.put("flag", entry.getFlags());
		m.put("metric", entry.getMetric());
		m.put("ref", entry.getRef());
		m.put("use", entry.getUse());
		m.put("interface", entry.getIface());
		return m;
	}

	public static Map<String, Object> marshal(EthernetInterface ei) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("boot_proto", ei.getBootProto());
		m.put("broadcast", ei.getBroadcast() != null ? ei.getBroadcast().getHostAddress() : null);
		m.put("device", ei.getDevice());
		m.put("dhcp_hostname", ei.getDhcpHostname() != null ? ei.getDhcpHostname().getHostAddress() : null);
		m.put("dns1", ei.getDns1() != null ? ei.getDns1().getHostAddress() : null);
		m.put("dns2", ei.getDns2() != null ? ei.getDns2().getHostAddress() : null);
		m.put("ethtool_opts", ei.getEthtoolOpts());
		m.put("gateway", ei.getGateway() != null ? ei.getGateway().getHostAddress() : null);
		m.put("hw_addr", ei.getHwAddr());
		m.put("ip_addr", ei.getIpAddr() != null ? ei.getIpAddr().getHostAddress() : null);
		m.put("mac_addr", ei.getMacAddr());
		m.put("master", ei.getMaster());
		m.put("netmask", ei.getNetmask() != null ? ei.getNetmask().getHostAddress() : null);
		m.put("network", ei.getNetwork() != null ? ei.getNetwork().getHostAddress() : null);
		m.put("on_boot", ei.getOnBoot());
		m.put("peer_dns", ei.getPeerDns());
		m.put("slave", ei.getSlave());
		m.put("src_addr", ei.getSrcAdrr() != null ? ei.getSrcAdrr().getHostAddress() : null);
		m.put("user_ctl", ei.getUserCtl());
		m.put("address_bindings", marshal(ei.getAddressBindings()));
		return m;
	}

	public static Map<String, Object> marshal(AddressBinding ab) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("device", ab.getDevice());
		m.put("ip_addr", ab.getIpAddr() != null ? ab.getIpAddr().getHostAddress() : null);
		m.put("broadcast", ab.getBroadcast() != null ? ab.getBroadcast().getHostAddress() : null);
		m.put("netmask", ab.getNetmask() != null ? ab.getNetmask().getHostAddress() : null);
		return m;
	}

	public static Map<String, Object> marshal(EthernetToolInformation info) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("speed", info.getSpeed());
		m.put("duplex", info.getDuplex());
		m.put("auto_negotiation", info.getAutoNegotiation());
		m.put("link_detected", info.getLinkDetected());
		return m;
	}

	public static Map<String, Object> marshal(ArpEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ip", entry.getIp());
		m.put("hw_type", entry.getHardware());
		m.put("flags", entry.getFlags());
		m.put("mac", entry.getMac());
		m.put("mask", entry.getMask());
		m.put("device", entry.getDevice());
		return m;
	}
	
	public static Map<String, Object> marshal(Ipv6NeighborEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ip", entry.getAddress());
		m.put("mac", entry.getMac());
		m.put("device", entry.getDevice());
		m.put("state", entry.getState());
		return m;
	}
	
	public static List<Object> marshal(Collection<?> list) {
		if (list == null)
			return null;

		List<Object> serializedObjects = new ArrayList<Object>();

		for (Object obj : list) {
			if (obj instanceof DnsConfig)
				serializedObjects.add(marshal((DnsConfig) obj));
			else if (obj instanceof RoutingEntry)
				serializedObjects.add(marshal((RoutingEntry) obj));
			else if (obj instanceof RoutingEntryV6)
				serializedObjects.add(marshal((RoutingEntryV6) obj));
			else if (obj instanceof AddressBinding)
				serializedObjects.add(marshal((AddressBinding) obj));
			else if (obj instanceof ArpEntry)
				serializedObjects.add(marshal((ArpEntry) obj));
			else if (obj instanceof Ipv6NeighborEntry)
				serializedObjects.add(marshal((Ipv6NeighborEntry) obj));
		}

		return serializedObjects;
	}
}
