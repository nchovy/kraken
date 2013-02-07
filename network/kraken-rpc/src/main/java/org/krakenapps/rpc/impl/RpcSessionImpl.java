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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.rpc.RpcAsyncCallback;
import org.krakenapps.rpc.RpcAsyncResult;
import org.krakenapps.rpc.RpcAsyncTable;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMessage;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEventCallback;
import org.krakenapps.rpc.RpcSessionState;
import org.krakenapps.rpc.RpcWaitingCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcSessionImpl implements RpcSession {
	private final Logger logger = LoggerFactory.getLogger(RpcSessionImpl.class.getName());

	private int id;
	private String serviceName;
	private RpcConnection connection;
	private Map<String, Object> props;
	private RpcSessionState state = RpcSessionState.Opened;

	/**
	 * To cancel all blocking calls at close()
	 */
	private Set<Integer> blockingCalls;

	/**
	 * event callbacks
	 */
	private Set<RpcSessionEventCallback> callbacks;

	public RpcSessionImpl(int id, String serviceName, RpcConnection connection) {
		this.id = id;
		this.serviceName = serviceName;
		this.connection = connection;
		this.props = new HashMap<String, Object>();
		this.blockingCalls = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<RpcSessionEventCallback, Boolean>());
	}

	@Override
	public RpcSessionState getState() {
		return state;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public RpcConnection getConnection() {
		return connection;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public Object getProperty(String name) {
		return props.get(name);
	}

	@Override
	public void setProperty(String name, Object value) {
		props.put(name, value);
	}

	@Override
	public RpcAsyncResult call(String method, Object[] params, RpcAsyncCallback callback) {
		verify();

		// NOTE: how about return async future object for blocking?
		RpcConnection conn = getConnection();
		RpcAsyncTable table = conn.getAsyncTable();

		int msgId = conn.nextMessageId();
		RpcMessage msg = RpcMessage.newCall(msgId, getId(), method, params);
		if (logger.isTraceEnabled())
			logger.trace("kraken-rpc: async call [id={}, session={}, method={}]", new Object[] { msgId, getId(), method });

		RpcAsyncResult result = new RpcAsyncResult(callback);
		table.submit(msgId, result);
		conn.send(msg);
		return result;
	}

	@Override
	public Object call(String method, Object... params) throws RpcException, InterruptedException {
		return call(method, params, 0);
	}

	@Override
	public Object call(String method, Object[] params, long timeout) throws RpcException, InterruptedException {
		verify();

		// send rpc call message
		RpcConnection conn = getConnection();
		if (!method.equals("peering-request") && !method.equals("authenticate"))
			conn.waitPeering(timeout);

		RpcBlockingTable table = conn.getBlockingTable();

		int msgId = conn.nextMessageId();
		RpcMessage msg = RpcMessage.newCall(msgId, getId(), method, params);
		RpcWaitingCall call = table.set(msgId);
		conn.send(msg);

		if (logger.isTraceEnabled())
			logger.trace("kraken-rpc: blocking call [id={}, session={}, method={}]", new Object[] { msgId, getId(), method });

		// set blocking call
		blockingCalls.add(msgId);

		// wait response infinitely
		RpcMessage message = null;
		if (timeout == 0)
			message = table.await(call);
		else {
			message = table.await(call, timeout);
		}

		blockingCalls.remove(msgId);
		if (message == null)
			throw new RpcException("rpc timeout: message " + msgId);

		// response received
		Object type = message.getHeader("type");

		if (type.equals("rpc-error")) {
			String cause = message.getString("cause");
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: catching exception for id {}, method {}", msgId, method);

			throw new RpcException(cause);
		}

		if (type.equals("rpc-ret")) {
			if (logger.isDebugEnabled())
				logger.debug("kraken-rpc: response for id {}, method {}", msgId, method);

			return message.get("ret");
		}

		// unknown type of return message
		throw new RpcException("unknown rpc message type: " + type);
	}

	@Override
	public void post(String method, Object... params) {
		verify();

		// send rpc call message
		RpcConnection conn = getConnection();
		conn.waitPeering();

		RpcMessage msg = RpcMessage.newPost(conn.nextMessageId(), getId(), method, params);
		conn.send(msg);
	}

	@Override
	public void close() {
		state = RpcSessionState.Closed;

		// cancel all blocking calls
		RpcConnection conn = getConnection();
		RpcBlockingTable table = conn.getBlockingTable();
		for (Integer msgId : blockingCalls)
			table.cancel(msgId);

		blockingCalls.clear();

		// invoke all session callbacks
		for (RpcSessionEventCallback callback : callbacks) {
			try {
				callback.sessionClosed(this);
			} catch (Exception e) {
				logger.warn("kraken-rpc: session callback should not throw exception", e);
			}
		}
	}

	private void verify() {
		if (state == RpcSessionState.Closed)
			throw new IllegalStateException("session closed: " + id);
	}

	@Override
	public void addListener(RpcSessionEventCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(RpcSessionEventCallback callback) {
		callbacks.remove(callback);
	}

	@Override
	public String toString() {
		return String.format("id=%d, service=%s, peer=%s", id, serviceName, connection.getRemoteAddress());
	}
}
