/*
 * Copyright 2012 Future Systems
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.httpd.VirtualHost;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "http-service")
@Provides
public class HttpServiceImpl implements HttpService {
	private final Logger logger = LoggerFactory.getLogger(HttpServiceImpl.class.getName());

	@Requires
	private ConfigService conf;

	@Requires
	private KeyStoreManager keyStoreManager;

	private BundleContext bc;

	private HttpContextRegistry httpContextRegistry;

	private ConcurrentMap<InetSocketAddress, HttpServer> listeners;

	public HttpServiceImpl(BundleContext bc) {
		this.bc = bc;
		this.listeners = new ConcurrentHashMap<InetSocketAddress, HttpServer>();
		this.httpContextRegistry = new HttpContextRegistryImpl();
	}

	@Validate
	public void start() {
		// load configured servers
		ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
		Collection<HttpConfiguration> configs = db.findAll(HttpConfiguration.class).getDocuments(HttpConfiguration.class);
		for (HttpConfiguration c : configs) {
			try {
				logger.info("kraken httpd: opening http server [{}]", PrimitiveConverter.serialize(c));
				HttpServer server = loadServer(c);
				server.open();

				// build regex patterns
				HttpConfiguration sc = server.getConfiguration();
				for (VirtualHost v : sc.getVirtualHosts())
					v.setHostNames(v.getHostNames());

				logger.trace("kraken httpd: opened http server [{}]", server.getConfiguration().getListenAddress());
			} catch (Throwable t) {
				logger.error("kraken httpd: cannot open server", t);
			}
		}
	}

	@Invalidate
	public void stop() {
		for (HttpServer server : listeners.values())
			server.close();
	}

	@Override
	public HttpContextRegistry getContextRegistry() {
		return httpContextRegistry;
	}

	@Override
	public HttpContext ensureContext(String name) {
		return httpContextRegistry.ensureContext(name);
	}

	@Override
	public HttpContext findContext(String name) {
		return httpContextRegistry.findContext(name);
	}

	@Override
	public Collection<InetSocketAddress> getListenAddresses() {
		return listeners.keySet();
	}

	@Override
	public HttpServer getServer(InetSocketAddress listen) {
		return listeners.get(listen);
	}

	@Override
	public HttpServer createServer(HttpConfiguration config) {
		HttpServer server = loadServer(config);
		ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
		db.add(config, "kraken-httpd", "opened server " + config.getListenAddress());

		return server;
	}

	private HttpServer loadServer(HttpConfiguration config) {
		HttpServer server = new HttpServerImpl(bc, config, httpContextRegistry, keyStoreManager, conf);
		HttpServer old = listeners.putIfAbsent(config.getListenAddress(), server);
		if (old != null)
			throw new IllegalStateException("server already exists: " + config.getListenAddress());
		return server;
	}

	@Override
	public void removeServer(InetSocketAddress listen) {
		HttpServer server = listeners.remove(listen);
		if (server != null) {
			server.close();

			ConfigDatabase db = conf.ensureDatabase("kraken-httpd");
			Map<String, Object> filter = new HashMap<String, Object>();
			filter.put("listen_address", listen.getAddress().getHostAddress());
			filter.put("listen_port", listen.getPort());
			Config c = db.findOne(HttpConfiguration.class, Predicates.field(filter));
			if (c != null)
				db.remove(c, false, "kraken-httpd", "removed server " + listen);
		}
	}
}
