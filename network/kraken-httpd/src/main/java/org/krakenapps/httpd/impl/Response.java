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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Response implements HttpServletResponse {
	private final Logger logger = LoggerFactory.getLogger(Response.class.getName());
	private final int bufferSize = 128 * 1024;

	private BundleContext bc;
	private ChannelHandlerContext ctx;
	private HttpServletRequest req;
	private ServletOutputStream os;
	private PrintWriter writer;
	private HttpResponseStatus status = HttpResponseStatus.OK;
	private Map<String, List<String>> headers = new HashMap<String, List<String>>();
	private Set<Cookie> cookies = new HashSet<Cookie>();

	public Response(BundleContext bc, ChannelHandlerContext ctx, HttpServletRequest req) {
		this.bc = bc;
		this.ctx = ctx;
		this.req = req;
		this.os = new ResponseOutputStream();
		this.writer = new PrintWriter(new OutputStreamWriter(os, Charset.forName("utf-8")));
	}

	private class ResponseOutputStream extends ServletOutputStream {
		private boolean closed = false;
		private boolean sentHeader = false;
		private ChannelBuffer buf = ChannelBuffers.dynamicBuffer();

		@Override
		public void write(int b) throws IOException {
			buf.writeByte(b);
			if (buf.readableBytes() > bufferSize)
				flush();
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			buf.writeBytes(b, off, len);
			if (buf.readableBytes() > bufferSize)
				flush();
		}

		@Override
		public void close() throws IOException {
			if (closed) {
				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: response output closed");
				return;
			}

			closed = true;

			flush(true);

			String transferEncoding = getHeader(HttpHeaders.Names.TRANSFER_ENCODING);
			if (logger.isDebugEnabled())
				logger.debug("kraken httpd: transfer encoding header [{}]", transferEncoding);

			if (transferEncoding != null && transferEncoding.equals("chunked")) {
				ctx.getChannel().write(new DefaultHttpChunk(ChannelBuffers.EMPTY_BUFFER));
				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: channel [{}], last empty chunk", ctx.getChannel());
			}

			if (logger.isDebugEnabled())
				logger.debug("kraken httpd: closing channel [{}]", ctx.getChannel());

			if (!isKeepAlive()) {
				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: channel [{}] will be closed", ctx.getChannel());
				ctx.getChannel().close();
			}
		}

		@Override
		public void flush() throws IOException {
			flush(false);
		}

		private void flush(boolean force) {
			String transferEncoding = getHeader(HttpHeaders.Names.TRANSFER_ENCODING);
			boolean isChunked = transferEncoding != null && transferEncoding.equals("chunked");

			if ((force || isChunked) && !sentHeader) {
				// send response if not sent
				HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

				HttpSessionImpl session = (HttpSessionImpl) req.getSession(false);
				if (session != null) {
					if (session.isNew()) {
						resp.addHeader(HttpHeaders.Names.SET_COOKIE, "JSESSIONID=" + session.getId() + "; path=/");
						session.setNew(false);
					}

					session.setLastAccess(new Date());
				}

				if (!isChunked)
					resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());

				for (Cookie c : cookies) {
					resp.addHeader(HttpHeaders.Names.SET_COOKIE, c.getName() + "=" + c.getValue());
				}

				for (String name : headers.keySet())
					resp.setHeader(name, headers.get(name));

				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: channel [{}], sent header", ctx.getChannel());

				// write http header
				ctx.getChannel().write(resp);

				sentHeader = true;
			}

			if (isChunked) {
				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: channel [{}], flush chunk [{}]", ctx.getChannel(), buf.readableBytes());

				ctx.getChannel().write(new DefaultHttpChunk(buf));
				buf = ChannelBuffers.dynamicBuffer();
			} else if (sentHeader) {
				if (logger.isDebugEnabled())
					logger.debug("kraken httpd: channel [{}], flush response [{}]", ctx.getChannel(), buf.readableBytes());

				ctx.getChannel().write(buf);
				buf = ChannelBuffers.dynamicBuffer();
			}
		}

		private boolean isKeepAlive() {
			String connection = req.getHeader(Names.CONNECTION);
			if (connection != null && Values.CLOSE.equalsIgnoreCase(connection))
				return false;

			if (req.getProtocol().equals("HTTP/1.1")) {
				return !Values.CLOSE.equalsIgnoreCase(connection);
			} else {
				return Values.KEEP_ALIVE.equalsIgnoreCase(connection);
			}
		}

	}

	@Override
	public String getCharacterEncoding() {
		List<String> contentTypes = (List<String>) headers.get(HttpHeaders.Names.CONTENT_TYPE);
		if (contentTypes == null)
			return null;

		String contentType = contentTypes.get(0);
		if (!contentType.contains("charset"))
			return null;

		for (String t : contentType.split(";")) {
			if (t.trim().startsWith("charset"))
				return t.split("=")[1].trim();
		}
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public void setContentLength(int len) {
		headers.put(HttpHeaders.Names.CONTENT_LENGTH, Arrays.asList(Integer.toString(len)));
	}

	@Override
	public void setContentType(String type) {
		headers.put(HttpHeaders.Names.CONTENT_TYPE, Arrays.asList(type));
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.containsKey(name);
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
		setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		this.status = HttpResponseStatus.valueOf(sc);
		if (msg == null)
			msg = "";

		String body = "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n" //
				+ "<html><head><title>" + sc + " " + status.getReasonPhrase()
				+ "</title></head>\n" //
				+ "<body><h1>" + sc + " " + status.getReasonPhrase() + "</h1><pre>" + msg
				+ "</pre><hr/><address>Kraken HTTPd/"
				+ bc.getBundle().getHeaders().get(Constants.BUNDLE_VERSION) + "</address></body></html>";

		writer.append(body);
		writer.close();
	}

	@Override
	public void sendError(int sc) throws IOException {
		sendError(sc, null);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		this.status = HttpResponseStatus.MOVED_PERMANENTLY;
		setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
		setHeader(HttpHeaders.Names.LOCATION, location);
	}

	public void close() {
		writer.close();
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBufferSize(int size) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLocale(Locale loc) {
		// TODO Auto-generated method stub

	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeader(String name) {
		List<String> l = headers.get(name);
		if (l == null || l.size() == 0)
			return null;

		return l.get(0);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return headers.get(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public void addDateHeader(String name, long date) {
		addHeader(name, new Date(date).toString());
	}

	@Override
	public void addIntHeader(String name, int value) {
		addHeader(name, Integer.toString(value));
	}

	@Override
	public void addHeader(String name, String value) {
		List<String> l = headers.get(name);
		if (l == null) {
			l = new ArrayList<String>();
			headers.put(name, l);
		}

		l.add(value);
	}

	@Override
	public int getStatus() {
		return status.getCode();
	}

	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, new Date(date).toString());
	}

	@Override
	public void setIntHeader(String name, int value) {
		setHeader(name, Integer.toString(value));
	}

	@Override
	public void setHeader(String name, String value) {
		if (logger.isDebugEnabled())
			logger.debug("kraken httpd: set response header [name: {}, value: {}]", name, value);
		headers.put(name, Arrays.asList(value));
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
