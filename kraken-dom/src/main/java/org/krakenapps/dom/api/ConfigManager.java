package org.krakenapps.dom.api;

import java.util.Collection;
import java.util.List;

import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.Predicate;

public interface ConfigManager {
	ConfigDatabase findDatabase(String domain);

	ConfigDatabase getDatabase(String domain);

	<T> Collection<T> all(String domain, Class<T> cls);

	<T> Collection<T> all(String domain, Class<T> cls, Predicate pred);

	<T> T find(String domain, Class<T> cls, Predicate pred);

	<T> T get(String domain, Class<T> cls, Predicate pred, String notFoundMessage);

	<T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage, DefaultEntityEventProvider<T> provider);

	<T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage, DefaultEntityEventProvider<T> provider);

	<T> void removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage, DefaultEntityEventProvider<T> provider);

	<T> void remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage, DefaultEntityEventProvider<T> provider);

	PrimitiveParseCallback getParseCallback(String domain);
}
