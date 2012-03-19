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
package org.krakenapps.httpd.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.httpd.HttpConfigurationListener;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.VirtualHost;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "http-server")
@Provides
public class HttpServerImpl implements HttpServer, HttpConfigurationListener {
	private final Logger logger = LoggerFactory.getLogger(HttpServerImpl.class.getName());

	private BundleContext bc;
	private HttpConfiguration config;
	private HttpContextRegistry contextRegistry;
	private KeyStoreManager keyStoreManager;
	private Channel listener;
	private ConfigService conf;

	public HttpServerImpl(BundleContext bc, HttpConfiguration config, HttpContextRegistry contextRegistry,
			KeyStoreManager keyStoreManager, ConfigService conf) {
		this.bc = bc;
		this.config = config;
		this.contextRegistry = contextRegistry;
		this.keyStoreManager = keyStoreManager;
		this.conf = conf;

		// set configuration set listener
		config.getListeners().add(this);
	}

	@Override
	public void open() {
		// Configure the server.
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new HttpPipelineFactory(bc, config, contextRegistry, keyStoreManager));

		// Bind and start to accept incoming connections.
		InetSocketAddress addr = config.getListenAddress();
		listener = bootstrap.bind(addr);

		logger.info("kraken httpd: {} ({}) opened", addr, config.isSsl() ? "https" : "http");
	}

	@Override
	public HttpConfiguration getConfiguration() {
		return config;
	}

	@Override
	public void addVirtualHost(VirtualHost vhost) {
		if (vhost.getHttpContextName() == null)
			throw new IllegalArgumentException("kraken httpd: http context name shoud not null");

		VirtualHost target = findVirtualHost(vhost.getHttpContextName());
		if (target != null)
			throw new IllegalStateException("duplicated http context exists: " + vhost.getHttpContextName());

		config.getVirtualHosts().add(vhost);
		saveConfig();
	}

	@Override
	public void removeVirtualHost(String httpContextName) {
		VirtualHost target = findVirtualHost(httpContextName);
		if (target != null) {
			config.getVirtualHosts().remove(target);
			saveConfig();
		}
	}

	private void saveConfig() {
		Map<String, Object> filter = getFilter();

		ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
		Config c = db.findOne(HttpConfiguration.class, Predicates.field(filter));
		if (c != null) {
			db.update(c, this.config);
		} else {
			logger.error("kraken httpd: cannot find configuration for " + config.getListenAddress());
		}
	}

	private Map<String, Object> getFilter() {
		Map<String, Object> filter = new HashMap<String, Object>();
		InetSocketAddress listen = config.getListenAddress();
		filter.put("listen_address", listen.getAddress().getHostAddress());
		filter.put("listen_port", listen.getPort());
		return filter;
	}

	private VirtualHost findVirtualHost(String httpContextName) {
		VirtualHost target = null;

		for (VirtualHost h : config.getVirtualHosts())
			if (h.getHttpContextName().equals(httpContextName))
				target = h;
		return target;
	}

	@Override
	public void onSet(String field, Object value) {
		saveConfig();
	}

	@Override
	public void close() {
		// remove reference
		config.getListeners().remove(this);

		try {
			if (listener != null) {
				logger.info("kraken httpd: {} closed", listener.getLocalAddress());
				listener.unbind();
			}
		} catch (Throwable t) {
			logger.error("kraken httpd: cannot close " + listener.getLocalAddress(), t);
		}
	}

	@Override
	public boolean isOpened() {
		// TODO Auto-generated method stub
		return  listener == null ? false : listener.isOpen();
	}
}
