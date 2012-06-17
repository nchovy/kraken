package org.krakenapps.msgbus.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.msgbus.ResourceApi;
import org.krakenapps.msgbus.ResourceHandler;

@Component(name = "msgbus-resource-api")
@Provides
public class ResourceApiImpl implements ResourceApi {
	private ConcurrentMap<String, ResourceHandler> handlers;

	public ResourceApiImpl() {
		handlers = new ConcurrentHashMap<String, ResourceHandler>();
	}

	@Override
	public Collection<String> getResourceHandlerKeys() {
		return new ArrayList<String>(handlers.keySet());
	}

	@Override
	public ResourceHandler getResourceHandler(String groupId) {
		return handlers.get(groupId);
	}

	@Override
	public void register(String groupId, ResourceHandler handler) {
		ResourceHandler old = handlers.putIfAbsent(groupId, handler);
		if (old != null)
			throw new IllegalStateException("duplicated resource handler: " + groupId);
	}

	@Override
	public void unregister(String groupId, ResourceHandler handler) {
		ResourceHandler old = handlers.get(groupId);
		if (old == handler)
			handlers.remove(groupId);
	}
}
