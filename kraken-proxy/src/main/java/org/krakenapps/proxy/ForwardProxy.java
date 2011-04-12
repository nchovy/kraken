package org.krakenapps.proxy;

import java.util.Collection;

public interface ForwardProxy {
	Collection<String> getRouteNames();

	ForwardRoute getRoute(String name);

	void addRoute(String name, ForwardRoute route);

	void removeRoute(String name);
}
