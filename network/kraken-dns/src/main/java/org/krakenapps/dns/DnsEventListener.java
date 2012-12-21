package org.krakenapps.dns;

import java.net.DatagramPacket;

public interface DnsEventListener {
	void onReceive(DatagramPacket queryPacket, DnsMessage query);

	void onSend(DatagramPacket queryPacket, DnsMessage query, DatagramPacket responsePacket, DnsMessage response);

	void onError(DatagramPacket packet, Throwable t);

	void onDrop(DatagramPacket queryPacket, DnsMessage query, Throwable t);
}
