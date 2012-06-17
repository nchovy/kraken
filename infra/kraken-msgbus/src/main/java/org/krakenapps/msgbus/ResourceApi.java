package org.krakenapps.msgbus;

import java.util.Collection;

public interface ResourceApi {
	Collection<String> getResourceHandlerKeys();

	ResourceHandler getResourceHandler(String groupId);

	void register(String groupId, ResourceHandler handler);

	void unregister(String groupId, ResourceHandler handler);
}
