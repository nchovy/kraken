/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.eventstorage.EventTableNotFoundException;
import org.krakenapps.eventstorage.EventTableRegistry;

@Component(name = "eventstorage-table-registry")
@Provides
public class EventTableRegistryImpl implements EventTableRegistry {
	private static final String COMMITTER = "kraken-eventstorage";

	@Requires
	private ConfigService conf;
	private AtomicInteger nextTableId;
	private ConcurrentMap<Integer, String> tableNames;
	private ConcurrentMap<String, EventTableSchema> tableSchemas;

	public EventTableRegistryImpl() {
		this.tableSchemas = new ConcurrentHashMap<String, EventTableSchema>();
		this.tableNames = new ConcurrentHashMap<Integer, String>();

		ConfigDatabase db = getDatabase();
		ConfigCollection col = db.ensureCollection(EventTableSchema.class);

		int maxId = 0;
		ConfigIterator it = col.findAll();
		for (EventTableSchema t : it.getDocuments(EventTableSchema.class)) {
			tableNames.put(t.getId(), t.getName());
			tableSchemas.put(t.getName(), t);
			if (maxId < t.getId())
				maxId = t.getId();
		}
		this.nextTableId = new AtomicInteger(maxId + 1);
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
		return getTableSchema(tableName).getId();
	}

	@Override
	public String getTableName(int tableId) {
		return tableNames.get(tableId);
	}

	@Override
	public int createTable(String tableName, Map<String, String> tableMetadata) {
		if (tableSchemas.containsKey(tableName))
			throw new IllegalStateException("table already exists: " + tableName);

		int newId = nextTableId.getAndIncrement();
		EventTableSchema table = new EventTableSchema(newId, tableName);
		if (tableMetadata != null)
			table.getMetadata().putAll(tableMetadata);

		ConfigDatabase db = getDatabase();
		db.add(table, COMMITTER, "create table [" + tableName + "]");

		tableNames.put(table.getId(), table.getName());
		tableSchemas.put(tableName, table);

		return newId;
	}

	@Override
	public void renameTable(String currentName, String newName) {
		if (tableSchemas.containsKey(newName))
			throw new IllegalStateException("table already exists: " + newName);

		ConfigDatabase db = getDatabase();
		Config c = getConfig(db, currentName);

		EventTableSchema schema = c.getDocument(EventTableSchema.class);
		schema.setName(newName);
		db.update(c, schema, false, COMMITTER, "rename table [" + currentName + "] to [" + newName + "]");

		tableSchemas.remove(currentName);
		tableSchemas.putIfAbsent(newName, schema);
		tableNames.put(schema.getId(), newName);
	}

	@Override
	public void dropTable(String tableName) {
		getTableSchema(tableName);

		ConfigDatabase db = getDatabase();
		db.remove(getConfig(db, tableName), false, COMMITTER, "drop table [" + tableName + "]");

		EventTableSchema t = tableSchemas.remove(tableName);
		if (t != null)
			tableNames.remove(t.getId());
	}

	@Override
	public Set<String> getTableMetadataKeys(String tableName) {
		EventTableSchema t = getTableSchema(tableName);
		return t.getMetadata().keySet();
	}

	@Override
	public String getTableMetadata(String tableName, String key) {
		EventTableSchema t = getTableSchema(tableName);
		return (String) t.getMetadata().get(key);
	}

	@Override
	public void setTableMetadata(String tableName, String key, String value) {
		ConfigDatabase db = getDatabase();
		Config c = getConfig(db, tableName);
		EventTableSchema t = getTableSchema(tableName);
		t.getMetadata().put(key, value);
		db.update(c, t, false, COMMITTER, "set table [" + tableName + "] metadata [" + key + "] to [" + value + "]");
	}

	@Override
	public void unsetTableMetadata(String tableName, String key) {
		ConfigDatabase db = getDatabase();
		Config c = getConfig(db, tableName);
		EventTableSchema t = getTableSchema(tableName);
		t.getMetadata().remove(key);
		db.update(c, t, false, COMMITTER, "unset table [" + tableName + "] metadata [" + key + "]");
	}

	private EventTableSchema getTableSchema(String tableName) {
		EventTableSchema t = tableSchemas.get(tableName);
		if (t == null)
			throw new EventTableNotFoundException(tableName);
		return t;
	}

	private ConfigDatabase getDatabase() {
		return conf.ensureDatabase("kraken-eventstorage");
	}

	private Config getConfig(ConfigDatabase db, String tableName) {
		Config config = db.findOne(EventTableSchema.class, Predicates.field("name", tableName));
		if (config == null)
			throw new EventTableNotFoundException(tableName);
		return config;
	}
}
