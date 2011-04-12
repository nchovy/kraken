package org.krakenapps.sleepproxy;

import java.net.InetAddress;
import java.util.Map;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public interface TrafficMonitor {
	Map<InetAddress, MacAddress> getArpCache();
	
	/**
	 * start arp poisoning and monitor tcp syn packet.
	 */
	void register(InetAddress ip);

	/**
	 * stop arp poisoning
	 */
	void unregister(InetAddress ip);
	
}
