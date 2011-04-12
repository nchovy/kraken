package org.krakenapps.rpc.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionEventListener;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcExceptionEvent;
import org.krakenapps.rpc.RpcMessage;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.krakenapps.rpc.RpcService;
import org.krakenapps.rpc.RpcServiceBinding;
import org.krakenapps.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelPipelineCoverage("all")
public class RpcHandler extends SimpleChannelHandler implements RpcConnectionEventListener {
	final Logger logger = LoggerFactory.getLogger(RpcHandler.class.getName());
	private static final int HIGH_WATERMARK = 10;

	private String guid;
	private RpcControlService control;
	private RpcPeerRegistry peerRegistry;
	private ThreadPoolExecutor executor;
	private LinkedBlockingQueue<Runnable> queue;

	private Map<Integer, RpcConnectionImpl> connMap;
	private ConcurrentHashMap<RpcService, String> serviceMap;

	public RpcHandler(String guid, RpcPeerRegistry peerRegistry) {
		this.guid = guid;
		this.peerRegistry = peerRegistry;
		this.connMap = new ConcurrentHashMap<Integer, RpcConnectionImpl>();
		this.control = new RpcControlService(peerRegistry);
		this.serviceMap = new ConcurrentHashMap<RpcService, String>();
	}

	public void start() {
		queue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS, queue, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Kraken RPC Handler");
			}
		});
	}

	public void stop() {
		executor.shutdownNow();
		executor = null;
	}

	public RpcConnection newClientConnection(Channel channel, X509Certificate peerCert) {
		return initializeConnection(channel, peerCert);
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
		RpcConnectionImpl newConnection = new RpcConnectionImpl(channel, guid, peerRegistry);

		// register handler to connection
		newConnection.addListener(this);

		connMap.put(channel.getId(), newConnection);

		logger.info("kraken-rpc: [{}] {} connected, remote={}", new Object[] { newConnection.getId(),
				newConnection.isClient() ? "client" : "server", newConnection.getRemoteAddress() });

		// ssl handshake
		SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			ChannelFuture cf = sslHandler.handshake(e.getChannel());
			cf.addListener(new SslConnectInitializer(sslHandler));
			return;
		}

		init(e.getChannel(), null);
	}

	private class SslConnectInitializer implements ChannelFutureListener, Runnable {
		private SslHandler sslHandler;
		private Channel channel;
		private X509Certificate peerCert;

		public SslConnectInitializer(SslHandler sslHandler) {
			this.sslHandler = sslHandler;
		}

		@Override
		public void operationComplete(ChannelFuture cf) throws Exception {
			Certificate[] certs = sslHandler.getEngine().getSession().getPeerCertificates();

			peerCert = (X509Certificate) certs[0];
			channel = cf.getChannel();

			String peerCommonName = peerCert.getSubjectDN().getName();
			logger.info("kraken-rpc: new peer certificate subject={}, remote={}", peerCommonName,
					channel.getRemoteAddress());

			executor.submit(this);
		}

		@Override
		public void run() {
			init(channel, peerCert);
		}
	}

	private void init(Channel channel, X509Certificate peerCert) {
		// client will initialize connection at caller side.
		logger.trace("kraken-rpc: new channel {}", channel.getLocalAddress());

		if (channel.getParent() != null)
			initializeConnection(channel, peerCert);
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		RpcConnection connection = findConnection(e.getChannel().getId());
		// connection may be removed by active close.
		if (connection == null)
			return;

		// passively close
		connection.close();
	}

	private RpcConnection initializeConnection(Channel channel, X509Certificate peerCert) {
		RpcConnectionImpl newConnection = connMap.get(channel.getId());
		newConnection.setPeerCert(peerCert);

		// bind rpc control service by default
		newConnection.bind("rpc-control", control);

		// create rpc control session without message exchange. it is supported
		// by default.
		RpcSession session = newConnection.openSession(0, "rpc-control");
		if (session == null) {
			newConnection.close();
			return null;
		}

		// auto-binding
		for (RpcService service : serviceMap.keySet()) {
			String serviceName = serviceMap.get(service);
			newConnection.bind(serviceName, service);
		}

		// at this moment, control session is opened
		newConnection.setControlReady();

		// try peering
		boolean peeringResult = newConnection.requestPeering();
		if (!peeringResult) {
			logger.error("kraken-rpc: {} peering failed with {}, connection will be closed", newConnection.getId(),
					newConnection.getRemoteAddress());

			newConnection.close();
			return null;
		}

		return newConnection;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable cause = e.getCause();
		if (cause instanceof ConnectException) {
			logger.debug("kraken-rpc: connect failed");
			return;
		}

		logger.warn("kraken-rpc: unhandled exception", cause);

		// close
		e.getChannel().close();
	}

	private class MessageHandler implements Runnable {
		private MessageEvent event;
		private Channel channel;

		public MessageHandler(Channel channel, MessageEvent event) {
			this.channel = event.getChannel();
			this.event = event;
		}

		@Override
		public void run() {
			try {
				handle();
			} catch (Exception e) {
				logger.error("kraken-rpc: cannot handle message", e);
			} finally {
				if (channel.isOpen())
					channel.setReadable(true);
			}
		}

		private void handle() {
			Channel channel = event.getChannel();
			RpcConnection conn = findConnection(channel.getId());
			if (conn == null)
				throw new IllegalStateException("channel " + channel.getId() + " not found. already disconnected.");

			conn.waitControlReady();

			RpcMessage msg = new RpcMessage((Object[]) event.getMessage());
			Integer id = (Integer) msg.getHeader("id");
			Integer sessionId = (Integer) msg.getHeader("session");
			String type = (String) msg.getHeader("type");
			String methodName = msg.getString("method");

			if (logger.isTraceEnabled())
				logger.trace(
						"kraken-rpc: msg received - connection: {}, session: {}, message id: {}, type: {}, method: {}",
						new Object[] { conn.getId(), sessionId, id, type, methodName });

			RpcSession session = conn.findSession(sessionId);
			if (session == null) {
				logger.warn("kraken-rpc: session {} not found, connection={}, peer={}", new Object[] { sessionId,
						conn.getId(), conn.getRemoteAddress() });
				return;
			}

			msg.setSession(session);
			String serviceName = session.getServiceName();
			RpcServiceBinding binding = conn.findServiceBinding(serviceName);

			if (type.equals("rpc-call")) {
				try {
					Object ret = call(binding, msg);
					RpcMessage resp = RpcMessage.newResponse(conn.nextMessageId(), session.getId(), id, methodName, ret);
					channel.write(resp);
				} catch (Throwable t) {
					if (t.getCause() != null)
						t = t.getCause();

					cutStackTrace(t);

					logger.trace("kraken-rpc: call [" + conn.getId() + ", " + methodName + "] throws exception", t);
					RpcMessage error = RpcMessage.newException(conn.nextMessageId(), session.getId(), id,
							t.getMessage());
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
			} else if (type.equals("rpc-ret") || type.equals("rpc-error")) {
				RpcBlockingTable blockingTable = conn.getBlockingTable();
				int originalId = (Integer) msg.getHeader("ret-for");
				if (logger.isDebugEnabled())
					logger.debug("kraken-rpc: response for msg {}", originalId);

				blockingTable.signal(originalId, msg);
			}
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		Channel channel = ctx.getChannel();
		if (queue.size() > HIGH_WATERMARK) {
			if (logger.isTraceEnabled())
				logger.trace("kraken-rpc: pause channel [{}]", channel.getRemoteAddress());

			channel.setReadable(false);
		}

		executor.submit(new MessageHandler(channel, event));
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

		logger.trace("kraken-rpc: received rpc-call, connection={}, session={}, method={}", new Object[] {
				connectionId, sessionId, methodName });

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
				return method.invoke(binding.getService(), args);
			}
		} finally {
			RpcContext.setMessage(null);
		}
	}

	/*
	 * RpcConnectionEventListener callback dispatcher
	 */

	@Override
	public void connectionOpened(RpcConnection connection) {
		// ignore
	}

	@Override
	public void connectionClosed(RpcConnection connection) {
		// remove connection
		connMap.remove(connection.getId());
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
}
