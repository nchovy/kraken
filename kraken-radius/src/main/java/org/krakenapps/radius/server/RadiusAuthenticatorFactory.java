package org.krakenapps.radius.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RadiusAuthenticatorFactory {
	Set<Integer> getSupportedAuthTypes();

	List<RadiusConfigMetadata> getConfigMetadatas();

	RadiusAuthenticator newInstance(String name, Map<String, Object> configs);
}
