package org.krakenapps.httpd;

import java.util.Collection;

public interface HttpContextRegistry {
	Collection<String> getContextNames();

	HttpContext ensureContext(String name);

	HttpContext findContext(String name);

	void removeContext(String name);

}
