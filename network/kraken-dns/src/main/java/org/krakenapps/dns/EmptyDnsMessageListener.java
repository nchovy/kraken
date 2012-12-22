package org.krakenapps.dns;

import java.net.DatagramPacket;

public class EmptyDnsMessageListener implements DnsEventListener {

	@Override
	public void onReceive(DnsMessage query) {
	}

	@Override
	public void onSend(DnsMessage query, DnsMessage response) {
	}

	@Override
	public void onError(DatagramPacket packet, Throwable t) {
	}

	@Override
	public void onDrop(DnsMessage query, Throwable t) {
	}

}
