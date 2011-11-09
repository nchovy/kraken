package org.krakenapps.api;

import java.util.Map;

public interface PrimitiveSerializeCallback {
	void onSerialize(Object root, Class<?> cls, Object obj, Map<String, Object> referenceKeys);
}
