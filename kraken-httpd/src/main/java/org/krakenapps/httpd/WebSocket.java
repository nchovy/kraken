package org.krakenapps.httpd;

import java.net.InetSocketAddress;

public interface WebSocket {
	InetSocketAddress getLocalAddress();

	InetSocketAddress getRemoteAddress();

	void send(String frame);

	void close();
}
