/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.httpd;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManager {
	private final Logger logger = LoggerFactory.getLogger(WebSocketManager.class.getName());
	private static final String WEBSOCKET_PATH = "/websocket";

	/**
	 * part of websocket location (ws://domain/path/to/websocket)
	 */
	private String path = WEBSOCKET_PATH;

	private WebSocketServlet servlet;

	private ConcurrentMap<InetSocketAddress, WebSocket> sockets;
	private CopyOnWriteArraySet<WebSocketListener> listeners;

	public WebSocketManager() {
		servlet = new WebSocketServlet(this);
		sockets = new ConcurrentHashMap<InetSocketAddress, WebSocket>();
		listeners = new CopyOnWriteArraySet<WebSocketListener>();
	}

	public WebSocketServlet getServlet() {
		return servlet;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Collection<WebSocket> getSockets() {
		return new ArrayList<WebSocket>(sockets.values());
	}

	public void register(WebSocket socket) {
		logger.trace("kraken httpd: adding websocket session [{}]", socket);
		sockets.put(socket.getRemoteAddress(), socket);

		// invoke callbacks
		for (WebSocketListener listener : listeners) {
			try {
				listener.onConnected(socket);
			} catch (Exception e) {
				logger.warn("kraken httpd: callback should not throw any exception", e);
			}
		}
	}

	public void unregister(InetSocketAddress remote) {
		WebSocket socket = sockets.remove(remote);
		if (socket == null)
			return;

		logger.trace("kraken httpd: removing websocket session [{}]", remote);

		for (WebSocketListener listener : listeners) {
			try {
				listener.onDisconnected(socket);
			} catch (Exception e) {
				logger.warn("kraken httpd: callback should not throw any exception", e);
			}
		}
	}

	public void dispatch(WebSocketFrame frame) {
		WebSocket socket = sockets.get(frame.getRemote());
		if (socket == null) {
			logger.warn("kraken httpd: websocket not found for frame [{}]", frame);
			return;
		}

		logger.trace("kraken httpd: received frame [{}]", frame);

		for (WebSocketListener listener : listeners) {
			try {
				listener.onMessage(socket, frame);
			} catch (Exception e) {
				logger.warn("kraken httpd: callback should not throw any exception", e);
			}
		}

	}

	public void addListener(WebSocketListener listener) {
		logger.trace("kraken httpd: listener [{}] added", listener);
		listeners.add(listener);
	}

	public void removeListener(WebSocketListener listener) {
		logger.trace("kraken httpd: listener [{}] removed", listener);
		listeners.remove(listener);
	}

	@Override
	public String toString() {
		return "WebSocket path [" + path + "], sessions [" + sockets.size() + "]";
	}
}
