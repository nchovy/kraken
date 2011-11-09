package org.krakenapps.api;

public interface PrimitiveSerializeCallback {
	void onSerialize(Object root, Object parent, Class<?> cls, Object serialized);
}
