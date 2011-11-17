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
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.krakenapps.servlet.api.ServletRegistry;
import org.krakenapps.webconsole.CometSessionStore;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class.getName());
	private static final String WEBSOCKET_PATH = "/websocket";
	private static final int MAX_WEBSOCKET_FRAME_SIZE = 8 * 1024 * 1024;

	private BundleContext bc;
	private MessageBus msgbus;
	private ServletRegistry servletRegistry;
	private CometSessionStore comet;

	public WebSocketServerHandler(BundleContext bc, MessageBus msgbus, ServletRegistry servletRegistry, CometSessionStore comet) {
		this.bc = bc;
		this.msgbus = msgbus;
		this.servletRegistry = servletRegistry;
		this.comet = comet;
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
				ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
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

		// comet session check
		if (req.getUri().startsWith("/kraken-comet")) {
			handleComet(ctx, req);
			return;
		}

		try {
			service(ctx, req);
		} catch (Exception e) {
			logger.error("kraken webconsole: servlet error", e);
		}
	}

	public void service(ChannelHandlerContext ctx, HttpRequest req) throws IOException {
		Response response = null;

		try {
			String servletPath = servletRegistry.getServletPath(req.getUri());
			if (servletPath == null)
				throw new IllegalArgumentException("invalid request path");

			HttpServlet servlet = servletRegistry.getServlet(servletPath);

			String pathInfo = req.getUri().substring(servletPath.length());
			HttpServletRequest request = new Request(ctx, req, servletPath, pathInfo);
			response = new Response(bc, ctx, req);

			servlet.service(request, response);

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

	private void handleComet(ChannelHandlerContext ctx, HttpRequest req) {
		String cookie = req.getHeader(HttpHeaders.Names.COOKIE);
		String sessionKey = null;
		if (cookie != null) {
			int begin = cookie.indexOf("kraken-session=");
			if (begin > 0) {
				int end = cookie.indexOf(";", begin);
				if (end < 0)
					end = cookie.length();

				sessionKey = cookie.substring(begin + "kraken-session=".length(), end);
			}
		}

		if (req.getUri().equals("/kraken-comet/trap"))
			handleCometTrap(ctx, sessionKey);
		else if (req.getUri().equals("/kraken-comet/request"))
			handleCometRequest(ctx, sessionKey, req);
	}

	private void handleCometTrap(ChannelHandlerContext ctx, String sessionKey) {
		if (sessionKey == null) {
			HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
			resp.setHeader(HttpHeaders.Names.SET_COOKIE, "kraken-session=" + UUID.randomUUID().toString());
			resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0");
			ChannelFuture future = ctx.getChannel().write(resp);
			future.addListener(ChannelFutureListener.CLOSE);
			logger.info("kraken webconsole: set cookie and close [{}]", ctx.getChannel().getRemoteAddress());
			return;
		}

		logger.info("kraken webconsole: new comet session [{}]", sessionKey);

		Session session = comet.find(sessionKey);
		if (session != null) {
			// session closed callback will trigger msgbus.closeSession()
			session.close();

			logger.info("kraken webconsole: closed old comet session [{}]", sessionKey);
		}

		session = new CometSession(comet, sessionKey, ctx.getChannel());
		comet.register(sessionKey, session);
		msgbus.openSession(session);

		logger.info("kraken webconsole: comet session [{}] opened", ctx.getChannel().getRemoteAddress());
	}

	private void handleCometRequest(ChannelHandlerContext ctx, String sessionKey, HttpRequest req) {
		ChannelBuffer buf = req.getContent();
		String text = new String(buf.array(), buf.readerIndex(), buf.readableBytes(), Charset.forName("utf-8"));

		CometSession session = new CometSession(comet, sessionKey, ctx.getChannel());
		Message msg = KrakenMessageDecoder.decode(session, text);

		msgbus.dispatch(session, msg);
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
