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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.krakenapps.msgbus.Message;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketSession implements Session {
	private final Logger logger = LoggerFactory.getLogger(WebSocketSession.class.getName());
	private Channel channel;
	private Map<String, Object> params;

	public WebSocketSession(Channel channel) {
		this.channel = channel;
		this.params = new ConcurrentHashMap<String, Object>();
	}

	@Override
	public Locale getLocale() {
		if (!params.containsKey("locale"))
			return new Locale("en");

		return new Locale((String) params.get("locale"));
	}

	public int getId() {
		return channel.getId();
	}

	@Override
	public Integer getOrgId() {
		return getInt("org_id");
	}

	@Override
	public Integer getAdminId() {
		return getInt("admin_id");
	}

	@Override
	public InetAddress getLocalAddress() {
		return ((InetSocketAddress) channel.getLocalAddress()).getAddress();
	}

	@Override
	public InetAddress getRemoteAddress() {
		return ((InetSocketAddress) channel.getRemoteAddress()).getAddress();
	}

	@Override
	public boolean has(String key) {
		return params.containsKey(key);
	}

	public Object get(String key) {
		return params.get(key);
	}

	public String getString(String key) {
		return (String) params.get(key);
	}

	public Integer getInt(String key) {
		return (Integer) params.get(key);
	}

	public void setProperty(String key, Object value) {
		params.put(key, value);
	}

	public void unsetProperty(String key) {
		params.remove(key);
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
