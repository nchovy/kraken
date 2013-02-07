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

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpContextRegistry;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.httpd.WebSocket;
import org.krakenapps.httpd.WebSocketFrame;
import org.krakenapps.httpd.WebSocketListener;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.krakenapps.webconsole.WebConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MsgbusPlugin
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
		if (httpd != null) {
			HttpContextRegistry contextRegistry = httpd.getContextRegistry();
			HttpContext ctx = contextRegistry.ensureContext("webconsole");
			ctx.getWebSocketManager().removeListener(this);
		}
	}

	@Override
	public void onConnected(WebSocket socket) {
		logger.trace("kraken webconsole: websocket connected [{}]", socket);
		WebSocketSession webSocketSession = new WebSocketSession(socket);
		sessions.put(socket.getRemoteAddress(), webSocketSession);
		msgbus.openSession(webSocketSession);
	}

	@Override
	public void onDisconnected(WebSocket socket) {
		WebSocketSession session = sessions.get(socket.getRemoteAddress());
		if (session == null)
			return;

		logger.trace("kraken webconsole: websocket disconnected [{}]", session);
		msgbus.closeSession(session);
	}

	@Override
	public void onMessage(WebSocket socket, WebSocketFrame frame) {
		WebSocketSession session = sessions.get(socket.getRemoteAddress());
		if (session == null) {
			logger.error("kraken webconsole: session not found for [{}]", socket);
			return;
		}

		logger.trace("kraken webconsole: websocket frame [{}]", frame);
		Message msg = KrakenMessageDecoder.decode(session, frame.getTextData());
		if (msg != null)
			msgbus.dispatch(session, msg);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void onSessionClose(Session session) {
		logger.debug("kraken webconsole: session [{}] closed", session);

		for (InetSocketAddress key : sessions.keySet()) {
			WebSocketSession wss = sessions.get(key);
			if (wss.getGuid().equals(session.getGuid())) {
				logger.info("kraken webconsole: kill websocket session [{}]", wss);
				wss.close();
				sessions.remove(key);
				break;
			}
		}
	}
}
