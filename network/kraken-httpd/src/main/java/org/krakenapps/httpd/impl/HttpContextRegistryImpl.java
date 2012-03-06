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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;

@Component(name = "http-context-registry")
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
