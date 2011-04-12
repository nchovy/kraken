package org.krakenapps.rpc;

public interface RpcConnectionEventListener {
	void connectionOpened(RpcConnection connection);

	void connectionClosed(RpcConnection connection);
}
