package org.krakenapps.arpwatch.impl;

import java.net.InetAddress;
import java.util.Date;

import org.krakenapps.arpwatch.ArpSpoofEvent;
import org.krakenapps.pcap.decoder.ethernet.MacAddress;

public class ArpSpoofEventImpl implements ArpSpoofEvent {
	private final Date date;
	private final MacAddress attacker;
	private final InetAddress ip;

	public ArpSpoofEventImpl(MacAddress attacker, InetAddress ip) {
		this.date = new Date();
		this.attacker = attacker;
		this.ip = ip;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public InetAddress getSpoofedIp() {
		return ip;
	}

	@Override
	public MacAddress getAttackerMac() {
		return attacker;
	}

}
