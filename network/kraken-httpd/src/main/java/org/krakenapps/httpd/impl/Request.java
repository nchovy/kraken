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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.ssl.SslHandler;
import org.krakenapps.httpd.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class Request implements HttpServletRequest {
	private ChannelHandlerContext ctx;
	private boolean secure;
	private boolean asyncStarted;

	/**
	 * can be null if not found
	 */
	private HttpContext httpContext;

	/**
	 * can be null if not found
	 */
	private String servletPath;

	private String pathInfo;

	private String queryString;

	private HttpRequest req;
	private Response resp;
	private HttpSession session;
	private ServletInputStream is;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
	private Cookie[] cookies;
	private List<Locale> locales = new ArrayList<Locale>();
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	public Request(ChannelHandlerContext ctx, HttpRequest req) {
		this.ctx = ctx;
		this.req = req;
		this.queryString = "";

		ChannelBuffer c = req.getContent();
		this.is = new RequestInputStream(new ByteArrayInputStream(c.array(), 0, c.readableBytes()));

		parseCookies(req);
		parseLocales(req);
		parseParameters();

		setSslAttributes(ctx);
		setChannel(ctx);
	}

	public void setHttpContext(HttpContext httpContext) {
		this.httpContext = httpContext;
		setSession();
	}

	public void setResponse(Response resp) {
		this.resp = resp;
	}

	private void setSession() {
		// TODO: domain check
		String key = getRequestedSessionId();
		if (key != null)
			session = httpContext.getHttpSessions().get(key);
	}

	private void parseParameters() {
		String contentType = req.getHeader("Content-Type");

		if (req.getMethod().equals(HttpMethod.POST)) {
			if (!(contentType != null && contentType.equals("application/octet-stream"))) {
				ChannelBuffer c = req.getContent();
				String body = new String(c.array(), c.readerIndex(), c.readableBytes(), Charset.forName("utf-8"));
				setParams(body);
			}
		}

		if (req.getUri().contains("?")) {
			int p = req.getUri().indexOf("?");
			this.queryString = req.getUri().substring(p + 1);
			setParams(this.queryString);
		}
	}

	private void setSslAttributes(ChannelHandlerContext ctx) {
		SslHandler sslHandler = (SslHandler) ctx.getPipeline().get("ssl");
		this.secure = sslHandler != null;
		if (secure) {
			SSLSession session = sslHandler.getEngine().getSession();
			String cipherSuite = session.getCipherSuite();
			try {
				setAttribute("javax.servlet.request.X509Certificate", session.getPeerCertificateChain());
			} catch (SSLPeerUnverifiedException e) {
			}

			setAttribute("javax.servlet.request.cipher_suite", cipherSuite);
			setAttribute("javax.servlet.request.key_size", deduceKeyLength(cipherSuite));
		}
	}

	private void setChannel(ChannelHandlerContext ctx) {
		// set netty channel (only for internal use)
		setAttribute("netty.channel", ctx.getChannel());
	}

	private void parseLocales(HttpRequest req) {
		List<String> langs = req.getHeaders(HttpHeaders.Names.ACCEPT_LANGUAGE);
		if (langs != null)
			for (String lang : langs)
				locales.add(new Locale(lang));

		// see servlet spec section 3.9
		if (langs.size() == 0)
			locales.add(Locale.getDefault());
	}

	private void parseCookies(HttpRequest req) {
		List<String> cs = req.getHeaders(HttpHeaders.Names.COOKIE);
		ArrayList<Cookie> parsed = new ArrayList<Cookie>();
		this.cookies = new Cookie[cs.size()];
		for (int i = 0; i < cs.size(); i++) {
			String s = cs.get(i);
			if (s == null || s.trim().isEmpty())
				continue;

			String name = null;
			String value = null;
			if (s.contains("=")) {
				String[] split = s.split("=", 2);
				name = split[0].trim();
				value = split[1].trim();
			} else {
				name = s.trim();
			}

			logger.debug("kraken httpd: cookie [{} -> name={}, value={}]", new Object[] { s, name, value });
			parsed.add(new Cookie(name, value));
		}

		this.cookies = new Cookie[parsed.size()];
		parsed.toArray(this.cookies);
	}

	private int deduceKeyLength(String cipherSuite) {
		if (cipherSuite.equals("IDEA_CBC"))
			return 128;
		if (cipherSuite.equals("RC2_CBC_40"))
			return 40;
		if (cipherSuite.equals("RC4_40"))
			return 40;
		if (cipherSuite.equals("RC4_128"))
			return 128;
		if (cipherSuite.equals("DES40_CBC"))
			return 40;
		if (cipherSuite.equals("DES_CBC"))
			return 56;
		if (cipherSuite.equals("3DES_EDE_CBC"))
			return 168;
		return 0;
	}

	private void setParams(String params) {
		if (params == null || params.isEmpty())
			return;

		for (String param : params.split("&")) {
			logger.trace("param: {}", param);
			int pos = param.indexOf("=");
			if (pos > 0) {
				String name = param.substring(0, pos);
				String value = null;
				try {
					String encodedValue = param.substring(pos + 1);
					value = URLDecoder.decode(encodedValue, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}

				if (value.isEmpty())
					value = null;

				List<String> values = new ArrayList<String>();
				if (!parameters.containsKey(name))
					parameters.put(name, values);
				else
					values = parameters.get(name);

				values.add(value);
				logger.trace("kraken webconsole: param name [{}], value [{}]", name, value);
			} else {
				parameters.put(param, null);
				logger.trace("kraken webconsole: param name: {}", param);
			}
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
	public ServletContext getServletContext() {
		return null;
	}

	@Override
	public DispatcherType getDispatcherType() {
		// TODO: implement other dispatcher routines
		return DispatcherType.REQUEST;
	}

	@Override
	public boolean isSecure() {
		return secure;
	}

	// TODO: parse should be deferred for character encoding support
	@Override
	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getAttributeNames() {
		return Collections.enumeration(attributes.keySet());
	}

	@Override
	public void setAttribute(String name, Object o) {
		attributes.put(name, o);
	}

	@Override
	public void removeAttribute(String name) {
		attributes.remove(name);
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
		String[] values = getParameterValues(name);
		if (values == null || values.length == 0)
			return null;
		return values[0];
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> m = new HashMap<String, String[]>();
		for (String key : parameters.keySet())
			m.put(key, (String[]) parameters.get(key).toArray());
		return m;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		List<String> values = parameters.get(name);
		if (values == null)
			return null;

		return values.toArray(new String[0]);
	}

	@Override
	public String getProtocol() {
		return req.getProtocolVersion().getText();
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
	public String getLocalName() {
		return null;
	}

	@Override
	public String getLocalAddr() {
		return ((InetSocketAddress) ctx.getChannel().getLocalAddress()).getAddress().getHostAddress();
	}

	@Override
	public int getLocalPort() {
		return ((InetSocketAddress) ctx.getChannel().getLocalAddress()).getPort();
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
	public int getRemotePort() {
		return ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getPort();
	}

	@Override
	public String getScheme() {
		return ctx.getPipeline().get("ssl") == null ? "http" : "https";
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

	@SuppressWarnings("unchecked")
	@Override
	public Enumeration getHeaderNames() {
		return Collections.enumeration(req.getHeaderNames());
	}

	@Override
	public String getHeader(String name) {
		return req.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return Collections.enumeration(req.getHeaders(name));
	}

	@Override
	public long getDateHeader(String name) {
		try {
			return Long.parseLong(req.getHeader(name));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
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
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getRemoteUser() {
		return null;
	}

	/**
	 * requested path except query string, protocol scheme and domain
	 */
	@Override
	public String getRequestURI() {
		String path = req.getUri();

		// cut protocol scheme and domain
		int p = path.indexOf("://");
		if (p > 0) {
			int p2 = path.indexOf('/', p + 3);
			if (p2 > 0)
				path = path.substring(p2);
			else
				path = "/";
		}

		// cut query string off
		p = path.indexOf('?');
		if (p > 0)
			path = path.substring(0, p);

		return path;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO: support session id from url
		String key = null;
		for (Cookie c : getCookies())
			if (c.getName().equals("JSESSIONID"))
				key = c.getValue();

		return key;
	}

	@Override
	public StringBuffer getRequestURL() {
		String uri = req.getUri();
		int p = uri.indexOf('?');
		if (p < 0)
			return new StringBuffer(uri);
		else
			return new StringBuffer(uri.substring(0, p));
	}

	@Override
	public String getContextPath() {
		if (httpContext != null)
			return httpContext.getServletContext().getContextPath();

		return null;
	}

	@Override
	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
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
		// it must return null if local file system path cannot be determined
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales() {
		return Collections.enumeration(locales);
	}

	@Override
	public Locale getLocale() {
		return locales.get(0);
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (!create)
			return session;

		if (session == null) {
			String key = getRequestedSessionId();
			if (key == null)
				key = UUID.randomUUID().toString();

			session = new HttpSessionImpl(key);
			HttpSession old = httpContext.getHttpSessions().putIfAbsent(key, session);
			if (old != null)
				session = old;
		}

		return session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO: support session key from url
		return getSession(false) != null;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO: will be implemented later
		return false;
	}

	@Deprecated
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		String key = getRequestedSessionId();
		if (key == null)
			return false;

		return httpContext.getHttpSessions().containsKey(key);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext getAsyncContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException {
		asyncStarted = true;
		return new AsyncContextImpl(this, resp);
	}

	@Override
	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAsyncStarted() {
		return asyncStarted;
	}

	@Override
	public boolean isAsyncSupported() {
		return false;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void login(String username, String password) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part getPart(String name) throws IOException, ServletException {
		// TODO Auto-generated method stub
		return null;
	}

}
