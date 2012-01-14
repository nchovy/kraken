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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.VirtualHost;
import org.krakenapps.httpd.WebSocketFrame;
import org.krakenapps.httpd.WebSocketManager;
import org.krakenapps.servlet.api.ServletRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class.getName());

	private BundleContext bc;
	private HttpConfiguration config;
	private HttpContextRegistry contextRegistry;

	public HttpServerHandler(BundleContext bc, HttpConfiguration config, HttpContextRegistry contextRegistry) {
		this.bc = bc;
		this.config = config;
		this.contextRegistry = contextRegistry;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		Channel channel = ctx.getChannel();
		ctx.setAttachment(new HttpSessionImpl(channel.getId().toString(), channel));
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;
			HttpContext httpContext = findHttpContext(req.getHeader(HttpHeaders.Names.HOST));
			if (httpContext == null) {
				Response r = new Response(bc, ctx, req);
				r.sendError(404);
				return;
			}

			service(ctx, req, httpContext);
		} else if (msg instanceof WebSocketFrame) {
			WebSocketFrame frame = (WebSocketFrame) msg;
			HttpContext httpContext = findHttpContext(frame.getHost());
			httpContext.getWebSocketManager().dispatch(frame);
		}
	}

	private HttpContext findHttpContext(String host) {
		for (VirtualHost v : config.getVirtualHosts())
			if (v.matches(host))
				return contextRegistry.findContext(v.getHttpContextName());
		return null;
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// At this moment, channel is already closed
		// Do NOT call ctx.getChannel().close() again
		for (String name : contextRegistry.getContextNames()) {
			HttpContext context = contextRegistry.findContext(name);
			InetSocketAddress remote = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
			context.getWebSocketManager().unregister(remote);
		}
	}

	private void service(ChannelHandlerContext ctx, HttpRequest req, HttpContext httpContext) throws IOException {
		HttpServlet servlet = null;
		Response response = null;
		String pathInfo = null;

		WebSocketManager webSocketManager = httpContext.getWebSocketManager();
		ServletRegistry servletRegistry = httpContext.getServletRegistry();
		response = new Response(bc, ctx, req);

		try {
			String servletPath = servletRegistry.getServletPath(req.getUri());
			if (servletPath != null) {
				servlet = servletRegistry.getServlet(servletPath);
				pathInfo = req.getUri().substring(servletPath.length());
			} else if (webSocketManager.getPath().equals(req.getUri())) {
				servlet = webSocketManager.getServlet();
				pathInfo = req.getUri();
			} else
				throw new IllegalArgumentException("invalid request path: " + req.getUri());

			HttpServletRequest request = new Request(ctx, req, servletPath, pathInfo);
			servlet.service(request, response);

			// prevent http response closing for websocket
			if (webSocketManager.getPath().equals(req.getUri()))
				response = null;
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ServletException e) {
			logger.error("kraken webconsole: servlet service error.", e);
		} finally {
			if (response != null)
				response.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		List<String> trace = Arrays.asList("Connection reset by peer",
				"An existing connection was forcibly closed by the remote host");

		if (e.getCause() instanceof IOException && trace.contains(e.getCause().getMessage())) {
			logger.trace("kraken webconsole: websocket reset", e.getCause());
		} else if (e.getCause() instanceof ClosedChannelException) {
			logger.trace("kraken webconsole: websocket closed", e.getCause());
		} else {
			logger.error("kraken webconsole: websocket transport error", e.getCause());
			e.getChannel().close();
		}
	}
}
