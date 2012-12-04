package org.krakenapps.confdb;

public interface ObjectBuilder<T> {
	T build(Config c);
}
