/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logstorage.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.ConfigTransaction;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.logstorage.LogTableEventListener;
import org.krakenapps.logstorage.LogTableNotFoundException;
import org.krakenapps.logstorage.LogTableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-table-registry")
@Provides
public class LogTableRegistryImpl implements LogTableRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogTableRegistryImpl.class.getName());

	@Requires
	private ConfigService conf;

	/**
	 * table id generator
	 */
	private AtomicInteger nextTableId;

	/**
	 * table id to name mappings
	 */
	private ConcurrentMap<Integer, String> tableNames;

	/**
	 * table name to schema mappings
	 */
	private ConcurrentMap<String, LogTableSchema> tableSchemas;

	private CopyOnWriteArraySet<LogTableEventListener> callbacks;

	public LogTableRegistryImpl() {
		tableSchemas = new ConcurrentHashMap<String, LogTableSchema>();
		tableNames = new ConcurrentHashMap<Integer, String>();
		callbacks = new CopyOnWriteArraySet<LogTableEventListener>();

		// load table id mappings
		loadTableMappings();
	}

	private void loadTableMappings() {
		int maxId = 0;

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		ConfigCollection col = db.getCollection(LogTableSchema.class);
		if (col == null) {
			col = db.ensureCollection(LogTableSchema.class);
			ConfigCollection before = db.getCollection("org.krakenapps.logstorage.engine.LogTableSchema");
			if (before != null) {
				ConfigTransaction xact = db.beginTransaction();
				try {
					ConfigIterator it = before.findAll();
					while (it.hasNext())
						col.add(xact, it.next().getDocument());
					xact.commit("kraken-logstorage", "migration from collection org.krakenapps.logstorage.engine.LogTableSchema");
					db.dropCollection("org.krakenapps.logstorage.engine.LogTableSchema");
				} catch (Throwable e) {
					xact.rollback();
					throw new IllegalStateException("migration failed");
				}
			}
		}

		ConfigIterator it = col.findAll();
		for (LogTableSchema t : it.getDocuments(LogTableSchema.class)) {
			tableNames.put(t.getId(), t.getName());
			tableSchemas.put(t.getName(), t);
			if (maxId < t.getId())
				maxId = t.getId();
		}

		nextTableId = new AtomicInteger(maxId);
	}

	@Override
	public boolean exists(String tableName) {
		return tableSchemas.containsKey(tableName);
	}

	@Override
	public Collection<String> getTableNames() {
		return new ArrayList<String>(tableSchemas.keySet());
	}

	@Override
	public int getTableId(String tableName) {
		LogTableSchema table = tableSchemas.get(tableName);
		if (table == null)
			throw new LogTableNotFoundException(tableName);

		return table.getId();
	}

	@Override
	public String getTableName(int tableId) {
		return tableNames.get(tableId);
	}

	@Override
	public void createTable(String tableName, Map<String, String> tableMetadata) {
		if (tableSchemas.containsKey(tableName))
			throw new IllegalStateException("table already exists: " + tableName);

		int newId = nextTableId.incrementAndGet();
		LogTableSchema table = new LogTableSchema(newId, tableName);
		if (tableMetadata != null)
			table.getMetadata().putAll(tableMetadata);

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		db.add(table, "kraken-logstorage", "created " + tableName + " table");

		tableNames.put(table.getId(), table.getName());
		tableSchemas.put(tableName, table);

		// invoke callbacks
		for (LogTableEventListener callback : callbacks) {
			try {
				callback.onCreate(tableName, tableMetadata);
			} catch (Exception e) {
				logger.warn("kraken logstorage: table event listener should not throw any exception", e);
			}
		}
	}

	@Override
	public void renameTable(String currentName, String newName) {
		// check duplicated name first
		if (tableSchemas.containsKey(newName))
			throw new IllegalStateException("table already exists: " + newName);

		// change renamed table metadata
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogTableSchema.class, Predicates.field("name", currentName));
		if (c == null)
			throw new IllegalStateException("table not found: " + currentName);

		LogTableSchema schema = c.getDocument(LogTableSchema.class);
		schema.setName(newName);
		db.update(c, schema);

		// rename table in memory
		tableSchemas.remove(currentName);
		tableSchemas.putIfAbsent(newName, schema);
		tableNames.put(schema.getId(), newName);
	}

	@Override
	public void dropTable(String tableName) {
		LogTableSchema old = tableSchemas.remove(tableName);
		if (old == null)
			throw new IllegalStateException("table not found: " + tableName);

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogTableSchema.class, Predicates.field("name", tableName));
		if (c == null)
			return;
		db.remove(c);

		LogTableSchema t = tableSchemas.remove(tableName);
		if (t != null)
			tableNames.remove(t.getId());

		// invoke callbacks
		for (LogTableEventListener callback : callbacks) {
			try {
				callback.onDrop(tableName);
			} catch (Exception e) {
				logger.warn("kraken logstorage: table event listener should not throw any exception", e);
			}
		}
	}

	@Deprecated
	@Override
	public org.krakenapps.logstorage.TableMetadata getTableMetadata(int tableId) {
		String tableName = tableNames.get(tableId);
		if (tableName == null)
			return null;

		LogTableSchema t = tableSchemas.get(tableName);
		Map<String, String> m = new HashMap<String, String>();
		for (String key : t.getMetadata().keySet()) {
			Object value = t.getMetadata().get(key);
			m.put(key, value == null ? null : value.toString());
		}
		return new org.krakenapps.logstorage.TableMetadata(tableId, tableName, m);
	}

	@Override
	public Set<String> getTableMetadataKeys(String tableName) {
		LogTableSchema t = tableSchemas.get(tableName);
		if (t == null)
			throw new IllegalArgumentException("table not exists: " + tableName);

		return t.getMetadata().keySet();
	}

	@Override
	public String getTableMetadata(String tableName, String key) {
		LogTableSchema t = tableSchemas.get(tableName);
		if (t == null)
			throw new IllegalArgumentException("table not exists: " + tableName);

		return (String) t.getMetadata().get(key);
	}

	@Override
	public void setTableMetadata(String tableName, String key, String value) {
		LogTableSchema t = tableSchemas.get(tableName);
		if (t == null)
			throw new IllegalArgumentException("table not exists: " + tableName);

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogTableSchema.class, Predicates.field("name", tableName));
		t.getMetadata().put(key, value);
		db.update(c, t, false, "kraken-logstorage", "set table [" + tableName + "] metadata " + key + " to " + value);
	}

	@Override
	public void unsetTableMetadata(String tableName, String key) {
		LogTableSchema t = tableSchemas.get(tableName);
		if (t == null)
			throw new IllegalArgumentException("table not exists: " + tableName);

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogTableSchema.class, Predicates.field("name", tableName));
		t.getMetadata().remove(key);
		db.update(c, t, false, "kraken-logstorage", "unset table [" + tableName + "] metadata " + key);
	}

	@Override
	public void addListener(LogTableEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeListener(LogTableEventListener listener) {
		callbacks.remove(listener);
	}
}
