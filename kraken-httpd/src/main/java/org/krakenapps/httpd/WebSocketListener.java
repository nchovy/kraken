package org.krakenapps.httpd;

import java.net.InetSocketAddress;

import javax.servlet.http.HttpSession;

public interface WebSocketListener {
	void onConnected(InetSocketAddress remote, HttpSession session);

	void onDisconnected(InetSocketAddress remote);

	void onMessage(WebSocketFrame frame);
}
