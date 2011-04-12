package org.krakenapps.portmon;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface PortEventListener {
	void onConnect(InetSocketAddress target, int connectTime);

	void onConnectRefused(InetSocketAddress target, int timeout, IOException e);
}
