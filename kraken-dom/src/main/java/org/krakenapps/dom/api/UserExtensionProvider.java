package org.krakenapps.dom.api;

import java.util.Map;

public interface UserExtensionProvider {
	String getName();

	UserExtensionSchema getSchema();

	Map<String, Object> getExtension(int orgId, int id);

	Object get(int orgId, int id, String key);

	void set(int orgId, int id, Map<String, Object> values);
}
