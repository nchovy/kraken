package org.krakenapps.webconsole.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpSession;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.channel.Channel;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.httpd.WebSocketFrame;
import org.krakenapps.httpd.WebSocketListener;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.webconsole.WebConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole")
@Provides(specifications = { WebConsole.class })
public class WebConsoleImpl implements WebConsole, WebSocketListener {
	private final Logger logger = LoggerFactory.getLogger(WebConsoleImpl.class.getName());

	@Requires
	private HttpService httpd;

	@Requires
	private MessageBus msgbus;

	private ConcurrentMap<InetSocketAddress, WebSocketSession> sessions;

	@Validate
	public void start() {
		sessions = new ConcurrentHashMap<InetSocketAddress, WebSocketSession>();
		HttpContextRegistry contextRegistry = httpd.getContextRegistry();
		HttpContext ctx = contextRegistry.ensureContext("webconsole");
		ctx.getWebSocketManager().addListener(this);
	}

	@Invalidate
	public void stop() {
	}

	@Override
	public void onConnected(InetSocketAddress remote, HttpSession session) {
		logger.trace("kraken webconsole: websocket connected [{}] from [{}]", session, remote);
		Channel channel = (Channel) session.getAttribute("netty.channel");
		WebSocketSession webSocketSession = new WebSocketSession(channel);
		sessions.put(remote, webSocketSession);
		msgbus.openSession(webSocketSession);
	}

	@Override
	public void onDisconnected(InetSocketAddress remote) {
		WebSocketSession session = sessions.get(remote);
		if (session == null)
			return;

		logger.trace("kraken webconsole: websocket disconnected [{}]", session);
		msgbus.closeSession(session);
	}

	@Override
	public void onMessage(WebSocketFrame frame) {
		WebSocketSession session = sessions.get(frame.getRemote());
		if (session == null) {
			logger.error("kraken webconsole: session not found for [{}]", frame.getRemote());
			return;
		}

		logger.trace("kraken webconsole: websocket frame [{}]", frame);
		Message msg = KrakenMessageDecoder.decode(session, frame.getTextData());
		if (msg != null)
			msgbus.dispatch(session, msg);
	}
}
