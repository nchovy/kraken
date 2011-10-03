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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.krakenapps.util.DirectoryMap;
import org.krakenapps.webconsole.PageNotFoundException;
import org.krakenapps.webconsole.ServletRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-servlet-registry")
@Provides
public class ServletRegistryImpl implements ServletRegistry {
	private static final long serialVersionUID = 1L;
	private static final String CONTEXT_ITEM = "/context";
	private final Logger logger = LoggerFactory.getLogger(ServletRegistryImpl.class);

	private BundleContext bc;
	private DirectoryMap<HttpServlet> directoryMap;

	public ServletRegistryImpl(BundleContext bc) {
		this.bc = bc;
	}

	@Validate
	public void start() {
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
	public HttpServlet getContext(String prefix) {
		if (prefix.equals("/"))
			prefix = "";

		return directoryMap.get(prefix + CONTEXT_ITEM);
	}

	@Override
	public void service(ChannelHandlerContext ctx, HttpRequest req) {
		try {
			if (!serviceInternal(ctx, req, true))
				sendNotFound(ctx, req);
		} catch (ServletException e) {
			logger.error("kraken webconsole: servlet service error.", e);
		} catch (IOException e) {
			logger.error("kraken webconsole: io error.", e);
		}
	}

	private boolean serviceInternal(ChannelHandlerContext ctx, HttpRequest req, boolean service) throws ServletException,
			IOException {
		ServletResponse response = null;

		try {
			String servletPath = findServlet(req.getUri());
			if (servletPath == null)
				throw new IllegalArgumentException("invalid request path");

			HttpServlet servlet = directoryMap.get(servletPath + "/context");

			String pathInfo = req.getUri().substring(servletPath.length());
			HttpServletRequest request = new Request(ctx, req, servletPath, pathInfo);
			response = new Response(bc, ctx, req, service);

			servlet.service(request, response);
		} catch (PageNotFoundException e) {
			if (e.getMovedLocation() != null) {
				HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MOVED_PERMANENTLY);
				resp.setHeader(HttpHeaders.Names.LOCATION, e.getMovedLocation());
				ctx.getChannel().write(resp);
				return true;
			} else {
				sendNotFound(ctx, req);
			}
			return false;
		} catch (FileNotFoundException e) {
			sendNotFound(ctx, req);
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	private void sendNotFound(ChannelHandlerContext ctx, HttpRequest req) {
		HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		ChannelFuture f = ctx.getChannel().write(resp);
		if (!HttpHeaders.isKeepAlive(req) || !resp.getStatus().equals(HttpResponseStatus.OK)) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private String findServlet(String path) {
		String[] tokens = split(path);

		String prefix = null;
		String dir = "";

		// default
		if (directoryMap.containsKey("/context"))
			prefix = dir;

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
}
