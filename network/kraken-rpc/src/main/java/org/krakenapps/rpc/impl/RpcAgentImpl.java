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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.rpc.RpcBindingProperties;
import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnectionEventListener;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "rpc-agent")
@Provides
public class RpcAgentImpl implements RpcAgent {
	private final Logger logger = LoggerFactory.getLogger(RpcAgentImpl.class.getName());
	private BundleContext bc;
	private RpcPeerRegistry peerRegistry;

	private RpcHandler handler;
	private RpcServiceTracker tracker;

	private ConcurrentMap<RpcBindingProperties, Channel> bindings;

	@Requires
	private KeyStoreManager keyStoreManager;

	@Requires
	private ConfigService conf;

	public RpcAgentImpl(BundleContext bc) {
		this.bc = bc;
		peerRegistry = new RpcPeerRegistryImpl(conf);
		handler = new RpcHandler(getGuid(), peerRegistry);
		tracker = new RpcServiceTracker(bc, handler);
		bindings = new ConcurrentHashMap<RpcBindingProperties, Channel>();
	}

	@Validate
	public void start() throws Exception {
		try {
			handler.start();
			bc.addServiceListener(tracker);

			// open configured bindings
			ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
			ConfigIterator it = db.findAll(RpcBindingProperties.class);
			try {
				while (it.hasNext()) {
					Config c = it.next();
					RpcBindingProperties props = c.getDocument(RpcBindingProperties.class);
					if (props.getKeyAlias() != null && props.getTrustAlias() != null)
						bindSsl(props);
					else
						bind(props);
				}
			} finally {
				it.close();
			}

			// register all auto-wiring RPC services.
			tracker.scan();
		} catch (Exception e) {
			stop();
			throw e;
		}
	}

	@Invalidate
	public void stop() {
		for (RpcBindingProperties props : bindings.keySet())
			unbind(props);

		bc.removeServiceListener(tracker);
		handler.stop();
	}

	@Override
	public String getGuid() {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		ConfigCollection col = db.ensureCollection("agent");
		Config c = col.findOne(null);
		if (c != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> doc = (Map<String, Object>) c.getDocument();
			return (String) doc.get("guid");
		}

		Map<String, Object> doc = new HashMap<String, Object>();
		String guid = UUID.randomUUID().toString();
		doc.put("guid", guid);
		col.add(doc);
		return guid;
	}

	@Override
	public Collection<RpcBindingProperties> getBindings() {
		return new ArrayList<RpcBindingProperties>(bindings.keySet());
	}

	@Override
	public void open(RpcBindingProperties props) {
		if (bindings.containsKey(props))
			throw new IllegalStateException("already opened: " + props);

		if (props.getKeyAlias() != null && props.getTrustAlias() != null)
			bindSsl(props);
		else
			bind(props);

		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		db.add(props, "kraken-rpc", "opened " + props);
	}

	@Override
	public void close(RpcBindingProperties props) {
		ConfigDatabase db = conf.ensureDatabase("kraken-rpc");
		Predicate p = Predicates.and(Predicates.field("addr", props.getHost()),
				Predicates.field("port", props.getPort()));

		Config c = db.findOne(RpcBindingProperties.class, p);
		if (c != null)
			db.remove(c, false, "kraken-rpc", "closed " + props);

		unbind(props);
	}

	@Override
	public RpcConnection connectSsl(RpcConnectionProperties props) {
		RpcClient client = new RpcClient(handler);
		return client.connectSsl(props);
	}

	@Override
	public RpcConnection connect(RpcConnectionProperties props) {
		RpcClient client = new RpcClient(handler);
		return client.connect(props);
	}

	private Channel bindSsl(RpcBindingProperties props) {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		final String keyAlias = props.getKeyAlias();
		final String trustAlias = props.getTrustAlias();

		ServerBootstrap bootstrap = new ServerBootstrap(factory);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();

				// ssl
				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyAlias, "SunX509");

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

		InetSocketAddress address = new InetSocketAddress(props.getHost(), props.getPort());
		Channel channel = bootstrap.bind(address);
		bindings.put(props, channel);

		logger.info("kraken-rpc: {} ssl port opened", address);
		return channel;
	}

	private Channel bind(RpcBindingProperties props) {
		ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ServerBootstrap bootstrap = new ServerBootstrap(factory);
		ChannelPipeline pipeline = bootstrap.getPipeline();

		// decoder, encoder and handler
		pipeline.addLast("decoder", new RpcDecoder());
		pipeline.addLast("encoder", new RpcEncoder());
		pipeline.addLast("handler", handler);

		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);

		InetSocketAddress address = new InetSocketAddress(props.getHost(), props.getPort());

		Channel channel = bootstrap.bind(address);
		bindings.put(props, channel);

		logger.info("kraken-rpc: {} port opened", address);
		return channel;
	}

	private void unbind(RpcBindingProperties props) {
		Channel channel = bindings.remove(props);
		if (channel == null)
			return;

		logger.info("kraken-rpc: unbinding [{}]", props);
		channel.unbind();
		channel.close().awaitUninterruptibly();
	}

	@Override
	public RpcConnection findConnection(int id) {
		return handler.findConnection(id);
	}

	@Override
	public Collection<RpcConnection> getConnections() {
		return handler.getConnections();
	}

	@Override
	public RpcPeerRegistry getPeerRegistry() {
		return peerRegistry;
	}

	@Override
	public void addConnectionListener(RpcConnectionEventListener listener) {
		handler.addConnectionListener(listener);
	}

	@Override
	public void removeConnectionListener(RpcConnectionEventListener listener) {
		handler.removeConnectionListener(listener);
	}
}
