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
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.webconsole.StaticResourceApi;
import org.krakenapps.webconsole.WebSocketServer;
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
	private StaticResourceApi staticResourceApi;

	@Requires
	private PreferencesService prefsvc;

	private Map<InetSocketAddress, Channel> listeners;
	private BundleContext bc;

	public WebSocketServerImpl(BundleContext bc) {
		this.bc = bc;
		this.listeners = new ConcurrentHashMap<InetSocketAddress, Channel>();
	}

	@Override
	public Collection<InetSocketAddress> getBindings() {
		return listeners.keySet();
	}

	@Override
	public void open(InetSocketAddress address) {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(msgbus, staticResourceApi));

		// Bind and start to accept incoming connections.
		Channel listener = bootstrap.bind(address);
		listeners.put(address, listener);

		setBindAddressConfig(address);
		logger.info("kraken webconsole: {} opened", address);
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
		staticResourceApi.register("/", new BundleResourceContext(bc.getBundle(), "/WEB-INF"));
		for (InetSocketAddress addr : getBindAddressConfig()) {
			open(addr);
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

	private List<InetSocketAddress> getBindAddressConfig() {
		List<InetSocketAddress> bindings = new ArrayList<InetSocketAddress>();
		try {
			Preferences p = getConfig();
			for (String name : p.childrenNames()) {
				logger.trace("kraken webconsole: binding [{}] found", name);

				String[] tokens = name.split(":");
				if (tokens.length == 1) {
					bindings.add(new InetSocketAddress(Integer.valueOf(tokens[0])));
				} else if (tokens.length == 2) {
					bindings.add(new InetSocketAddress(tokens[0], Integer.valueOf(tokens[1])));
				}
			}
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: io error", e);
		}
		return bindings;

	}

	private void setBindAddressConfig(InetSocketAddress address) {
		try {
			Preferences p = getConfig();
			String name = address.getAddress().getHostAddress() + ":" + address.getPort();
			p.node(name);

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: cannot save bind address", e);
		}
	}
	
	private void unsetBindAddressConfig(InetSocketAddress address) {
		try {
			Preferences p = getConfig();
			String name = address.getAddress().getHostAddress() + ":" + address.getPort();
			p.node(name).removeNode();

			p.flush();
			p.sync();
		} catch (BackingStoreException e) {
			logger.error("kraken webconsole: cannot remove bind address", e);
		}
	}
}
