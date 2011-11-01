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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.krakenapps.webconsole.ServletRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class.getName());
	private static final String WEBSOCKET_PATH = "/websocket";
	private static final int MAX_WEBSOCKET_FRAME_SIZE = 8 * 1024 * 1024;

	private MessageBus msgbus;
	private ServletRegistry servletRegistry;

	public WebSocketServerHandler(MessageBus msgbus, ServletRegistry servletRegistry) {
		this.msgbus = msgbus;
		this.servletRegistry = servletRegistry;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// At this moment, channel is already closed
		// Do NOT call ctx.getChannel().close() again
		Session session = msgbus.getSession(ctx.getChannel().getId());
		if (session != null)
			msgbus.closeSession(session);
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
		// handshake request
		String path = req.getUri();
		if (req.getMethod() == HttpMethod.GET && path.equals(WEBSOCKET_PATH)
				&& HttpHeaders.Values.UPGRADE.equalsIgnoreCase(req.getHeader(HttpHeaders.Names.CONNECTION))
				&& HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(req.getHeader(HttpHeaders.Names.UPGRADE))) {

			// create websocket handshake response
			HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101,
					"Web Socket Protocol Handshake"));
			resp.addHeader(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
			resp.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);

			// fill in the headers and contents depending on handshake method
			if (req.containsHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY1)
					&& req.containsHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY2)) {
				// New handshake method with a challenge
				resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN, req.getHeader(HttpHeaders.Names.ORIGIN));
				resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
				String protocol = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
				if (protocol != null) {
					resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, protocol);
				}

				// calculate the answer of the challenge
				String key1 = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY1);
				String key2 = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY2);
				int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
				int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
				long c = req.getContent().readLong();
				ChannelBuffer input = ChannelBuffers.buffer(16);
				input.writeInt(a);
				input.writeInt(b);
				input.writeLong(c);
				ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(
						input.array()));
				resp.setContent(output);
			} else {
				// Old handshake method with no challenge
				resp.addHeader(HttpHeaders.Names.WEBSOCKET_ORIGIN, req.getHeader(HttpHeaders.Names.ORIGIN));
				resp.addHeader(HttpHeaders.Names.WEBSOCKET_LOCATION, getWebSocketLocation(req));
				String protocol = req.getHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL);
				if (protocol != null) {
					resp.addHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL, protocol);
				}
			}

			// upgrade the connection and send the handshake response
			ChannelPipeline p = ctx.getChannel().getPipeline();
			p.remove("aggregator");
			p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder(MAX_WEBSOCKET_FRAME_SIZE));

			ctx.getChannel().write(resp);

			p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());

			// open session
			WebSocketSession session = new WebSocketSession(ctx.getChannel());
			msgbus.openSession(session);

			return;
		}

		try {
			servletRegistry.service(ctx, req);
		} catch (IllegalArgumentException e) {
			// send an error page otherwise
			sendHttpResponse(ctx, req, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		Integer id = ctx.getChannel().getId();
		Session session = msgbus.getSession(id);
		if (session == null) {
			// should not reachable
			logger.error("kraken webconsole: session not found for channel [{}]", id);
			return;
		}

		Message msg = KrakenMessageDecoder.decode(session, frame.getTextData());
		if (msg != null)
			msgbus.dispatch(session, msg);
	}

	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse resp) {
		if (resp.getStatus().getCode() != 200) {
			resp.setContent(ChannelBuffers.copiedBuffer(resp.getStatus().toString(), CharsetUtil.UTF_8));
			HttpHeaders.setContentLength(resp, resp.getContent().readableBytes());
		}

		ChannelFuture f = ctx.getChannel().write(resp);
		if (!HttpHeaders.isKeepAlive(req) || resp.getStatus().getCode() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
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

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	}
}
