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
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.httpd.HttpConfiguration;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "http-server")
@Provides
public class HttpServerImpl implements HttpServer {
	private final Logger logger = LoggerFactory.getLogger(HttpServerImpl.class.getName());

	private BundleContext bc;
	private HttpConfiguration config;
	private HttpContextRegistry contextRegistry;
	private KeyStoreManager keyStoreManager;
	private Channel listener;

	public HttpServerImpl(BundleContext bc, HttpConfiguration config, HttpContextRegistry contextRegistry,
			KeyStoreManager keyStoreManager) {
		this.bc = bc;
		this.config = config;
		this.contextRegistry = contextRegistry;
		this.keyStoreManager = keyStoreManager;
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
	public void close() {
		try {
			if (listener != null) {
				logger.info("kraken httpd: {} closed", listener.getLocalAddress());
				listener.unbind();
			}
		} catch (Throwable t) {
			logger.error("kraken httpd: cannot close " + listener.getLocalAddress(), t);
		}
	}
}
