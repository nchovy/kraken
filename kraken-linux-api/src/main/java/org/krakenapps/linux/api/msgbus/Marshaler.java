package org.krakenapps.linux.api.msgbus;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.linux.api.DnsConfig;
import org.krakenapps.linux.api.EthernetInterface;
import org.krakenapps.linux.api.EthernetToolInformation;
import org.krakenapps.linux.api.RoutingEntry;
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
		m.put("address", sortlist.getAddress().getHostAddress());
		m.put("netmask", sortlist.getNetmask().getHostAddress());
		return m;
	}

	public static Map<String, Object> marshal(RoutingEntry entry) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("destination", entry.getDestination().getHostAddress());
		m.put("gateway", entry.getGateway().getHostAddress());
		m.put("genmask", entry.getGenmask().getHostAddress());
		m.put("flag", entry.getFlags());
		m.put("metric", entry.getMetric());
		m.put("ref", entry.getRef());
		m.put("use", entry.getUse());
		m.put("iface", entry.getIface());
		return m;
	}

	public static Map<String, Object> marshal(EthernetInterface ei) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("boot_proto", ei.getBootProto());
		m.put("broadcast", ei.getBroadcast().getHostAddress());
		m.put("device", ei.getDevice());
		m.put("dhcp_hostname", ei.getDhcpHostname().getHostAddress());
		m.put("dns1", ei.getDns1().getHostAddress());
		m.put("dns2", ei.getDns2().getHostAddress());
		m.put("ethtool_opts", ei.getEthtoolOpts());
		m.put("gateway", ei.getGateway().getHostAddress());
		m.put("hw_addr", ei.getHwAddr());
		m.put("ip_addr", ei.getIpAddr().getHostAddress());
		m.put("mac_addr", ei.getMacAddr());
		m.put("master", ei.getMaster());
		m.put("netmask", ei.getNetmask().getHostAddress());
		m.put("network", ei.getNetwork().getHostAddress());
		m.put("on_boot", ei.getOnBoot());
		m.put("peer_dns", ei.getPeerDns());
		m.put("slave", ei.getSlave());
		m.put("src_addr", ei.getSrcAdrr().getHostAddress());
		m.put("user_ctl", ei.getUserCtl());
		m.put("address_bindings", marshal(ei.getAddressBindings()));
		return m;
	}

	public static Map<String, Object> marshal(AddressBinding ab) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("device", ab.getDevice());
		m.put("ip_addr", ab.getIpAddr().getHostAddress());
		m.put("broadcast", ab.getBroadcast().getHostAddress());
		m.put("netmask", ab.getNetmask().getHostAddress());
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

	public static List<Object> marshal(Collection<?> list) {
		if (list == null)
			return null;

		List<Object> serializedObjects = new ArrayList<Object>();

		for (Object obj : list) {
			if (obj instanceof DnsConfig)
				serializedObjects.add(marshal((DnsConfig) obj));
			else if (obj instanceof RoutingEntry)
				serializedObjects.add(marshal((RoutingEntry) obj));
			else if (obj instanceof AddressBinding)
				serializedObjects.add(marshal((AddressBinding) obj));
		}

		return serializedObjects;
	}
}
