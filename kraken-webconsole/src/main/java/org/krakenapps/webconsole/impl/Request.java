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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

@SuppressWarnings("rawtypes")
public class Request implements HttpServletRequest {
	private ChannelHandlerContext ctx;
	private HttpRequest req;
	private String servletPath;
	private String pathInfo;
	private String queryString;
	private ServletInputStream is;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, String> parameters = new HashMap<String, String>();
	private Cookie[] cookies;

	public Request(ChannelHandlerContext ctx, HttpRequest req, String servletPath, String pathInfo) {
		this.ctx = ctx;
		this.req = req;
		this.servletPath = servletPath;
		this.pathInfo = pathInfo;
		this.queryString = "";

		ChannelBuffer content = req.getContent();

		if (req.getMethod().equals(HttpMethod.POST)) {
			String body = new String(content.array(), content.readerIndex(), content.readableBytes(), Charset.forName("utf-8"));
			setParams(body);
		}

		if (pathInfo.contains("?")) {
			this.queryString = pathInfo.substring(pathInfo.indexOf("?") + 1);
			this.pathInfo = pathInfo.substring(0, pathInfo.indexOf("?"));
			String params = pathInfo.substring(pathInfo.indexOf("?") + 1);
			setParams(params);
		}

		this.is = new RequestInputStream(new ByteArrayInputStream(content.array(), 0, content.readableBytes()));
		List<String> cs = req.getHeaders(HttpHeaders.Names.COOKIE);
		this.cookies = new Cookie[cs.size()];
		for (int i = 0; i < cs.size(); i++) {
			String s = cs.get(i);
			String name = null;
			String value = null;
			if (s.contains("=")) {
				String[] split = s.split("=", 2);
				name = split[0].trim();
				value = split[1].trim();
			} else {
				name = s.trim();
			}
			this.cookies[i] = new Cookie(name, value);
		}
	}

	private void setParams(String params) {
		if (params == null || params.isEmpty())
			return;

		for (String param : params.split("&")) {
			String name = param.substring(0, param.indexOf("="));
			String value = param.substring(param.indexOf("=") + 1);
			parameters.put(name, value);
		}
	}

	private class RequestInputStream extends ServletInputStream {
		private InputStream is;

		public RequestInputStream(InputStream is) {
			this.is = is;
		}

		@Override
		public int read() throws IOException {
			return is.read();
		}
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public String getCharacterEncoding() {
		String contentType = getContentType();
		if (contentType == null || !contentType.contains("charset"))
			return null;
		for (String t : contentType.split(";")) {
			if (t.trim().startsWith("charset"))
				return t.split("=")[1].trim();
		}
		return null;
	}

	@Override
	public int getContentLength() {
		return (int) HttpHeaders.getContentLength(req);
	}

	@Override
	public String getContentType() {
		return req.getHeader(HttpHeaders.Names.CONTENT_TYPE);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return is;
	}

	@Override
	public String getParameter(String name) {
		return parameters.get(name);
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return (String[]) parameters.values().toArray();
	}

	@Override
	public String getProtocol() {
		return "HTTP/1.1";
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(is));
	}

	@Deprecated
	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getHostName();
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		String host = req.getHeader("Host");
		if (host == null)
			return null;

		if (host.contains(":"))
			return host.substring(0, host.indexOf(":"));
		else
			return host;
	}

	@Override
	public int getServerPort() {
		String host = req.getHeader("Host");
		if (host == null || !host.contains(":"))
			return 80;
		return Integer.parseInt(host.substring(host.indexOf(":") + 1));
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public String getAuthType() {
		String auth = req.getHeader(HttpHeaders.Names.AUTHORIZATION);
		if (auth == null)
			return null;
		return auth.substring(0, auth.indexOf(" "));
	}

	@Override
	public Cookie[] getCookies() {
		return cookies;
	}

	@Override
	public long getDateHeader(String name) {
		try {
			long value = Long.parseLong(req.getHeader(name));
			return value;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public Enumeration getHeaderNames() {
		return Collections.enumeration(req.getHeaderNames());
	}

	@Override
	public int getIntHeader(String name) {
		return HttpHeaders.getIntHeader(req, name);
	}

	@Override
	public String getMethod() {
		return req.getMethod().getName();
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		return servletPath + pathInfo;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession(boolean create) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	@Deprecated
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

}
