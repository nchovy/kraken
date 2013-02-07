/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.dom.api;

import java.util.Collection;
import java.util.List;

import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigParser;
import org.krakenapps.confdb.ObjectBuilder;
import org.krakenapps.confdb.Predicate;

public interface ConfigManager {
	void setParser(Class<?> cls, ConfigParser parser);

	ConfigDatabase findDatabase(String domain);

	ConfigDatabase getDatabase(String domain);

	<T> int count(String domain, Class<T> cls, Predicate pred);

	List<Config> matches(String domain, Class<?> cls, Predicate pred, int offset, int limit);

	<T> Collection<T> all(String domain, Class<T> cls);

	<T> Collection<T> all(String domain, Class<T> cls, Predicate pred);

	<T> Collection<T> all(String domain, Class<T> cls, Predicate pred, int offset, int limit);

	<T> T find(String domain, Class<T> cls, Predicate pred);

	<T> Collection<T> findObjects(String domain, Class<?> cls, ObjectBuilder<T> builder);

	<T> Collection<T> findObjects(String domain, Class<?> cls, ObjectBuilder<T> builder, Predicate pred, int offset, int limit);

	<T> T get(String domain, Class<T> cls, Predicate pred, String notFoundMessage);

	<T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void adds(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider, Object state);

	<T> void adds(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage);

	<T> void adds(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String alreadyExistMessage, Object state);

	<T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void add(String domain, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage,
			DefaultEntityEventProvider<T> provider, Object state);

	<T> void add(Transaction xact, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage);

	<T> void add(Transaction xact, Class<T> cls, Predicate pred, T doc, String alreadyExistMessage, Object state);

	<T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void updates(String domain, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object state);

	<T> void updates(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage);

	<T> void updates(Transaction xact, Class<T> cls, List<Predicate> preds, List<T> docs, String notFoundMessage, Object state);

	<T> void updateForGuids(String domain, Class<T> cls, List<String> guids, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void updateForGuids(String domain, Class<T> cls, List<String> guids, List<T> docs, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object state);

	<T> void updateForGuids(Transaction xact, Class<T> cls, List<String> guids, List<T> docs, String notFoundMessage);

	<T> void updateForGuids(Transaction xact, Class<T> cls, List<String> guids, List<T> docs, String notFoundMessage, Object state);

	<T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> void update(String domain, Class<T> cls, Predicate pred, T doc, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object state);

	<T> void update(Transaction xact, Class<T> cls, Predicate pred, T doc, String notFoundMessage);

	<T> void update(Transaction xact, Class<T> cls, Predicate pred, T doc, String notFoundMessage, Object state);

	<T> Collection<T> removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> Collection<T> removes(String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState);

	<T> Collection<T> removes(Transaction xact, String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> Collection<T> removes(Transaction xact, String domain, Class<T> cls, List<Predicate> preds, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState);

	<T> T remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage, DefaultEntityEventProvider<T> provider);

	<T> T remove(String domain, Class<T> cls, Predicate pred, String notFoundMessage, DefaultEntityEventProvider<T> provider,
			Object removingState, Object removedState);

	<T> T remove(Transaction xact, String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider);

	<T> T remove(Transaction xact, String domain, Class<T> cls, Predicate pred, String notFoundMessage,
			DefaultEntityEventProvider<T> provider, Object removingState, Object removedState);

	PrimitiveParseCallback getParseCallback(String domain);

}
