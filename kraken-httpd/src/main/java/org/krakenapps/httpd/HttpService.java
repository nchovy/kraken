package org.krakenapps.httpd;

import java.net.InetSocketAddress;
import java.util.Collection;

import org.krakenapps.servlet.api.ServletRegistry;

public interface HttpService {
	HttpContextRegistry getContextRegistry();

	HttpContext ensureContext(String name);

	HttpContext findContext(String name);

	ServletRegistry findServletRegistry(int port);

	ServletRegistry findServletRegistry(String domain, int port);

	Collection<InetSocketAddress> getListenAddresses();

	HttpServer getServer(InetSocketAddress listen);

	HttpServer createServer(HttpConfiguration config);

	void removeServer(InetSocketAddress listen);
}
