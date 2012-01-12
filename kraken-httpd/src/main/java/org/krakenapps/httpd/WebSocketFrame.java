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
		return "host=" + host + ", type=" + type + ", text=" + text;
	}
}
