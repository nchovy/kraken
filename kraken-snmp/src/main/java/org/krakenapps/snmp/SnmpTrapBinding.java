package org.krakenapps.snmp;

import java.net.InetSocketAddress;

public class SnmpTrapBinding {
	private InetSocketAddress bindAddress;

	public SnmpTrapBinding(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}

	public InetSocketAddress getBindAddress() {
		return bindAddress;
	}
}
