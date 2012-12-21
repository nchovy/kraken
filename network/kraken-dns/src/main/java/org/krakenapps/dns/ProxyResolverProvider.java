package org.krakenapps.dns;

import java.net.InetAddress;

public interface ProxyResolverProvider extends DnsResolverProvider {
	InetAddress getNameServer();

	void setNameServer(InetAddress addr);
}
