package org.krakenapps.confdb;

import java.util.Iterator;

public interface ConfigCollection {
	String getName();

	Iterator<Config> find(Predicate pred);

	Config findOne(Predicate pred);

	Config add(Object o);

	Config update(Config c);

	Config update(Config c, boolean ignoreConflict);

	Config remove(Config c);

	Config remove(Config c, boolean ignoreConflict);

	void addHook(ActionType type, ConfigListener listener);

	void removeHook(ActionType type, ConfigListener listener);
}
