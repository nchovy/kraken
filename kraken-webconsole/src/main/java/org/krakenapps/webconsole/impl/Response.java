package org.krakenapps.webconsole.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class Response implements HttpServletResponse {
	private ChannelHandlerContext ctx;
	private HttpRequest req;
	private ServletOutputStream os;
	private HttpResponseStatus status = HttpResponseStatus.OK;
	private Map<String, Object> header = new HashMap<String, Object>();
	private Set<Cookie> cookies = new HashSet<Cookie>();

	public Response(ChannelHandlerContext ctx, HttpRequest req) {
		this.ctx = ctx;
		this.req = req;
		this.os = new ResponseOutputStream();
	}

	private class ResponseOutputStream extends ServletOutputStream {
		private ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		@Override
		public void write(int b) throws IOException {
			buf.writeByte(b);
		}

		@Override
		public void flush() throws IOException {
			HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

			String cookie = null;
			for (Cookie c : cookies) {
				if (cookie == null)
					cookie = String.format("%s=%s", c.getName(), c.getValue());
				else
					cookie = String.format("%s; %s=%s", cookie, c.getName(), c.getValue());
			}
			if (cookie != null)
				header.put(HttpHeaders.Names.COOKIE, cookie);

			for (String name : header.keySet())
				resp.setHeader(name, header.get(name));
			resp.setContent(buf);

			ChannelFuture f = ctx.getChannel().write(resp);
			if (!HttpHeaders.isKeepAlive(req) || !resp.getStatus().equals(HttpResponseStatus.OK)) {
				f.addListener(ChannelFutureListener.CLOSE);
			}
		}

	}

	@Override
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(os);
	}

	@Override
	public void setContentLength(int len) {
		header.put(HttpHeaders.Names.CONTENT_LENGTH, len);
	}

	@Override
	public void setContentType(String type) {
		header.put(HttpHeaders.Names.CONTENT_TYPE, type);
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return header.containsKey(name);
	}

	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public String encodeRedirectUrl(String url) {
		return encodeRedirectURL(url);
	}

	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public String encodeUrl(String url) {
		return encodeURL(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendError(int sc) throws IOException {
		sendError(sc, null);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDateHeader(String name, long date) {
		header.put(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		header.put(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		header.put(name, value);
	}

	@Deprecated
	@Override
	public void setStatus(int sc, String sm) {
		setStatus(sc);
	}

	@Override
	public void setStatus(int sc) {
		this.status = HttpResponseStatus.valueOf(sc);
	}

}
