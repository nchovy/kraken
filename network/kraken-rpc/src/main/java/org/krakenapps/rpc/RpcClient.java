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

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.rpc.impl.RpcDecoder;
import org.krakenapps.rpc.impl.RpcEncoder;
import org.krakenapps.rpc.impl.RpcHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
	private final Logger logger = LoggerFactory.getLogger(RpcClient.class.getName());
	private final boolean ownHandler;

	private ClientBootstrap bootstrap;
	private RpcHandler handler;
	private RpcConnection conn;

	public RpcClient(String guid) {
		this.handler = new RpcHandler(guid, new TrustPeerRegistry());
		this.ownHandler = true;
	}

	public RpcClient(RpcHandler handler) {
		this.handler = handler;
		this.ownHandler = false;
	}

	public RpcConnection connect(RpcConnectionProperties props) {
		if (conn != null)
			return conn;

		if (ownHandler)
			handler.start();

		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
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

		Channel channel = null;
		try {
			ChannelFuture channelFuture = bootstrap.connect(props.getRemoteAddress());
			channel = channelFuture.awaitUninterruptibly().getChannel();
			return doPostConnectSteps(channel, props);
		} catch (Exception e) {
			if (channel != null)
				channel.close();

			// shutdown executors
			bootstrap.releaseExternalResources();

			throw new RuntimeException("rpc connection failed", e);
		}
	}

	public RpcConnection connectSsl(RpcConnectionProperties props) {
		if (conn != null)
			return conn;

		if (ownHandler)
			handler.start();

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		final KeyManagerFactory kmf = props.getKeyManagerFactory();
		final TrustManagerFactory tmf = props.getTrustManagerFactory();

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

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

		ChannelFuture channelFuture = bootstrap.connect(props.getRemoteAddress());
		Channel channel = channelFuture.awaitUninterruptibly().getChannel();
		SslHandler sslHandler = (SslHandler) channel.getPipeline().get("ssl");
		try {
			sslHandler.handshake().await(5000);
			X509Certificate peerCert = (X509Certificate) sslHandler.getEngine().getSession().getPeerCertificates()[0];
			props.setPeerCert(peerCert);

			return doPostConnectSteps(channel, props);
		} catch (Exception e) {
			if (channel != null)
				channel.close();

			// shutdown executors
			bootstrap.releaseExternalResources();

			throw new RuntimeException("rpc-ssl connection failed", e);
		}
	}

	/**
	 * Receive and set peer certificate for connection.
	 */
	private RpcConnection doPostConnectSteps(Channel channel, RpcConnectionProperties props) {
		if (channel != null && channel.isConnected()) {
			if (props.getPeerCert() != null)
				logger.trace("kraken-rpc: connected with peer {}", props.getPeerCert().getSubjectDN().getName());

			return handler.newClientConnection(channel, props);
		}

		return null;
	}

	public void close() {
		if (ownHandler)
			handler.stop();

		if (conn != null && conn.isOpen()) {
			conn.close();
		}
		conn = null;

		if (bootstrap != null) {
			bootstrap.releaseExternalResources();
		}
		bootstrap = null;
	}

}
