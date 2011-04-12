package org.krakenapps.pcap.decoder.ip;

import java.net.InetAddress;

import org.krakenapps.pcap.util.Buffer;

public interface IpPacket {
	Object getL2Frame();
	
	int getVersion();
	
	InetAddress getSourceAddress();
	
	InetAddress getDestinationAddress();
	
	Buffer getData();
}
