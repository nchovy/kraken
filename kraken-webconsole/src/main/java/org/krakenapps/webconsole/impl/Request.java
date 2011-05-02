package org.krakenapps.webconsole.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

@SuppressWarnings("rawtypes")
public class Request implements HttpServletRequest {
	private ChannelHandlerContext ctx;
	private HttpRequest req;
	private String uri;
	private String queryString;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Map<String, String> parameters = new HashMap<String, String>();

	public Request(ChannelHandlerContext ctx, HttpRequest req, String uri) {
		this.ctx = ctx;
		this.req = req;
		this.uri = uri;
		this.queryString = "";
		if (uri.contains("?")) {
			this.queryString = uri.substring(uri.indexOf("?") + 1);
			this.uri = uri.substring(0, uri.indexOf("?"));
			String params = uri.substring(uri.indexOf("?") + 1);
			for (String param : params.split("&")) {
				String name = param.substring(0, param.indexOf("="));
				String value = param.substring(param.indexOf("=") + 1);
				parameters.put(name, value);
			}
		}
		System.out.println(req.getHeaders());
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getContentLength() {
		return (int) HttpHeaders.getContentLength(req);
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public String getRealPath(String path) {
		return null;
	}

	@Override
	public String getRemoteAddr() {
		return ((InetSocketAddress) ctx.getChannel().getRemoteAddress()).getAddress().toString();
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String name) {
		// TODO Auto-generated method stub
		return -1;
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
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public String getMethod() {
		return req.getMethod().getName();
	}

	@Override
	public String getPathInfo() {
		return req.getUri();
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
		return uri;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
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
