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
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.krakenapps.httpd.HttpConfiguration;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.VirtualHost;
import org.krakenapps.httpd.WebSocketFrame;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerHandler extends IdleStateAwareChannelHandler {
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
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent event) {
		if (event.getState() == IdleState.ALL_IDLE) {
			Channel channel = ctx.getChannel();
			if (logger.isDebugEnabled()) {
				long idle = new Date().getTime() - event.getLastActivityTimeMillis();
				logger.debug("kraken httpd: closing idle connection [local={}, remote={}, idle={}, state={}]",
						new Object[] { channel.getLocalAddress(), channel.getRemoteAddress(), idle, event.getState() });
			}

			channel.close();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {

			HttpRequest req = (HttpRequest) msg;
			HttpContext httpContext = findHttpContext(req.getHeader(HttpHeaders.Names.HOST));
			if (httpContext == null) {
				Request request = new Request(ctx, req);
				Response response = new Response(bc, ctx, request);
				request.setResponse(response);

				response.sendError(404);
				return;
			}

			Request request = new Request(ctx, req);
			Response response = new Response(bc, ctx, request);
			request.setResponse(response);

			httpContext.handle(request, response);
		} else if (msg instanceof WebSocketFrame) {

			WebSocketFrame frame = (WebSocketFrame) msg;
			HttpContext httpContext = findHttpContext(frame.getHost());
			httpContext.getWebSocketManager().dispatch(frame);
		}
	}

	private HttpContext findHttpContext(String host) {
		if (host != null) {
			for (VirtualHost v : config.getVirtualHosts())
				if (v.matches(host))
					return contextRegistry.findContext(v.getHttpContextName());
		}

		String contextName = config.getDefaultHttpContext();
		if (contextName == null)
			return null;
		return contextRegistry.findContext(contextName);
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

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		List<String> trace = Arrays.asList("Connection reset by peer",
				"An existing connection was forcibly closed by the remote host");

		if (e.getCause() instanceof IOException && trace.contains(e.getCause().getMessage())) {
			logger.trace("kraken httpd: connection reset", e.getCause());
		} else if (e.getCause() instanceof ClosedChannelException) {
			logger.trace("kraken httpd: connection closed", e.getCause());
		} else {
			logger.error("kraken httpd: transport error", e.getCause());
			e.getChannel().close();
		}
	}
}
