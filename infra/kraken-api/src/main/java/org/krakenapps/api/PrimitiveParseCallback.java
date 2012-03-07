package org.krakenapps.api;

import java.util.Map;

public interface PrimitiveParseCallback {
	<T> T onParse(Class<T> clazz, Map<String, Object> referenceKey);
}
