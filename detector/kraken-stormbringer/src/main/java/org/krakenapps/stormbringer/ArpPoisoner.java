package org.krakenapps.stormbringer;

import java.net.InetAddress;
import java.util.Map;

import org.krakenapps.pcap.decoder.ethernet.MacAddress;
import org.krakenapps.pcap.live.PcapDevice;

public interface ArpPoisoner extends Runnable {
	void addAdapter(PcapDevice device);

	void removeAdapter(PcapDevice device);

	void addTarget(InetAddress peer1, InetAddress peer2);

	void removeTarget(InetAddress peer1, InetAddress peer2);

	Map<InetAddress, MacAddress> getArpCache();
	
	// NOTE: will be removed
	void setGateway(InetAddress ip);

	void attack();

	void stop();
}
