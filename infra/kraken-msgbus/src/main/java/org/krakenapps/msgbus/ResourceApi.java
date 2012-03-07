package org.krakenapps.msgbus;

public interface ResourceApi {
	ResourceHandler getResourceHandler(String groupId);

	void register(String groupId, ResourceHandler handler);

	void unregister(String groupId, ResourceHandler handler);
}
