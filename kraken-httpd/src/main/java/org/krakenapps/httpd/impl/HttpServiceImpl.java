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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpServer;
import org.krakenapps.httpd.HttpService;
import org.osgi.framework.BundleContext;

@Component(name = "http-service")
@Provides
public class HttpServiceImpl implements HttpService {

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

	/*
	 * public ServletRegistry findServletRegistry(int port) { HttpServer server
	 * = getServer(new InetSocketAddress(port)); List<VirtualHost> hosts =
	 * server.getConfiguration().getVirtualHosts(); String contextName =
	 * hosts.get(0).getHttpContextName(); HttpContext ctx =
	 * httpContextRegistry.findContext(contextName); if (ctx == null) throw new
	 * IllegalStateException("cannot find context: " + contextName);
	 * 
	 * return ctx.getServletRegistry(); }
	 */
	/*
	 * @Override public ServletRegistry findServletRegistry(String domain, int
	 * port) { HttpServer server = getServer(new InetSocketAddress(port)); for
	 * (VirtualHost host : server.getConfiguration().getVirtualHosts()) { if
	 * (host.matches(domain)) { String contextName = host.getHttpContextName();
	 * HttpContext ctx = httpContextRegistry.findContext(contextName); if (ctx
	 * == null) throw new IllegalStateException("cannot find context: " +
	 * contextName);
	 * 
	 * return ctx.getServletRegistry(); } } return null; }
	 */
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
		HttpServer server = new HttpServerImpl(bc, config, httpContextRegistry, keyStoreManager);
		HttpServer old = listeners.putIfAbsent(config.getListenAddress(), server);
		if (old != null)
			throw new IllegalStateException("server already exists: " + config.getListenAddress());

		return server;
	}

	@Override
	public void removeServer(InetSocketAddress listen) {
		HttpServer server = listeners.remove(listen);
		if (server != null)
			server.close();
	}

}
