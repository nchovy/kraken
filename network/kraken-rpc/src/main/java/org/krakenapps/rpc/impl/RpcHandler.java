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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.rpc.RpcAsyncTable;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionEventListener;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcExceptionEvent;
import org.krakenapps.rpc.RpcMessage;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.krakenapps.rpc.RpcPeeringCallback;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcServiceBinding;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcTrustLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcHandler extends SimpleChannelHandler implements Runnable, RpcConnectionEventListener {
	final Logger logger = LoggerFactory.getLogger(RpcHandler.class.getName());
	private static final int HIGH_WATERMARK = 10;

	private String guid;
	private RpcControlService control;
	private ThreadPoolExecutor executor;
	private LinkedBlockingQueue<Runnable> queue;

	private volatile boolean doStop;
	private Thread scheduler;

	private Map<Integer, RpcConnectionImpl> connMap;
	private ConcurrentHashMap<RpcService, String> serviceMap;

	// work key = channel id + session id
	private ConcurrentMap<WorkKey, WorkStatus> worksheet;
	private ConcurrentMap<WorkKey, ConcurrentLinkedQueue<RpcMessage>> channelMessages;

	private CopyOnWriteArraySet<RpcConnectionEventListener> listeners;

	public RpcHandler(String guid, RpcPeerRegistry peerRegistry) {
		this.guid = guid;
		this.connMap = new ConcurrentHashMap<Integer, RpcConnectionImpl>();
		this.control = new RpcControlService(guid, peerRegistry);
		this.serviceMap = new ConcurrentHashMap<RpcService, String>();
		this.listeners = new CopyOnWriteArraySet<RpcConnectionEventListener>();
		this.worksheet = new ConcurrentHashMap<WorkKey, WorkStatus>();
		this.channelMessages = new ConcurrentHashMap<WorkKey, ConcurrentLinkedQueue<RpcMessage>>();
	}

	public void start() {
		if (executor != null)
			return;

		scheduler = new Thread(this, "Kraken RPC Scheduler");
		queue = new LinkedBlockingQueue<Runnable>();
		int cpuCount = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(cpuCount, cpuCount, 10, TimeUnit.SECONDS, queue, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Kraken RPC Handler");
			}
		});

		scheduler.start();
	}

	public void stop() {
		doStop = true;
		scheduler.interrupt();

		if (executor != null) {
			executor.shutdown();
			executor = null;
		}

		for (RpcConnection conn : connMap.values())
			conn.close();

		connMap.clear();
	}

	@Override
	public void run() {

		while (!doStop) {
			try {
				for (WorkKey workKey : channelMessages.keySet()) {
					WorkStatus status = worksheet.get(workKey);
					if (status == null) {
						ConcurrentLinkedQueue<RpcMessage> q = channelMessages.get(workKey);

						// prepare rpc message list
						List<RpcMessage> list = new ArrayList<RpcMessage>();
						while (true) {
							RpcMessage m = q.poll();
							if (m == null)
								break;

							list.add(m);
						}

						if (executor != null && list.size() > 0) {
							// prevent out-of-order execution per rpc session
							worksheet.put(workKey, new WorkStatus(list));
							executor.submit(new MessageHandler(workKey, list));
						} else {
							if (executor == null) {
								logger.trace("kraken rpc: handler threadpool stopped, drop msg");
								doStop = true;
							}
						}
					} else {
						long elapsed = new Date().getTime() - status.lastRun.getTime();
						if (!status.alerted && elapsed > 2000) {
							int i = 0;
							StringBuilder sb = new StringBuilder();
							for (RpcMessage m : status.runningMethods) {
								if (i++ != 0)
									sb.append(",");

								sb.append(m.getString("method"));
								sb.append("(");
								sb.append(m.getHeader("type"));
								sb.append(")");
							}

							logger.warn(
									"kraken rpc: rpc channel [{}] session [{}] work takes long time [{}] elapsed, pending methods [{}]",
									new Object[] { workKey.channelId, workKey.sessionId, elapsed, sb.toString() });

							status.alerted = true;
						}
					}
				}

				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.trace("kraken rpc: rpc run scheduler interrupted");
			} catch (Exception e) {
				logger.error("kraken rpc: rpc run scheduler failed", e);
			}
		}

		logger.trace("kraken rpc: rpc run scheduler stopped");
		doStop = false;
	}

	private static class WorkKey {
		private Channel channel;
		private int channelId;
		private int sessionId;
		private final int hashCode;

		public WorkKey(Channel channel, int sessionId) {
			this.channel = channel;
			this.channelId = channel.getId();
			this.sessionId = sessionId;

			final int prime = 31;
			int result = 1;
			result = prime * result + channelId;
			result = prime * result + sessionId;
			hashCode = result;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WorkKey other = (WorkKey) obj;
			if (channel.getId() != other.channelId)
				return false;
			if (sessionId != other.sessionId)
				return false;
			return true;
		}
	}

	private static class WorkStatus {
		private boolean alerted = false;
		private Date lastRun = new Date();
		private List<RpcMessage> runningMethods = new ArrayList<RpcMessage>();

		private WorkStatus(List<RpcMessage> list) {
			runningMethods = list;
		}
	}

	public RpcConnection newClientConnection(Channel channel, RpcConnectionProperties props) {
		try {
			return initializeConnection(channel, props);
		} catch (InterruptedException e) {
			logger.warn("kraken rpc: interrupted", e);
			return null;
		}
	}

	public RpcConnection findConnection(int id) {
		return connMap.get(id);
	}

	public Collection<RpcConnection> getConnections() {
		return new ArrayList<RpcConnection>(connMap.values());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// register connection
		Channel channel = e.getChannel();
		RpcConnectionImpl newConnection = new RpcConnectionImpl(channel, guid);
		InetSocketAddress addr = newConnection.getRemoteAddress();
		RpcConnectionProperties props = new RpcConnectionProperties(addr.getAddress().getHostAddress(), addr.getPort());

		// register handler to connection
		newConnection.addListener(this);

		// add to connection
		connMap.put(channel.getId(), newConnection);

		logger.trace("kraken rpc: [{}] {} connected, remote={}", new Object[] { newConnection.getId(),
				newConnection.isClient() ? "client" : "server", addr });

		// ssl handshake
		SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			ChannelFuture cf = sslHandler.handshake();
			cf.addListener(new SslConnectInitializer(props, sslHandler));
			return;
		}

		init(e.getChannel(), props);
	}

	private class SslConnectInitializer implements ChannelFutureListener, Runnable {
		private SslHandler sslHandler;
		private Channel channel;
		private RpcConnectionProperties props;

		public SslConnectInitializer(RpcConnectionProperties props, SslHandler sslHandler) {
			this.props = props;
			this.sslHandler = sslHandler;
		}

		@Override
		public void operationComplete(ChannelFuture cf) throws Exception {
			channel = cf.getChannel();

			Certificate[] certs = sslHandler.getEngine().getSession().getPeerCertificates();
			X509Certificate peerCert = (X509Certificate) certs[0];
			props.setPeerCert(peerCert);

			String peerCommonName = peerCert.getSubjectDN().getName();
			logger.trace("kraken rpc: new peer certificate subject={}, remote={}", peerCommonName, channel.getRemoteAddress());

			if (executor != null)
				new Thread(this).start();
			else
				logger.error("kraken rpc: handler threadpool stopped, drop ssl init");
		}

		@Override
		public void run() {
			try {
				init(channel, props);
			} catch (Throwable t) {
				logger.error("kraken rpc: ssl connection init fail", t);
			}
		}
	}

	private void init(Channel channel, RpcConnectionProperties props) {
		// client will initialize connection at caller side.
		logger.trace("kraken rpc: new channel {}", channel.getLocalAddress());

		try {
			if (channel.getParent() != null)
				initializeConnection(channel, props);
		} catch (InterruptedException e) {
			logger.trace("kraken rpc: interrupted", e);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		RpcConnection connection = findConnection(e.getChannel().getId());
		// connection may be removed by active close.
		if (connection == null)
			return;

		// passively close
		logger.trace("kraken rpc: received channel closed event [{}]", connection);
		connection.close();
	}

	private RpcConnection initializeConnection(Channel channel, RpcConnectionProperties props) throws InterruptedException {
		RpcConnectionImpl newConnection = null;

		logger.trace("kraken rpc: start initialize connection [{}]", channel.getId());

		// wait connect callback thread ends
		while (newConnection == null) {
			newConnection = connMap.get(channel.getId());
			if (logger.isTraceEnabled())
				logger.trace("kraken rpc: wait until connection setup [{}]", channel.getId());
			Thread.sleep(100);
		}

		newConnection.setPeerCert(props.getPeerCert());

		if (props.getPassword() != null)
			newConnection.setProperty("password", props.getPassword());

		// bind rpc control service by default
		newConnection.bind("rpc-control", control);

		// create rpc control session without message exchange. it is supported
		// by default.
		RpcSession session = newConnection.openSession(0, "rpc-control");
		if (session == null) {
			logger.error("kraken rpc: cannot open rpc control session, connection [{}]", channel.getId());
			newConnection.close();
			return null;
		}

		// auto-binding
		for (RpcService service : serviceMap.keySet()) {
			String serviceName = serviceMap.get(service);
			try {
				newConnection.bind(serviceName, service);
				if (logger.isTraceEnabled())
					logger.trace("kraken rpc: binding service [{}] to new connection [{}]", serviceName, newConnection);
			} catch (Throwable t) {
				logger.error("kraken rpc: cannot bind service [" + serviceName + "] to connection [" + newConnection + "]", t);
			}
		}

		// at this moment, control session is opened
		logger.trace("kraken rpc: signal control ready [{}]", channel.getId());
		newConnection.setControlReady();

		// try peering
		newConnection.requestPeering(new PeeringHandler(), props.getPassword());
		return newConnection;
	}

	private class PeeringHandler implements RpcPeeringCallback {
		@Override
		public void onCompleted(RpcConnection conn) {
			if (conn.getTrustedLevel() == null || conn.getTrustedLevel() == RpcTrustLevel.Untrusted) {
				logger.warn("kraken rpc: {} peering failed with {}", conn.getId(), conn.getRemoteAddress());
			} else {
				connectionOpened(conn);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable cause = e.getCause();
		if (cause instanceof ConnectException) {
			logger.debug("kraken rpc: connect failed");
			return;
		}

		if (cause instanceof ClosedChannelException) {
			logger.debug("kraken rpc: connection [{}] closed", e.getChannel().getRemoteAddress());
			return;
		}

		logger.error("kraken rpc: unhandled exception", cause);

		// close
		e.getChannel().close();
	}

	private class MessageHandler implements Runnable {
		private WorkKey workKey;
		private List<RpcMessage> msgs;
		private Channel channel;
		private RpcConnection conn;

		public MessageHandler(WorkKey workKey, List<RpcMessage> msgs) {
			this.workKey = workKey;
			this.channel = workKey.channel;
			this.msgs = msgs;
			this.conn = findConnection(channel.getId());

			if (conn == null)
				throw new IllegalStateException("channel " + channel.getId() + " not found. already disconnected.");
		}

		@Override
		public void run() {
			try {
				if (logger.isDebugEnabled())
					logger.debug("kraken rpc: channel [{}], begin {} request handling", channel.getId(), msgs.size());

				for (RpcMessage msg : msgs)
					handle(channel, conn, msg);
			} catch (Exception e) {
				logger.error("kraken rpc: cannot handle message", e);
			} finally {
				// clear work status
				worksheet.remove(workKey);

				if (channel.isOpen() && !channel.isReadable()) {
					logger.debug("kraken rpc: channel [{}], set readable true", channel.getId());
					channel.setReadable(true);
				}
			}
		}

	}

	private void handle(Channel channel, RpcConnection conn, RpcMessage msg) {
		conn.waitControlReady();

		Integer id = (Integer) msg.getHeader("id");
		Integer sessionId = (Integer) msg.getHeader("session");
		String type = (String) msg.getHeader("type");
		String methodName = msg.getString("method");

		if (logger.isTraceEnabled())
			logger.trace("kraken rpc: handle msg - connection: {}, session: {}, message id: {}, type: {}, method: {}",
					new Object[] { conn.getId(), sessionId, id, type, methodName });

		RpcSession session = conn.findSession(sessionId);
		if (session == null) {
			logger.warn("kraken rpc: session {} not found, connection={}, peer={}, msg id={}, method={}", new Object[] {
					sessionId, conn.getId(), conn.getRemoteAddress(), id, methodName });
			return;
		}

		msg.setSession(session);
		String serviceName = session.getServiceName();
		RpcServiceBinding binding = conn.findServiceBinding(serviceName);
		if (binding == null && (type.equals("rpc-call") || type.equals("rpc-post"))) {
			int newId = conn.nextMessageId();
			String cause = "service not found: " + serviceName;
			logger.debug("kraken rpc: connection={}, service [{}] not found", conn, serviceName);
			RpcMessage error = RpcMessage.newException(newId, session.getId(), id, cause);
			channel.write(error);
			return;
		}

		if (type.equals("rpc-call")) {
			int newId = conn.nextMessageId();
			try {
				Object ret = call(binding, msg);
				RpcMessage resp = RpcMessage.newResponse(newId, session.getId(), id, methodName, ret);
				channel.write(resp);

				logger.trace("kraken rpc: return for [id={}, ret={}, session={}, method={}]",
						new Object[] { newId, id, session.getId(), methodName });
			} catch (Throwable t) {
				if (t.getCause() != null)
					t = t.getCause();

				cutStackTrace(t);

				logger.trace("kraken rpc: throws exception for call [" + conn.getId() + ", " + methodName + "]", t);
				RpcMessage error = RpcMessage.newException(newId, session.getId(), id, t.getMessage());
				channel.write(error);
			}
		} else if (type.equals("rpc-post")) {
			try {
				// just call
				call(binding, msg);
			} catch (Throwable t) {
				RpcExceptionEvent ex = new RpcExceptionEventImpl(t);
				RpcService service = binding.getService();
				service.exceptionCaught(ex);
			}
		}
	}

	private void handleResponse(Channel channel, RpcMessage msg) {
		RpcConnection conn = findConnection(channel.getId());
		if (conn == null)
			throw new IllegalStateException("channel " + channel.getId() + " not found. already disconnected.");

		RpcAsyncTable asyncTable = conn.getAsyncTable();
		RpcBlockingTable blockingTable = conn.getBlockingTable();

		int originalId = (Integer) msg.getHeader("ret-for");
		if (logger.isDebugEnabled())
			logger.debug("kraken rpc: response for msg {}", originalId);

		if (asyncTable.contains(originalId))
			asyncTable.signal(originalId, msg);
		else
			blockingTable.signal(originalId, msg);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		Channel channel = ctx.getChannel();
		RpcMessage msg = new RpcMessage((Object[]) event.getMessage());
		if (logger.isDebugEnabled()) {
			Integer sessionId = (Integer) msg.getHeader("session");
			Integer id = (Integer) msg.getHeader("id");
			String methodName = msg.getString("method");
			String type = (String) msg.getHeader("type");
			logger.debug("kraken rpc: msg received - connection: {}, session: {}, message id: {}, type: {}, method: {}",
					new Object[] { channel.getId(), sessionId, id, type, methodName });
		}

		// handle return or exception (fast and short path)
		String type = (String) msg.getHeader("type");
		if (type.equals("rpc-ret") || type.equals("rpc-error")) {
			handleResponse(channel, msg);
			return;
		}

		// schedule call or post handling (long running)
		if (queue.size() > HIGH_WATERMARK) {
			if (logger.isTraceEnabled())
				logger.trace("kraken rpc: pause channel [{}]", channel.getRemoteAddress());

			channel.setReadable(false);
		}

		// fast path for control service
		Integer sessionId = (Integer) msg.getHeader("session");
		if (sessionId != null && sessionId == 0) {
			RpcConnection conn = findConnection(channel.getId());
			if (conn == null)
				throw new IllegalStateException("channel " + channel.getId() + " not found. already disconnected.");

			handle(channel, conn, msg);
			return;
		}

		// cannot use msg.getSession().getId() here (not set)
		WorkKey workKey = new WorkKey(channel, (Integer) msg.getHeader("session"));
		ConcurrentLinkedQueue<RpcMessage> q = channelMessages.get(workKey);
		if (q == null) {
			q = new ConcurrentLinkedQueue<RpcMessage>();
			ConcurrentLinkedQueue<RpcMessage> old = channelMessages.putIfAbsent(workKey, q);
			if (old != null)
				q = old;
		}

		q.add(msg);
	}

	/**
	 * Removes deep netty related stacktrace.
	 * 
	 * @param t
	 *            the exception
	 */
	private void cutStackTrace(Throwable t) {
		StackTraceElement[] stackTrace = t.getStackTrace();
		int level = 0;
		for (int i = 0; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().startsWith("org.jboss.netty.channel"))
				break;

			level++;
		}

		stackTrace = Arrays.copyOfRange(stackTrace, 0, level);
		t.setStackTrace(stackTrace);
	}

	/**
	 * Finds and invoke local rpc method.
	 * 
	 * @param binding
	 * @param msg
	 *            the received rpc message
	 * @return the return value of the rpc method
	 */
	private Object call(RpcServiceBinding binding, RpcMessage msg) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		String type = (String) msg.getHeader("type");
		if (!type.equals("rpc-call") && !type.equals("rpc-post"))
			throw new IllegalStateException("type should be rpc-call or rpc-post");

		int connectionId = msg.getSession().getConnection().getId();
		Integer sessionId = (Integer) msg.getHeader("session");
		String methodName = msg.getString("method");

		logger.trace("kraken rpc: received rpc-call, connection={}, session={}, method={}", new Object[] { connectionId,
				sessionId, methodName });

		Method method = binding.getMethod(methodName);
		if (method == null)
			throw new IllegalStateException("method not found: " + methodName);

		// set rpc context
		RpcContext.setMessage(msg);
		try {
			// parameters should be one rpc message or others
			Class<?>[] clazzes = method.getParameterTypes();
			if (clazzes.length == 1 && clazzes[0].equals(RpcMessage.class)) {
				return method.invoke(binding.getService(), msg);
			} else {
				Object[] args = (Object[]) msg.get("params");
				if (logger.isTraceEnabled())
					traceMethodCall(connectionId, sessionId, methodName, args);

				return method.invoke(binding.getService(), args);
			}
		} finally {
			RpcContext.setMessage(null);
		}
	}

	private void traceMethodCall(int conn, int session, String method, Object[] args) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			if (i != 0)
				sb.append(", ");

			sb.append(args[i]);
		}

		logger.trace("kraken rpc: method call, conn [{}], session [{}], method [{}], params [{}]", new Object[] { conn, session,
				method, sb.toString() });
	}

	/*
	 * RpcConnectionEventListener callback dispatcher
	 */

	@Override
	public void connectionOpened(RpcConnection connection) {
		// notify listeners
		for (RpcConnectionEventListener listener : listeners) {
			try {
				listener.connectionOpened(connection);
			} catch (Exception e) {
				logger.warn("kraken rpc: connection listener should not throw any exception", e);
			}
		}
	}

	@Override
	public void connectionClosed(RpcConnection connection) {
		logger.trace("kraken rpc: connection closed [{}], removed from connection map", connection);

		// remove connection
		connMap.remove(connection.getId());

		// notify listeners
		for (RpcConnectionEventListener listener : listeners) {
			try {
				listener.connectionClosed(connection);
			} catch (Exception e) {
				logger.warn("kraken rpc: connection listener should not throw any exception", e);
			}
		}
	}

	/*
	 * For RpcServiceTracker
	 */

	public void addService(RpcService service, String rpcName) {
		serviceMap.putIfAbsent(service, rpcName);
	}

	public void removeService(RpcService service) {
		serviceMap.remove(service);
	}

	public void addConnectionListener(RpcConnectionEventListener listener) {
		listeners.add(listener);
	}

	public void removeConnectionListener(RpcConnectionEventListener listener) {
		listeners.remove(listener);
	}
}
