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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.util.DirectoryMap;
import org.krakenapps.webconsole.HttpRequest;
import org.krakenapps.webconsole.StaticResourceApi;
import org.krakenapps.webconsole.StaticResourceContext;

@Component(name = "static-resource-api")
@Provides
public class StaticResourceApiImpl implements StaticResourceApi {
	private static final String CONTEXT_ITEM = "/context";
	private DirectoryMap<StaticResourceContext> directoryMap;

	@Validate
	public void start() {
		directoryMap = new DirectoryMap<StaticResourceContext>();
	}

	@Override
	public Collection<String> getPrefixes() {
		Set<String> prefixes = new HashSet<String>();
		Set<Entry<String, StaticResourceContext>> entries = directoryMap.entrySet();
		for (Entry<String, StaticResourceContext> entry : entries) {
			String key = entry.getKey();
			String prefix = key.substring(0, key.length() - CONTEXT_ITEM.length());
			if (prefix.length() == 0)
				prefix = "/";
			
			prefixes.add(prefix);
		}

		return prefixes;
	}

	@Override
	public StaticResourceContext getContext(String prefix) {
		if (prefix.equals("/"))
			prefix = "";

		return directoryMap.get(prefix + CONTEXT_ITEM);
	}

	@Override
	public InputStream getResource(HttpRequest req) {
		String path = req.getPath();
		String[] tokens = split(path);

		StaticResourceContext ctx = null;
		String selectedDir = null;
		String dir = "/";

		// default
		{
			StaticResourceContext c = directoryMap.get("/context");
			if (c != null) {
				ctx = c;
				selectedDir = dir;
			}
		}

		// longest match
		for (int i = 0; i < tokens.length; i++) {
			dir += tokens[i] + "/";
			StaticResourceContext c = directoryMap.get(dir + "context");
			if (c != null) {
				ctx = c;
				selectedDir = dir;
			}
		}

		if (selectedDir == null)
			return null;

		((WebConsoleHttpRequest) req).setPath(path.replaceFirst(selectedDir, ""));
		return ctx.open(req);
	}

	private String[] split(String path) {
		String[] s = path.split("/");
		int count = 0;
		for (int i = 0; i < s.length; i++)
			if (!s[i].isEmpty())
				count++;

		String[] n = new String[count];
		int j = 0;
		for (int i = 0; i < s.length; i++)
			if (!s[i].isEmpty())
				n[j++] = s[i];

		return n;
	}

	@Override
	public void register(String prefix, StaticResourceContext ctx) {
		if (prefix.equals("/"))
			prefix = "";

		StaticResourceContext old = directoryMap.putIfAbsent(prefix + CONTEXT_ITEM, ctx);
		if (old != null)
			throw new IllegalStateException(prefix + " already exists");
	}

	@Override
	public void unregister(String prefix) {
		if (prefix == null)
			throw new IllegalArgumentException("prefix should be not null");

		if (prefix.equals("/"))
			prefix = "";

		directoryMap.remove(prefix + CONTEXT_ITEM);
	}
}
