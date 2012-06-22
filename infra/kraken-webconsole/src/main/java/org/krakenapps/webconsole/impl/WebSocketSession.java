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

import java.net.InetAddress;

import org.krakenapps.httpd.WebSocket;
import org.krakenapps.msgbus.AbstractSession;
import org.krakenapps.msgbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketSession extends AbstractSession {
	private final Logger logger = LoggerFactory.getLogger(WebSocketSession.class.getName());
	private WebSocket socket;

	public WebSocketSession(WebSocket socket) {
		this.socket = socket;
	}

	@Override
	public InetAddress getLocalAddress() {
		return socket.getLocalAddress().getAddress();
	}

	@Override
	public InetAddress getRemoteAddress() {
		return socket.getRemoteAddress().getAddress();
	}

	public void send(Message msg) {
		String payload = KrakenMessageEncoder.encode(this, msg);
		if (logger.isDebugEnabled())
			logger.debug("kraken webconsole: sending [{}]", payload);

		if (socket != null)
			socket.send(payload);
	}

	public void close() {
		if (socket != null) {
			socket.close();
			socket = null;
		}
	}

	@Override
	public String toString() {
		return "websocket session, guid=" + getGuid() + ", remote=" + socket.getRemoteAddress();
	}

}
