/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.webconsole.impl;

import java.net.InetSocketAddress;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.webconsole.BundleResourceServlet;
import org.krakenapps.webconsole.ServletRegistry;
import org.krakenapps.webconsole.WebSocketServer;
import org.krakenapps.webconsole.WebSocketServerParams;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-server")
@Provides
public class WebSocketServerImpl implements WebSocketServer {
	private final Logger logger = LoggerFactory.getLogger(WebSocketServerImpl.class.getName());

	private volatile boolean isClosing = false;

	@Requires
	private MessageBus msgbus;

	@Requires
	private ServletRegistry staticResourceApi;

	@Requires
	private PreferencesService prefsvc;

	@Requires
	private KeyStoreManager keyStoreManager;

	private Map<InetSocketAddress, Channel> listeners;
	private BundleContext bc;

	public WebSocketServerImpl(BundleContext bc) {
		this.bc = bc;
		this.listeners = new ConcurrentHashMap<InetSocketAddress, Channel>();
	}

	@Override
	public Collection<InetSocketAddress> getListenAddresses() {
		return listeners.keySet();
	}

	@Override
	public void open(WebSocketServerParams params) {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(msgbus, staticResourceApi, params));

		// Bind and start to accept incoming connections.
		InetSocketAddress addr = params.getListenAddress();
		Channel listener = bootstrap.bind(addr);
		listeners.put(addr, listener);

		setBindAddressConfig(params);
		logger.info("kraken webconsole: {} ({}) opened", addr, params.isSsl() ? "https" : "http");
	}

	@Override
	public WebSocketServerParams getParameters(InetSocketAddress listen) {
		try {
			return getWebSocketServerParams(getContextKey(listen));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close(InetSocketAddress address) {
		Channel listener = listeners.get(address);
		if (listener != null) {
			InetSocketAddress addr = (InetSocketAddress) listener.getLocalAddress();
			listener.unbind();

			logger.info("kraken webconsole: {} closed", addr);
			listeners.remove(address);

			if (!isClosing)
				unsetBindAddressConfig(address);
		}
	}

	@Override
	public void closeAll() {
		for (InetSocketAddress address : listeners.keySet()) {
			try {
				close(address);
			} catch (Exception e) {
				logger.warn("kraken webconsole: cannot close channel " + address, e);
			}
		}
	}

	@Validate
	public void start() {
		try {
			staticResourceApi.register("/", new BundleResourceServlet(bc.getBundle(), "/WEB-INF"));
		} catch (Exception e) {
			logger.error("kraken webconsole: prefix register failed", e);
		}
		for (WebSocketServerParams params : getBindAddressConfig()) {
			try {
				open(params);
			} catch (Exception e) {
				logger.error("kraken webconsole: cannot open server", e);
			}
		}
	}

	@Invalidate
	public void stop() {
		isClosing = true;
		closeAll();
		logger.info("kraken webconsole: server closed");
		isClosing = false;
	}

	private Preferences getConfig() {
		return prefsvc.getSystemPreferences().node("bindings");
	}

	private List<WebSocketServerParams> getBindAddressConfig() {
		List<WebSocketServerParams> contexts = new ArrayList<WebSocketServerParams>();
		try {
			Preferences p = getConfig();
			for (String name : p.childrenNames()) {
				logger.trace("kraken webconsole: listen addr [{}] found", name);

				WebSocketServerParams ctx = getWebSocketServerParams(name);
				contexts.add(ctx);
			}
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: io error", e);
		} catch (Exception e) {
			logger.error("kraken webconsole: cannot load server config", e);
		}
		return contexts;

	}

	private WebSocketServerParams getWebSocketServerParams(String binding) throws KeyStoreException,
			NoSuchAlgorithmException, UnrecoverableKeyException {
		Preferences p = getConfig();
		Preferences props = p.node(binding);

		String[] tokens = binding.split(":");
		InetSocketAddress listenAddress = null;
		if (tokens.length == 1) {
			listenAddress = new InetSocketAddress(Integer.valueOf(tokens[0]));
		} else if (tokens.length == 2) {
			listenAddress = new InetSocketAddress(tokens[0], Integer.valueOf(tokens[1]));
		}

		boolean isSsl = props.getBoolean("https", false);
		String keyAlias = props.get("key_alias", null);
		String trustAlias = props.get("trust_alias", null);
		WebSocketServerParams ctx = null;

		if (isSsl)
			ctx = new WebSocketServerParams(listenAddress, keyStoreManager, keyAlias, trustAlias);
		else
			ctx = new WebSocketServerParams(listenAddress);
		return ctx;
	}

	private void setBindAddressConfig(WebSocketServerParams ctx) {
		try {
			Preferences bindings = getConfig();
			String name = getContextKey(ctx.getListenAddress());
			Preferences props = bindings.node(name);
			props.putBoolean("https", ctx.isSsl());

			if (ctx != null && ctx.isSsl()) {
				props.put("key_alias", ctx.getKeyAlias());
				props.put("trust_alias", ctx.getTrustAlias());
			}

			bindings.flush();
			bindings.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: cannot save bind address", e);
		}
	}

	private void unsetBindAddressConfig(InetSocketAddress address) {
		try {
			Preferences p = getConfig();
			String name = getContextKey(address);
			p.node(name).removeNode();

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: cannot remove bind address", e);
		}
	}

	private String getContextKey(InetSocketAddress address) {
		String name = address.getAddress().getHostAddress() + ":" + address.getPort();
		return name;
	}
}
