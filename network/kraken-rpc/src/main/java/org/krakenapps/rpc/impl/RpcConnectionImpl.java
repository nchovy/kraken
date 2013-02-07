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

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;
import org.krakenapps.rpc.RpcAsyncCallback;
import org.krakenapps.rpc.RpcAsyncResult;
import org.krakenapps.rpc.RpcAsyncTable;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionEventListener;
import org.krakenapps.rpc.RpcConnectionState;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcExceptionEvent;
import org.krakenapps.rpc.RpcMessage;
import org.krakenapps.rpc.RpcPeeringCallback;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcServiceBinding;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEvent;
import org.krakenapps.rpc.RpcSessionEventCallback;
import org.krakenapps.rpc.RpcSessionState;
import org.krakenapps.rpc.RpcTrustLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcConnectionImpl implements RpcConnection, RpcSessionEventCallback {
	private final Logger logger = LoggerFactory.getLogger(RpcConnectionImpl.class.getName());
	private RpcConnectionState state;
	private Channel channel;
	private RpcTrustLevel trustedLevel;
	private Map<String, RpcServiceBinding> bindingMap;
	private Map<Integer, RpcSession> sessionMap;
	private Map<String, Object> props;
	private AtomicInteger idCounter;
	private AtomicInteger sessionCounter;
	private RpcBlockingTableImpl blockingTable;
	private RpcAsyncTableImpl asyncTable;
	private String guid;
	private Set<RpcConnectionEventListener> callbacks;

	private final Lock controlLock = new ReentrantLock();
	private Condition controlReady = controlLock.newCondition();
	private volatile boolean isControlReady = false;

	private final Lock peeringLock = new ReentrantLock();
	private Condition peeringReady = peeringLock.newCondition();

	// peer information
	private String peerGuid;
	private String nonce;
	private RpcTrustLevel trustLevel;

	private X509Certificate peerCert;

	public RpcConnectionImpl(Channel channel, String guid) {
		this.state = RpcConnectionState.Opened;
		this.channel = channel;

		this.bindingMap = new ConcurrentHashMap<String, RpcServiceBinding>();
		this.sessionMap = new ConcurrentHashMap<Integer, RpcSession>();
		this.props = new ConcurrentHashMap<String, Object>();
		this.idCounter = new AtomicInteger();
		this.asyncTable = new RpcAsyncTableImpl();
		this.blockingTable = new RpcBlockingTableImpl();

		this.guid = guid;
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<RpcConnectionEventListener, Boolean>());

		if (isClient())
			sessionCounter = new AtomicInteger(1); // session id will be odd
		// number
		else
			sessionCounter = new AtomicInteger(0); // session id will be even
		// number

	}

	@Override
	public int getId() {
		return channel.getId();
	}

	@Override
	public boolean isOpen() {
		return state == RpcConnectionState.Opened;
	}

	@Override
	public boolean isClient() {
		return channel.getParent() == null;
	}

	@Override
	public RpcConnectionState getState() {
		return state;
	}

	@Override
	public RpcTrustLevel getTrustedLevel() {
		return trustedLevel;
	}

	@Override
	public void setTrustedLevel(RpcTrustLevel trustedLevel) {
		this.trustedLevel = trustedLevel;

		// wake all
		try {
			peeringLock.lock();
			peeringReady.signalAll();
		} finally {
			peeringLock.unlock();
		}
	}

	@Override
	public String getPeerGuid() {
		return peerGuid;
	}

	@Override
	public void setPeerGuid(String peerGuid) {
		this.peerGuid = peerGuid;
	}

	@Override
	public X509Certificate getPeerCertificate() {
		return peerCert;
	}

	@Override
	public String getNonce() {
		return nonce;
	}

	@Override
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	@Override
	public RpcTrustLevel getTrustLevel() {
		return trustLevel;
	}

	@Override
	public void setTrustLevel(RpcTrustLevel trustLevel) {
		this.trustLevel = trustLevel;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	@Override
	public Collection<RpcServiceBinding> getServiceBindings() {
		return Collections.unmodifiableCollection(bindingMap.values());
	}

	@Override
	public void bind(String name, RpcService service) {
		logger.trace("kraken rpc: binding {}:{} to {}", new Object[] { channel.getId(), name, service.getClass().getName() });
		bindingMap.put(name, new RpcServiceBindingImpl(name, service));
	}

	@Override
	public void unbind(String name) {
		bindingMap.remove(name);
	}

	@Override
	public RpcServiceBinding findServiceBinding(String serviceName) {
		return bindingMap.get(serviceName);
	}

	@Override
	public RpcSession createSession(String serviceName) throws RpcException, InterruptedException {
		return createSession(serviceName, new Properties());
	}

	@Override
	public RpcSession createSession(String serviceName, Properties props) throws RpcException, InterruptedException {
		RpcSession session = findSession(0);
		Integer sessionId = (Integer) session.call("session-request", new Object[] { serviceName, toMap(props) }, 5000);
		if (sessionId != null) {
			RpcSessionImpl newSession = new RpcSessionImpl(sessionId, serviceName, session.getConnection());
			sessionMap.put(sessionId, newSession);
			return newSession;
		}

		return null;
	}

	private Map<String, Object> toMap(Properties props) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Object key : props.keySet()) {
			m.put(key.toString(), props.get(key));
		}
		return m;
	}

	public RpcSession openSession(int newSessionId, String serviceName) {
		RpcServiceBinding binding = bindingMap.get(serviceName);
		if (binding == null)
			return null;

		RpcService service = binding.getService();

		RpcSession session = new RpcSessionImpl(newSessionId, serviceName, this);
		RpcSessionEvent event = new RpcSessionEventImpl(RpcSessionEvent.Opened, session);
		try {
			// create pseudo rpc message and invoke session request callback
			RpcMessage pseudoMessage = new RpcMessage(
					new Object[] { new HashMap<String, Object>(), new HashMap<String, Object>() });
			pseudoMessage.setSession(session);
			RpcContext.setMessage(pseudoMessage);

			service.sessionRequested(event);
			if (session.getState() == RpcSessionState.Opened) {
				if (!session.getServiceName().equals("rpc-control"))
					logger.trace("kraken rpc: session created [{}]", session);

				sessionMap.put(newSessionId, session);

				// invoke session opened callback and try peering
				service.sessionOpened(event);
				return session;
			}
		} catch (Exception e) {
			logger.error("kraken rpc: cannot create session", e);
		}

		return null;
	}

	@Override
	public RpcSession findSession(int sessionId) {
		return sessionMap.get(sessionId);
	}

	@Override
	public Collection<RpcSession> getSessions() {
		return Collections.unmodifiableCollection(sessionMap.values());
	}

	@Override
	public RpcSession getSession(int id) {
		return sessionMap.get(id);
	}

	@Override
	public void send(RpcMessage msg) {
		if (!msg.containsHeader("id"))
			throw new IllegalStateException("id header required");

		channel.write(msg);
	}

	@Override
	public void close() {
		if (state == RpcConnectionState.Closed)
			return;

		state = RpcConnectionState.Closed;

		// close all sessions
		for (RpcSession s : getSessions()) {
			s.close();
		}

		// unbind all services
		for (RpcServiceBinding binding : bindingMap.values()) {
			RpcService s = binding.getService();
			try {
				s.connectionClosed(this);
			} catch (Exception e) {
				RpcExceptionEvent event = new RpcExceptionEventImpl(e);
				s.exceptionCaught(event);
			}
		}

		// notify handler
		for (RpcConnectionEventListener callback : callbacks) {
			try {
				callback.connectionClosed(this);
			} catch (Exception e) {
				logger.warn("kraken rpc: event listener should not throw exception", e);
			}
		}

		// close socket
		channel.close().awaitUninterruptibly();
		logger.trace("kraken rpc: connection closed id={}, peer={}", channel.getId(), getRemoteAddress());
	}

	@Override
	public Object getProperty(String key) {
		return props.get(key);
	}

	@Override
	public void setProperty(String key, Object value) {
		props.put(key, value);
	}

	@Override
	public Collection<String> getPropertyKeys() {
		return Collections.unmodifiableCollection(props.keySet());
	}

	@Override
	public void sessionClosed(RpcSession session) {
		RpcServiceBinding binding = bindingMap.get(session.getServiceName());
		RpcService service = binding.getService();
		try {
			RpcSessionEvent event = new RpcSessionEventImpl(RpcSessionEvent.Closed, session);
			service.sessionClosed(event);
		} catch (Exception e) {
			RpcExceptionEvent event = new RpcExceptionEventImpl(e);
			service.exceptionCaught(event);
		}
	}

	@Override
	public void requestPeering(RpcPeeringCallback callback) {
		requestPeering(callback, null);
	}

	@Override
	public void requestPeering(RpcPeeringCallback callback, String password) {
		if (password != null)
			setProperty("password", password);

		RpcSession session = findSession(0);

		// call peer request
		session.call("peering-request", new Object[] { guid }, new NonPasswordPeering(session, callback, password));
	}

	@Override
	public void addListener(RpcConnectionEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeListener(RpcConnectionEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public int nextMessageId() {
		return idCounter.incrementAndGet();
	}

	@Override
	public int nextSessionId() {
		return sessionCounter.addAndGet(2);
	}

	@Override
	public RpcBlockingTable getBlockingTable() {
		return blockingTable;
	}

	@Override
	public RpcAsyncTable getAsyncTable() {
		return asyncTable;
	}

	@Override
	public void waitControlReady() {
		try {
			if (!isControlReady)
				logger.trace("kraken rpc: waiting control ready for [{}]", getId());

			controlLock.lock();
			while (!isControlReady) {
				controlReady.await();
			}
		} catch (InterruptedException e) {
			logger.trace("kraken rpc: interrupted waiting for peer [{}] control ready", peerGuid);
		} finally {
			controlLock.unlock();
		}
	}

	@Override
	public void waitPeering() {
		waitPeering(0);
	}

	@Override
	public void waitPeering(long timeout) {
		try {
			long begin = new Date().getTime();
			peeringLock.lock();
			while (trustedLevel == null) {
				if (timeout > 0 && new Date().getTime() - begin > timeout) {
					logger.error("kraken rpc: give up waiting peering [{}] response", peerGuid);
					throw new RpcException("peering timeout: " + timeout);
				}

				peeringReady.await(200, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			logger.trace("kraken rpc: interrupted waiting for peering [{}]", peerGuid);
		} finally {
			peeringLock.unlock();
		}
	}

	@Override
	public void setControlReady() {
		try {
			controlLock.lock();
			isControlReady = true;
			controlReady.signalAll();
		} finally {
			controlLock.unlock();
		}
	}

	public void setPeerCert(X509Certificate peerCert) {
		this.peerCert = peerCert;
	}

	@Override
	public String toString() {
		return String.format("id=%d, peer=(%s, %s), trusted level=%s, ssl=%s", channel.getId(), getPeerGuid(),
				getRemoteAddress(), trustedLevel, getPeerCertificate() != null);
	}

	private class NonPasswordPeering implements RpcAsyncCallback {
		private RpcSession session;
		private RpcPeeringCallback callback;
		private String password;

		public NonPasswordPeering(RpcSession session, RpcPeeringCallback callback, String password) {
			this.session = session;
			this.callback = callback;
			this.password = password;
		}

		@Override
		public void onComplete(RpcAsyncResult r) {
			if (r.isError()) {
				logger.error("kraken rpc: peering error", r.getException());
				session.getConnection().close();
				return;
			}

			Object[] tuple = (Object[]) r.getReturn();
			String type = (String) tuple[0];
			peerGuid = (String) tuple[1];

			logger.debug("kraken rpc: non password peering result [type={}, peer={}]", type, peerGuid);

			if (type.equals("success"))
				onSuccess(tuple);
			else if (type.equals("challenge"))
				onFaliure(tuple);
		}

		private void onFaliure(Object[] tuple) {
			// set peer information
			String nonce = (String) tuple[2];

			// hash = sha1(password + nonce)
			String hash = PasswordUtil.calculatePasswordHash(password, nonce);

			// try authenticate
			session.call("authenticate", new Object[] { guid, hash }, new PasswordPeering(session, callback));
		}

		private void onSuccess(Object[] tuple) {
			// no challenge
			int trustedLevel = (Integer) tuple[2];
			setTrustedLevel(RpcTrustLevel.parse(trustedLevel));

			logger.trace("kraken rpc: {} login success, trusted level = {}", new Object[] { peerGuid, trustedLevel });
			RpcConnection conn = session.getConnection();
			callback.onCompleted(conn);
		}
	}

	private class PasswordPeering implements RpcAsyncCallback {
		private RpcSession session;
		private RpcPeeringCallback callback;

		public PasswordPeering(RpcSession session, RpcPeeringCallback callback) {
			this.session = session;
			this.callback = callback;
		}

		@Override
		public void onComplete(RpcAsyncResult r) {
			Object[] ret = (Object[]) r.getReturn();
			boolean peered = (Boolean) ret[0];
			int trustedLevel = (Integer) ret[1];

			// challenge result
			logger.trace("kraken rpc: {} login result = {}, trusted level = {}", new Object[] { peerGuid, peered, trustedLevel });

			if (peered)
				setTrustedLevel(RpcTrustLevel.parse(trustedLevel));

			callback.onCompleted(session.getConnection());
		}
	}
}