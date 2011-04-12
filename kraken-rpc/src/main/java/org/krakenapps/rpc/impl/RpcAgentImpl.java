package org.krakenapps.rpc.impl;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcAgentImpl implements RpcAgent {
	private final Logger logger = LoggerFactory.getLogger(RpcAgentImpl.class.getName());
	private BundleContext bc;
	private RpcPeerRegistry peerRegistry;

	private Channel listener;
	private Channel sslListener;

	private int plainPort = 7139;
	private int sslPort = 7140;
	private RpcHandler handler;
	private RpcServiceTracker tracker;
	private KeyStoreManager keyStoreManager;

	public RpcAgentImpl(BundleContext bc) {
		this.bc = bc;
		peerRegistry = new RpcPeerRegistryImpl(getPreferences());
		handler = new RpcHandler(getGuid(), peerRegistry);
		tracker = new RpcServiceTracker(bc, handler);
	}

	public void start() throws Exception {
		try {
			handler.start();
			bc.addServiceListener(tracker);
			bind(plainPort);
			bindSsl(sslPort, "rpc-ca", "rpc-agent");

			// register all auto-wiring RPC services.
			tracker.scan();
		} catch (Exception e) {
			stop();
			throw e;
		}
	}

	public void stop() {
		unbind();
		unbindSsl();
		bc.removeServiceListener(tracker);
		handler.stop();
	}

	@Override
	public String getGuid() {
		try {
			Preferences prefs = getPreferences();
			Preferences p = prefs.node("/kraken-rpc");
			String guid = p.get("guid", null);
			if (guid == null) {
				p.put("guid", UUID.randomUUID().toString());
			} else {
				return guid;
			}

			p.flush();
			p.sync();

			return p.get("guid", guid);
		} catch (BackingStoreException e) {
			logger.warn("kraken-rpc: cannot generate guid", e);
		}

		return null;
	}

	@Override
	public RpcConnection connect(InetSocketAddress address) {
		return connect(address.getAddress().getHostAddress(), address.getPort());
	}

	@Override
	public RpcConnection connectSsl(InetSocketAddress address) {
		return connectSsl(address, "rpc-agent", "rpc-ca");
	}

	@Override
	public RpcConnection connectSsl(InetSocketAddress address, String keyAlias, String trustAlias) {
		return connectSsl(address.getAddress().getHostAddress(), address.getPort(), keyAlias, trustAlias);
	}

	@Override
	public RpcConnection connectSsl(String host, int port, final String keyAlias, final String trustAlias) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors
				.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyAlias, "SunX509");

				TrustManager[] trustManagers = null;
				KeyManager[] keyManagers = null;
				if (tmf != null)
					trustManagers = tmf.getTrustManagers();
				if (kmf != null)
					keyManagers = kmf.getKeyManagers();

				SSLContext clientContext = SSLContext.getInstance("TLS");
				clientContext.init(keyManagers, trustManagers, new SecureRandom());

				SSLEngine engine = clientContext.createSSLEngine();
				engine.setUseClientMode(true);

				SslHandler sslHandler = new SslHandler(engine);
				pipeline.addLast("ssl", sslHandler);
				pipeline.addLast("decoder", new RpcDecoder());
				pipeline.addLast("encoder", new RpcEncoder());
				pipeline.addLast("handler", handler);

				return pipeline;
			}
		});

		ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
		Channel channel = channelFuture.awaitUninterruptibly().getChannel();
		SslHandler sslHandler = (SslHandler) channel.getPipeline().get("ssl");
		try {
			sslHandler.handshake(channel).await(5000);
			X509Certificate peerCert = (X509Certificate) sslHandler.getEngine().getSession().getPeerCertificates()[0];
			return doPostConnectSteps(channel, peerCert);
		} catch (Exception e) {
			if (channel != null)
				channel.close();
			throw new RuntimeException("ssl connection failed", e);
		}
	}

	/**
	 * Receive and set peer certificate for connection.
	 */
	private RpcConnection doPostConnectSteps(Channel channel, X509Certificate peerCert) {
		if (channel != null && channel.isConnected()) {
			if (peerCert != null)
				logger.info("kraken-rpc: connected with peer {}", peerCert.getSubjectDN().getName());

			return handler.newClientConnection(channel, peerCert);
		}

		return null;
	}

	@Override
	public RpcConnection connect(String host, int port) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors
				.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("decoder", new RpcDecoder());
				pipeline.addLast("encoder", new RpcEncoder());
				pipeline.addLast("handler", handler);
				return pipeline;
			}
		});

		ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
		Channel channel = channelFuture.awaitUninterruptibly().getChannel();
		return doPostConnectSteps(channel, null);
	}

	public void bindSsl(int port, final String trustStoreAlias, final String keyStoreAlias) throws Exception {
		if (sslListener != null) {
			logger.warn("kraken-rpc: rpc port already opened, {}", listener.getRemoteAddress());
			return;
		}

		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors
				.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				// ssl
				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustStoreAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyStoreAlias, "SunX509");

				TrustManager[] trustManagers = null;
				KeyManager[] keyManagers = null;
				if (tmf != null)
					trustManagers = tmf.getTrustManagers();
				if (kmf != null)
					keyManagers = kmf.getKeyManagers();

				SSLContext serverContext = SSLContext.getInstance("TLS");
				serverContext.init(keyManagers, trustManagers, new SecureRandom());

				SSLEngine engine = serverContext.createSSLEngine();
				engine.setUseClientMode(false);
				engine.setNeedClientAuth(true);

				pipeline.addLast("ssl", new SslHandler(engine));

				// decoder, encoder and handler
				pipeline.addLast("decoder", new RpcDecoder());
				pipeline.addLast("encoder", new RpcEncoder());
				pipeline.addLast("handler", handler);

				return pipeline;
			}
		});

		sslListener = bootstrap.bind(new InetSocketAddress(port));

		logger.info("kraken-rpc: {} ssl port opened", port);
	}

	public void bind(int port) throws Exception {
		if (listener != null) {
			logger.warn("kraken-rpc: rpc port already opened, {}", listener.getRemoteAddress());
			return;
		}

		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors
				.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		ChannelPipeline pipeline = bootstrap.getPipeline();

		// decoder, encoder and handler
		pipeline.addLast("decoder", new RpcDecoder());
		pipeline.addLast("encoder", new RpcEncoder());
		pipeline.addLast("handler", handler);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		listener = bootstrap.bind(new InetSocketAddress(port));

		logger.info("kraken-rpc: {} port opened", port);
	}

	public void unbind() {
		if (listener == null)
			return;

		logger.info("kraken-rpc: unbinding listen port");
		listener.unbind();
		listener.close().awaitUninterruptibly();
		listener = null;
	}

	public void unbindSsl() {
		if (sslListener == null)
			return;

		logger.info("kraken-rpc: unbinding ssl listen port");
		sslListener.unbind();
		sslListener.close().awaitUninterruptibly();
		sslListener = null;
	}

	@Override
	public RpcConnection findConnection(int id) {
		return handler.findConnection(id);
	}

	@Override
	public Collection<RpcConnection> getConnections() {
		return handler.getConnections();
	}

	private Preferences getPreferences() {
		ServiceReference ref = bc.getServiceReference(PreferencesService.class.getName());
		PreferencesService prefsService = (PreferencesService) bc.getService(ref);
		Preferences prefs = prefsService.getSystemPreferences();
		return prefs;
	}

	@Override
	public RpcPeerRegistry getPeerRegistry() {
		return peerRegistry;
	}
}
