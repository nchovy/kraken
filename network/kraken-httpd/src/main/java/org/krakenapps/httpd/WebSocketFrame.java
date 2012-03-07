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

public class WebSocketFrame {
	private InetSocketAddress remote;
	private String host;
	private int type;
	private String text;

	public WebSocketFrame() {
	}

	public WebSocketFrame(String text) {
		this.text = text;
	}

	public InetSocketAddress getRemote() {
		return remote;
	}

	public void setRemote(InetSocketAddress remote) {
		this.remote = remote;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getTextData() {
		return text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "remote=" + remote + ", host=" + host + ", type=" + type + ", text=" + text;
	}
}
