package org.krakenapps.util;

public interface ManagedInstanceFactory<T, Hint> {
	T newInstance();

	T newInstance(Hint k);
}
