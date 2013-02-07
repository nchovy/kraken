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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigTransaction;

public class Transaction {
	private static final boolean CHECK_CONFLICT = false;

	private static Map<ConfigTransaction, Transaction> xacts = new HashMap<ConfigTransaction, Transaction>();

	private String domain;
	private ConfigDatabase db;
	private ConfigTransaction xact;
	private List<Log> logs = new ArrayList<Log>();
	private Map<Class<?>, DefaultEntityEventProvider<?>> eventProviders = new HashMap<Class<?>, DefaultEntityEventProvider<?>>();

	public static Transaction getInstance(ConfigTransaction xact) {
		return xacts.get(xact);
	}

	public Transaction(String domain, ConfigDatabase db) {
		this.domain = domain;
		this.db = db;
		this.xact = db.beginTransaction();
		xacts.put(this.xact, this);
	}

	public ConfigDatabase getConfigDatabase() {
		return xact.getDatabase();
	}

	public ConfigTransaction getConfigTransaction() {
		return xact;
	}

	public void add(Object doc) {
		add(doc, null);
	}

	public void add(Object doc, Object state) {
		db.add(xact, doc);
		logs.add(new Log(Log.Operation.Add, doc, state));
	}

	public void update(Config c, Object doc) {
		update(c, doc, CHECK_CONFLICT, null);
	}

	public void update(Config c, Object doc, boolean checkConflict) {
		update(c, doc, checkConflict, null);
	}

	public void update(Config c, Object doc, Object state) {
		update(c, doc, CHECK_CONFLICT, state);
	}

	public void update(Config c, Object doc, boolean checkConflict, Object state) {
		db.update(xact, c, doc, checkConflict);
		logs.add(new Log(Log.Operation.Update, doc, state));
	}

	public void remove(Config c, Object doc) {
		remove(c, doc, CHECK_CONFLICT, null);
	}

	public void remove(Config c, Object doc, boolean checkConflict) {
		remove(c, doc, checkConflict, null);
	}

	public void remove(Config c, Object doc, Object removingState, Object removedState) {
		remove(c, doc, CHECK_CONFLICT, removingState, removedState);
	}

	@SuppressWarnings("unchecked")
	public void remove(Config c, Object doc, boolean checkConflict, Object removingState, Object removedState) {
		DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(doc.getClass());
		if (provider != null)
			provider.fireEntityRemoving(domain, doc, xact, removingState);

		db.remove(xact, c, checkConflict);
		logs.add(new Log(Log.Operation.Remove, doc, removedState));
	}

	public void removePreChecked(Config c, Object doc, boolean checkConflict, Object removedState) {
		db.remove(xact, c, checkConflict);
		logs.add(new Log(Log.Operation.Remove, doc, removedState));
	}

	@SuppressWarnings("unchecked")
	public void commit(String committer, String log) {
		xact.commit(committer, log);

		Map<Class<?>, List<EntityState>> addedMap = new HashMap<Class<?>, List<EntityState>>();
		Map<Class<?>, List<EntityState>> updatedMap = new HashMap<Class<?>, List<EntityState>>();
		Map<Class<?>, List<EntityState>> removedMap = new HashMap<Class<?>, List<EntityState>>();

		for (Log l : logs) {
			Class<?> clazz = l.obj.getClass();
			DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(clazz);
			if (provider == null)
				continue;

			if (l.op == Log.Operation.Add) {
				List<EntityState> logs = addedMap.get(clazz);
				if (logs == null) {
					logs = new LinkedList<EntityState>();
					addedMap.put(clazz, logs);
				}

				logs.add(new EntityState(l.obj, l.state));
			} else if (l.op == Log.Operation.Update) {
				List<EntityState> logs = updatedMap.get(clazz);
				if (logs == null) {
					logs = new LinkedList<EntityState>();
					updatedMap.put(clazz, logs);
				}

				logs.add(new EntityState(l.obj, l.state));
			} else if (l.op == Log.Operation.Remove) {
				List<EntityState> logs = removedMap.get(clazz);
				if (logs == null) {
					logs = new LinkedList<EntityState>();
					removedMap.put(clazz, logs);
				}

				logs.add(new EntityState(l.obj, l.state));
			}
		}

		for (Class<?> key : addedMap.keySet()) {
			DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(key);
			provider.fireEntitiesAdded(domain, addedMap.get(key));
		}

		for (Class<?> key : updatedMap.keySet()) {
			DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(key);
			provider.fireEntitiesUpdated(domain, updatedMap.get(key));
		}

		for (Class<?> key : removedMap.keySet()) {
			DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(key);
			provider.fireEntitiesRemoved(domain, removedMap.get(key));
		}

		xacts.remove(this.xact);
	}

	public <T> void checkRemovability(Class<T> cls, Collection<EntityState> entities) {
		@SuppressWarnings("unchecked")
		DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(cls);
		if (provider != null)
			provider.fireEntitiesRemoving(domain, entities, xact);
	}

	public <T> void checkAddability(Class<T> cls, EntityState entity) {
		@SuppressWarnings("unchecked")
		DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(cls);
		if (provider != null)
			provider.fireEntityAdding(domain, entity.entity);
	}
	
	public <T> void checkAddability(Class<T> cls, Collection<EntityState> entities) {
		@SuppressWarnings("unchecked")
		DefaultEntityEventProvider<Object> provider = (DefaultEntityEventProvider<Object>) eventProviders.get(cls);
		if (provider != null)
			provider.fireEntitiesAdding(domain, entities);
	}

	public void rollback() {
		xact.rollback();
		xacts.remove(this.xact);
	}

	public <T> void addEventProvider(Class<T> cls, DefaultEntityEventProvider<T> provider) {
		eventProviders.put(cls, provider);
	}

	public DefaultEntityEventProvider<?> getEventProvider(Class<?> cls) {
		return eventProviders.get(cls);
	}

	public void removeEventProvider(Class<?> cls) {
		eventProviders.remove(cls);
	}

	public String getDomain() {
		return domain;
	}

	private static class Log {
		private static enum Operation {
			Add, Update, Remove
		}

		private Operation op;
		private Object obj;
		private Object state;

		private Log(Operation op, Object obj, Object state) {
			if (op == null || obj == null)
				throw new NullPointerException();

			this.op = op;
			this.obj = obj;
			this.state = state;
		}
	}

}
