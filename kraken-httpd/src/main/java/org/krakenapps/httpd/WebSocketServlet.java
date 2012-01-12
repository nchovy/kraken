package org.krakenapps.httpd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.krakenapps.httpd.impl.WebSocketFrameDecoderWithHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int MAX_WEBSOCKET_FRAME_SIZE = 8 * 1024 * 1024;
	private final Logger logger = LoggerFactory.getLogger(WebSocketServlet.class.getName());

	private WebSocketManager manager;

	public WebSocketServlet(WebSocketManager manager) {
		this.manager = manager;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// NOTE: check WEBSOCKET_PATH

		if (!HttpHeaders.Values.UPGRADE.equalsIgnoreCase(req.getHeader(HttpHeaders.Names.CONNECTION))
				|| !HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(req.getHeader(HttpHeaders.Names.UPGRADE)))
			return;

		try {
			// create websocket handshake response
			resp.setStatus(HttpServletResponse.SC_SWITCHING_PROTOCOLS);
			resp.addHeader(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
			resp.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);

			// fill in the headers and contents depending on handshake method
			String key1 = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY1);
			String key2 = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_KEY2);
			if (key1 != null && key2 != null) {
				// New handshake method with a challenge
				resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN, req.getHeader(HttpHeaders.Names.ORIGIN));
				resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
				String protocol = req.getHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL);
				if (protocol != null) {
					resp.addHeader(HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL, protocol);
				}

				// calculate the answer of the challenge
				int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
				int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
				byte[] l = new byte[8];
				req.getInputStream().read(l);
				ByteBuffer bb = ByteBuffer.wrap(l);
				bb.flip();
				long c = bb.getLong();
				ChannelBuffer input = ChannelBuffers.buffer(16);
				input.writeInt(a);
				input.writeInt(b);
				input.writeLong(c);
				byte[] output = MessageDigest.getInstance("MD5").digest(input.array());
				resp.getOutputStream().write(output);
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
			String host = req.getHeader(HttpHeaders.Names.HOST);
			Channel channel = (Channel) req.getAttribute("netty.channel");
			ChannelPipeline p = channel.getPipeline();
			p.remove("aggregator");
			p.replace("decoder", "wsdecoder", new WebSocketFrameDecoderWithHost(host, MAX_WEBSOCKET_FRAME_SIZE));
			p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());

			// open session
			InetSocketAddress remote = new InetSocketAddress(req.getRemoteAddr(), req.getRemotePort());
			manager.register(remote, req.getSession());
		} catch (NoSuchAlgorithmException e) {
			logger.error("kraken webconsole: md5 digest not found", e);
		}
	}

	private String getWebSocketLocation(HttpServletRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + manager.getPath();
	}
}
