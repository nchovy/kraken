package org.krakenapps.portmon;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface PortMonitor {

	void setTimeout(int milliseconds);

	Collection<InetSocketAddress> getTcpTargets();

	PortStatus getTcpPortStatus(InetSocketAddress target);

	void run();

	void addTcpTarget(InetSocketAddress target);

	void removeTcpTarget(InetSocketAddress target);

	void addListener(PortEventListener callback);

	void removeListener(PortEventListener callback);
}
