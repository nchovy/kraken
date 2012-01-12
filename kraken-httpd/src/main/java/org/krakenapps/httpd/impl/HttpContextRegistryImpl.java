package org.krakenapps.httpd.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;

public class HttpContextRegistryImpl implements HttpContextRegistry {

	private ConcurrentMap<String, HttpContext> contexts;

	public HttpContextRegistryImpl() {
		contexts = new ConcurrentHashMap<String, HttpContext>();
	}

	@Override
	public Collection<String> getContextNames() {
		return new ArrayList<String>(contexts.keySet());
	}

	@Override
	public HttpContext ensureContext(String name) {
		HttpContext ctx = new HttpContext(name);
		HttpContext old = contexts.putIfAbsent(name, ctx);
		if (old != null)
			return old;
		return ctx;
	}

	@Override
	public HttpContext findContext(String name) {
		return contexts.get(name);
	}

	@Override
	public void removeContext(String name) {
		contexts.remove(name);
	}
}
