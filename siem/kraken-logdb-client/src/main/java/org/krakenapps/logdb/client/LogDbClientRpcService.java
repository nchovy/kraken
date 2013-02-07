/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

	private LogDbClientRpcCallback callback;

	public LogDbClientRpcService(LogDbClientRpcCallback callback) {
		this.callback = callback;
	}

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

		callback.onPageLoaded(id, offset, limit);
	}

	@RpcMethod(name = "onEof")
	public void onEof(Map<String, Object> m) {
		int id = (Integer) m.get("id");
		int offset = (Integer) m.get("offset");
		int limit = (Integer) m.get("limit");

		callback.onEof(id, offset, limit);
	}
}
