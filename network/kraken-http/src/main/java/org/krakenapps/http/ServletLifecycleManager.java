package org.krakenapps.http;

import org.apache.felix.ipojo.ComponentInstance;

public interface ServletLifecycleManager {
	void registerServlet(KrakenHttpService http, String alias, ComponentInstance servletComponentInstance);

	void unregisterServlet(String instanceName);
}
