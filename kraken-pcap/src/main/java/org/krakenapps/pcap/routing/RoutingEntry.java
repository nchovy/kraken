package org.krakenapps.pcap.routing;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RoutingEntry {
	private String iface;
	private InetAddress destination;
	private InetAddress gateway;
	private InetAddress mask;
	private int metric;

	public RoutingEntry(String iface, byte[] destination, byte[] gateway, byte[] mask, int metric) {
		try {
			this.iface = iface;
			this.destination = InetAddress.getByAddress(destination);
			this.gateway = InetAddress.getByAddress(gateway);
			this.mask = InetAddress.getByAddress(mask);
			this.metric = metric;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException();
		}
	}

	public String getInterfaceName() {
		return iface;
	}

	public InetAddress getDestination() {
		return destination;
	}

	public InetAddress getGateway() {
		return gateway;
	}

	public InetAddress getMask() {
		return mask;
	}

	public int getMetric() {
		return metric;
	}

	@Override
	public String toString() {
		return String.format("name=%s, destination=%s, mask=%s, gateway=%s, metric=%d", iface,
				destination.getHostAddress(), mask.getHostAddress(), gateway.getHostAddress(), metric);
	}

}
