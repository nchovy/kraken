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

import javax.servlet.http.HttpSession;

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

	private ConcurrentMap<InetSocketAddress, HttpSession> sessions;
	private CopyOnWriteArraySet<WebSocketListener> listeners;

	public WebSocketManager() {
		servlet = new WebSocketServlet(this);
		sessions = new ConcurrentHashMap<InetSocketAddress, HttpSession>();
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

	public Collection<HttpSession> getSessions() {
		return new ArrayList<HttpSession>(sessions.values());
	}

	public void register(InetSocketAddress remote, HttpSession session) {
		logger.trace("kraken httpd: adding websocket session [{}]", session);
		sessions.put(remote, session);

		// invoke callbacks
		for (WebSocketListener listener : listeners) {
			try {
				listener.onConnected(remote, session);
			} catch (Exception e) {
				logger.warn("kraken httpd: callback should not throw any exception", e);
			}
		}
	}

	public void unregister(InetSocketAddress remote) {
		logger.trace("kraken httpd: removing websocket session [{}]", remote);

		for (WebSocketListener listener : listeners) {
			try {
				listener.onDisconnected(remote);
			} catch (Exception e) {
				logger.warn("kraken httpd: callback should not throw any exception", e);
			}
		}

		sessions.remove(remote);
	}

	public void dispatch(WebSocketFrame frame) {
		logger.trace("kraken httpd: received frame [{}]", frame);

		for (WebSocketListener listener : listeners) {
			try {
				listener.onMessage(frame);
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
		return "WebSocket path [" + path + "], sessions [" + sessions.size() + "]";
	}
}
