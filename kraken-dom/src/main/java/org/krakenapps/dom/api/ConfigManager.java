package org.krakenapps.dom.api;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.Predicate;

public interface ConfigManager {
	ConfigDatabase findDatabase(String domain);

	ConfigDatabase getDatabase(String domain);

	ConfigCollection ensureCollection(String domain, Class<?> cls);

	Config findOne(ConfigDatabase db, Class<?> cls, Predicate pred, String notFoundMessage);

	<T> T find(String domain, Class<T> cls, Predicate pred);

	<T> T get(String domain, Class<T> cls, Predicate pred, String notFoundMessage);

	<T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage, DefaultEntityEventProvider<T> provider);

	<T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage, DefaultEntityEventProvider<T> provider);

	<T> void remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage, DefaultEntityEventProvider<T> provider);
}
