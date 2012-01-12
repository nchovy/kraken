package org.krakenapps.httpd;

import org.krakenapps.httpd.impl.ServletRegistryImpl;
import org.krakenapps.servlet.api.ServletRegistry;

public class HttpContext {
	private String name;
	private ServletRegistry servletRegistry;
	private WebSocketManager webSocketManager;

	public HttpContext(String name) {
		this.name = name;
		this.servletRegistry = new ServletRegistryImpl();
		this.webSocketManager = new WebSocketManager();
	}

	public String getName() {
		return name;
	}

	public ServletRegistry getServletRegistry() {
		return servletRegistry;
	}

	public WebSocketManager getWebSocketManager() {
		return webSocketManager;
	}

	@Override
	public String toString() {
		return "[" + name + "]\n" + servletRegistry + webSocketManager;
	}
}
