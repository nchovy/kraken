package org.krakenapps.confdb;

public interface ConfigTransactionCache {
	Object get(Class<?> cls, ReferenceKeys pred);

	void put(Class<?> cls, Object obj);

	void remove(Class<?> cls, Object obj);
}
