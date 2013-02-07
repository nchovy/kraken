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
package org.krakenapps.rpc.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.rpc.RpcAsyncResult;
import org.krakenapps.rpc.RpcAsyncTable;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMessage;
import org.krakenapps.rpc.RpcWaitingCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcAsyncTableImpl implements RpcAsyncTable {
	private final Logger logger = LoggerFactory.getLogger(RpcAsyncTableImpl.class.getName());
	private Map<Integer, RpcWaitingCall> callMap;

	public RpcAsyncTableImpl() {
		callMap = new ConcurrentHashMap<Integer, RpcWaitingCall>();
	}

	@Override
	public Collection<RpcWaitingCall> getWaitingCalls() {
		return Collections.unmodifiableCollection(callMap.values());
	}

	@Override
	public boolean contains(int id) {
		return callMap.containsKey(id);
	}

	@Override
	public void cancel(int id) {
		callMap.remove(id);
	}

	@Override
	public void signal(int id, RpcMessage response) {
		if (logger.isDebugEnabled())
			logger.debug("kraken-rpc: signal call response {}", id);

		RpcWaitingCallImpl item = (RpcWaitingCallImpl) callMap.get(id);
		if (item == null) {
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: no waiting item {}, maybe canceled", id);
			return;
		}

		// invoke callback
		Object type = response.getHeader("type");
		String method = response.getString("method");

		RpcAsyncResult asyncResult = item.result;

		if (type.equals("rpc-error")) {
			String cause = response.getString("cause");
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: catching exception for id {}, method {}", id, method);

			asyncResult.setException(new RpcException(cause));
		}

		if (type.equals("rpc-ret")) {
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: response for id {}, method {}", id, method);

			asyncResult.setReturn(response.get("ret"));
		}

		item.result.getCallback().onComplete(asyncResult);
	}

	@Override
	public void submit(int id, RpcAsyncResult result) {
		if (result == null)
			throw new IllegalArgumentException("callback should not null");

		callMap.put(id, new RpcWaitingCallImpl(id, result));
	}

	private static class RpcWaitingCallImpl implements RpcWaitingCall {
		private int id;
		private Date since;
		private RpcAsyncResult result = null;

		public RpcWaitingCallImpl(int id, RpcAsyncResult result) {
			this.id = id;
			this.result = result;
			this.since = new Date();
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public Date getSince() {
			return since;
		}

		@Override
		public void done(RpcMessage result) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void await(int timeout) throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public RpcMessage getResult() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return String.format("id=%s, since=%s", id, since.toString());
		}
	}

}
