package org.krakenapps.rpc;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface RpcAgent {
	String getGuid();

	RpcConnection connect(InetSocketAddress address);

	RpcConnection connect(String host, int port);

	RpcConnection connectSsl(InetSocketAddress address);

	RpcConnection connectSsl(InetSocketAddress address, String keyAlias, String trustAlias);

	RpcConnection connectSsl(String host, int port, String keyAlias, String trustAlias);

	Collection<RpcConnection> getConnections();

	RpcConnection findConnection(int id);

	RpcPeerRegistry getPeerRegistry();
}
