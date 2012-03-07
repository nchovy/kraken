package org.krakenapps.rpc;

public interface RpcService {
	void sessionRequested(RpcSessionEvent e);

	void sessionOpened(RpcSessionEvent e);

	void sessionClosed(RpcSessionEvent e);

	void exceptionCaught(RpcExceptionEvent e);

	void connectionClosed(RpcConnection e);
}
