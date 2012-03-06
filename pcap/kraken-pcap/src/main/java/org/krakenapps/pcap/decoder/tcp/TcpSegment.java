package org.krakenapps.pcap.decoder.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.krakenapps.pcap.decoder.ip.IpPacket;
import org.krakenapps.pcap.util.Buffer;

public interface TcpSegment {
	IpPacket getIpPacket();
	
	boolean isSyn();

	boolean isAck();

	boolean isPsh();
	
	boolean isFin();

	boolean isRst();
	
	boolean isUrg();

	TcpDirection getDirection();

	TcpSessionKey getSessionKey();
	
	InetSocketAddress getSource();
	
	InetSocketAddress getDestination();

	InetAddress getSourceAddress();

	InetAddress getDestinationAddress();

	int getTotalLength();

	int getSourcePort();

	int getDestinationPort();

	int getSeq();

	int getAck();

	int getRelativeSeq();

	int getRelativeAck();

	Buffer getData();
}
