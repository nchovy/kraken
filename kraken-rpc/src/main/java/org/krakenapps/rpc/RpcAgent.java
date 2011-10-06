package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcAgent {
	String getGuid();

	RpcConnection connect(RpcConnectionProperties props);

	RpcConnection connectSsl(RpcConnectionProperties props);

	Collection<RpcConnection> getConnections();

	RpcConnection findConnection(int id);

	RpcPeerRegistry getPeerRegistry();

	void addConnectionListener(RpcConnectionEventListener listener);

	void removeConnectionListener(RpcConnectionEventListener listener);
}
