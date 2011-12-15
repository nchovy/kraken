package org.krakenapps.mail;

import java.util.Collection;

public interface PostboxRegistry {
	Collection<PostboxConfig> getConfigs();

	PostboxConfig getConfig(String name);

	void register(PostboxConfig config);

	void unregister(String name);

	Postbox connect(PostboxConfig config);
}
