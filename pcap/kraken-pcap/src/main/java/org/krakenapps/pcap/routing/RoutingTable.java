package org.krakenapps.pcap.routing;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

import org.krakenapps.pcap.util.IpConverter;

public class RoutingTable {
	static {
		System.loadLibrary("kpcap");
	}
	
	public static List<RoutingEntry> getRoutingEntries() {
		return getNativeRoutingEntries();
	}

	private static native List<RoutingEntry> getNativeRoutingEntries();

	public static RoutingEntry findRoute(InetAddress ip) {
		int target = IpConverter.toInt((Inet4Address) ip);

		for (RoutingEntry entry : RoutingTable.getRoutingEntries()) {
			int dst = IpConverter.toInt((Inet4Address) entry.getDestination());
			int mask = IpConverter.toInt((Inet4Address) entry.getMask());

			if (dst == (target & mask)) {
				return entry;
			}
		}

		return null;
	}
}
