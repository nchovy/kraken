package org.krakenapps.dns;

import java.net.DatagramPacket;

public interface DnsEventListener {
	void onReceive(DnsMessage query);

	void onSend(DnsMessage query, DnsMessage response);

	void onError(DatagramPacket packet, Throwable t);

	void onDrop(DnsMessage query, Throwable t);
}
