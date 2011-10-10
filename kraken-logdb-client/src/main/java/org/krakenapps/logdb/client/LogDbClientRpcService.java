package org.krakenapps.logdb.client;

import java.util.Map;

import org.krakenapps.rpc.RpcExceptionEvent;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSessionEvent;
import org.krakenapps.rpc.SimpleRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDbClientRpcService extends SimpleRpcService {
	private final Logger logger = LoggerFactory.getLogger(LogDbClientRpcService.class.getName());

	@Override
	public void exceptionCaught(RpcExceptionEvent e) {
		logger.error("kraken logdb client: rpc error", e);
	}

	@Override
	public void sessionOpened(RpcSessionEvent e) {
		logger.info("kraken logdb client: session opened from {}", e.getSession().getConnection());
	}

	@Override
	public void sessionClosed(RpcSessionEvent e) {
		logger.info("kraken logdb client: session closed from {}", e.getSession().getConnection());
	}

	@RpcMethod(name = "onPageLoaded")
	public void onPageLoaded(Map<String, Object> m) {
		int id = (Integer) m.get("id");
		int offset = (Integer) m.get("offset");
		int limit = (Integer) m.get("limit");

		logger.info("kraken logdb client: on page loaded, id: {}, offset: {}, limit: {}", new Object[] { id, offset,
				limit });
	}

	@RpcMethod(name = "onEof")
	public void onEof(Map<String, Object> m) {
		int id = (Integer) m.get("id");
		int offset = (Integer) m.get("offset");
		int limit = (Integer) m.get("limit");

		logger.info("kraken logdb client: on eof id: {}, offset: {}, limit: {}", new Object[] { id, offset, limit });
	}
}
