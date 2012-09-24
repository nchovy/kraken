package org.krakenapps.dom.api;

import org.krakenapps.confdb.Config;

public class ConfigUpdateRequest<T> {
	public Config config;
	public T doc;

	public ConfigUpdateRequest(Config config, T doc) {
		this.config = config;
		this.doc = doc;
	}
}
