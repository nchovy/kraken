package org.krakenapps.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRpcService implements RpcService {
	private final Logger logger = LoggerFactory.getLogger(SimpleRpcService.class.getName());

	@Override
	public void connectionClosed(RpcConnection e) {
	}

	@Override
	public void exceptionCaught(RpcExceptionEvent e) {
		logger.error("kraken-rpc: rpc service throws exception", e.getCause());
	}

	@Override
	public void sessionClosed(RpcSessionEvent e) {
	}

	@Override
	public void sessionOpened(RpcSessionEvent e) {
	}

	@Override
	public void sessionRequested(RpcSessionEvent e) {
	}

}
