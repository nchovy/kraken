package org.krakenapps.webconsole.impl;

import java.io.IOException;
import java.io.PrintWriter;

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
	private String contentType;

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
			HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
			resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
			resp.setContent(buf);

			ChannelFuture f = ctx.getChannel().write(resp);
			if (!HttpHeaders.isKeepAlive(req) || resp.getStatus().getCode() != 200) {
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
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String encodeUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
	}

}
