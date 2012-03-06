package org.krakenapps.rpc;

import java.util.Collection;

public interface RpcAgent {
	String getGuid();

	Collection<RpcBindingProperties> getBindings();

	void open(RpcBindingProperties props);

	void close(RpcBindingProperties props);

	RpcConnection connect(RpcConnectionProperties props);

	RpcConnection connectSsl(RpcConnectionProperties props);

	Collection<RpcConnection> getConnections();

	RpcConnection findConnection(int id);

	RpcPeerRegistry getPeerRegistry();

	void addConnectionListener(RpcConnectionEventListener listener);

	void removeConnectionListener(RpcConnectionEventListener listener);
}
