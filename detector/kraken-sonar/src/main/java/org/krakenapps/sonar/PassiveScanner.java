package org.krakenapps.sonar;

import java.util.Collection;

import org.krakenapps.pcap.Protocol;
import org.krakenapps.pcap.decoder.arp.ArpProcessor;
import org.krakenapps.pcap.util.PcapLiveRunner;

public interface PassiveScanner {
	void start();

	void stop();

	Collection<String> getDeviceNames();

	PcapLiveRunner getDevice(String name);

	void addArpSniffer(ArpProcessor callback);

	void removeArpSniffer(ArpProcessor callback);

	void addTcpSniffer(Protocol protocol, Object callback);

	void removeTcpSniffer(Protocol protocol, Object callback);

	void addUdpSniffer(Protocol protocol, Object callback);

	void removeUdpSniffer(Protocol protocol, Object callback);
}
