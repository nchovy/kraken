package org.krakenapps.dns;

import java.net.DatagramPacket;

public class EmptyDnsMessageListener implements DnsEventListener {

	@Override
	public void onReceive(DatagramPacket packet, DnsMessage query) {
	}

	@Override
	public void onSend(DatagramPacket queryPacket, DnsMessage query, DatagramPacket responsePacket, DnsMessage response) {
	}

	@Override
	public void onError(DatagramPacket packet, Throwable t) {
	}

	@Override
	public void onDrop(DatagramPacket packet, DnsMessage query, Throwable t) {
	}

}
