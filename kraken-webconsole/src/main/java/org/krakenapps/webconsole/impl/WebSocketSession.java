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
import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.krakenapps.msgbus.AbstractSession;
import org.krakenapps.msgbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketSession extends AbstractSession {
	private final Logger logger = LoggerFactory.getLogger(WebSocketSession.class.getName());
	private Channel channel;

	public WebSocketSession(Channel channel) {
		this.channel = channel;
	}

	public int getId() {
		return channel.getId();
	}

	@Override
	public InetAddress getLocalAddress() {
		return ((InetSocketAddress) channel.getLocalAddress()).getAddress();
	}

	@Override
	public InetAddress getRemoteAddress() {
		return ((InetSocketAddress) channel.getRemoteAddress()).getAddress();
	}

	public void send(Message msg) {
		String payload = KrakenMessageEncoder.encode(this, msg);
		if (logger.isDebugEnabled())
			logger.debug("kraken webconsole: sending [{}]", payload);

		if (channel != null)
			channel.write(new DefaultWebSocketFrame(payload));
	}

	public void close() {
		channel.close();
		channel = null;
	}

	@Override
	public String toString() {
		return channel.toString();
	}

}
