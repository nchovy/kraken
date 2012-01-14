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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.util.DirectoryMap;

public class ServletRegistryImpl implements ServletRegistry {
	private static final long serialVersionUID = 1L;
	private static final String CONTEXT_ITEM = "/context";

	private DirectoryMap<HttpServlet> directoryMap;

	public ServletRegistryImpl() {
		directoryMap = new DirectoryMap<HttpServlet>();
	}

	@Override
	public Collection<String> getPrefixes() {
		Set<String> prefixes = new HashSet<String>();
		Set<Entry<String, HttpServlet>> entries = directoryMap.entrySet();
		for (Entry<String, HttpServlet> entry : entries) {
			String key = entry.getKey();
			String prefix = key.substring(0, key.length() - CONTEXT_ITEM.length());
			if (prefix.length() == 0)
				prefix = "/";

			prefixes.add(prefix);
		}

		return prefixes;
	}

	@Override
	public HttpServlet getServlet(String prefix) {
		if (prefix.equals("/"))
			prefix = "";

		return directoryMap.get(prefix + CONTEXT_ITEM);
	}

	@Override
	public String getServletPath(String path) {
		String[] tokens = split(path);

		String prefix = null;
		String dir = "";

		// default
		if (directoryMap.containsKey("/context"))
			prefix = dir;

		// TODO: For now, it does not comply with servlet spec. see section 3.5
		// longest match
		for (int i = 0; i < tokens.length; i++) {
			dir += "/" + tokens[i];
			if (directoryMap.containsKey(dir + "/context"))
				prefix = dir;
		}

		return prefix;
	}

	private String[] split(String path) {
		int queryPos = path.lastIndexOf("?");
		if (queryPos > 0)
			path = path.substring(0, queryPos);

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
	public void register(String prefix, HttpServlet ctx) {
		if (prefix.equals("/"))
			prefix = "";

		HttpServlet old = directoryMap.putIfAbsent(prefix + CONTEXT_ITEM, ctx);
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Servlet Registry\n");
		for (String prefix : getPrefixes()) {
			HttpServlet servlet = getServlet(prefix);
			sb.append("  ");
			sb.append(prefix);
			sb.append(" -> ");
			sb.append(servlet.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
