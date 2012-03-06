package org.krakenapps.proxy;

import java.net.InetSocketAddress;

public class ForwardRoute {
	private InetSocketAddress remote;
	private InetSocketAddress local;

	public ForwardRoute(InetSocketAddress local, InetSocketAddress remote) {
		this.remote = remote;
		this.local = local;
	}
	
	public InetSocketAddress getRemote() {
		return remote;
	}

	public InetSocketAddress getLocal() {
		return local;
	}
}
