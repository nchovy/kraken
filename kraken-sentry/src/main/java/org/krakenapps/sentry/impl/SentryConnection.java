/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.sentry.impl;

import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentryConnection implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(SentryConnection.class.getName());

	private RpcAgent agent;
	private RpcConnection connection;
	private RpcService sentryRpcService;

	public SentryConnection(RpcAgent agent, RpcConnection connection, RpcService sentryRpcService) {
		this.agent = agent;
		this.connection = connection;
		this.sentryRpcService = sentryRpcService;
	}

	@Override
	public void run() {
		try {
			// bind kraken-sentry rpc service
			connection.bind("kraken-sentry", sentryRpcService);
			connection.requestPeering(null);

			RpcSession session = connection.createSession("kraken-base");
			session.call("hello", new Object[] { agent.getGuid() });
		} catch (RpcException e) {
			logger.error("krane sentry: connect failed", e);
		} catch (InterruptedException e) {
			logger.error("krane sentry: connect failed", e);
		}
	}

}
