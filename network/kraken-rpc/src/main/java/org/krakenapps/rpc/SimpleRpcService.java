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
