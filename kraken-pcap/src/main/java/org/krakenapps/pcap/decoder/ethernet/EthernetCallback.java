package org.krakenapps.pcap.decoder.ethernet;

public interface EthernetCallback {
	void onReceived(EthernetFrame frame);
}
