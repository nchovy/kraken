/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.ConfigServiceListener;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileConfigService implements ConfigService {
	private final Logger logger = LoggerFactory.getLogger(FileConfigService.class.getName());
	private File baseDir;
	private ConcurrentMap<DatabaseCacheKey, ConfigDatabase> instances;
	private ConfigDatabase metadb;

	private CopyOnWriteArraySet<ConfigServiceListener> listeners;

	public FileConfigService() throws IOException {
		listeners = new CopyOnWriteArraySet<ConfigServiceListener>();
		baseDir = new File(System.getProperty("kraken.data.dir"), "kraken-confdb");
		baseDir.mkdirs();
		metadb = new FileConfigDatabase(baseDir, "confdb");
		instances = new ConcurrentHashMap<DatabaseCacheKey, ConfigDatabase>();
	}

	@Override
	public List<String> getDatabaseNames() {
		ConfigCollection col = metadb.ensureCollection("database");
		ConfigIterator it = col.findAll();

		List<String> names = new ArrayList<String>();
		while (it.hasNext()) {
			Config next = it.next();
			DatabaseMetadata meta = PrimitiveConverter.parse(DatabaseMetadata.class, next.getDocument());
			names.add(meta.name);
		}
		return names;
	}

	@Override
	public ConfigDatabase getDatabase(String name) {
		return getDatabase(name, null);
	}

	@Override
	public ConfigDatabase getDatabase(String name, Integer rev) {
		try {
			DatabaseCacheKey cacheKey = new DatabaseCacheKey(name, rev);
			ConfigDatabase db = instances.get(cacheKey);
			if (db != null)
				return db;

			ConfigCollection col = metadb.ensureCollection("database");
			Config c = col.findOne(Predicates.field("name", name));
			if (c == null)
				return null;

			db = new FileConfigDatabase(baseDir, name, rev);
			ConfigDatabase old = instances.putIfAbsent(cacheKey, db);
			return old != null ? old : db;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ConfigDatabase ensureDatabase(String name) {
		ConfigDatabase db = getDatabase(name);
		if (db == null)
			createDatabase(name);

		return getDatabase(name);
	}

	@Override
	public ConfigDatabase createDatabase(String name) {
		try {
			ConfigCollection col = metadb.ensureCollection("database");
			Config c = col.findOne(Predicates.field("name", name));
			if (c != null)
				throw new IllegalStateException("db already exists: " + name);

			DatabaseMetadata meta = new DatabaseMetadata(name);
			col.add(PrimitiveConverter.serialize(meta));

			ConfigDatabase db = new FileConfigDatabase(baseDir, name);
			instances.putIfAbsent(new DatabaseCacheKey(name, null), db);

			for (ConfigServiceListener listener : listeners) {
				try {
					listener.onCreateDatabase(db);
				} catch (Throwable t) {
					logger.error("kraken confdb: create config database callback should not throw any exception", t);
				}
			}
			return db;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dropDatabase(String name) {
		try {
			ConfigCollection col = metadb.ensureCollection("database");
			Config c = col.findOne(Predicates.field("name", name));
			if (c == null)
				throw new IllegalStateException("db not exists");

			col.remove(c);

			FileConfigDatabase db = (FileConfigDatabase) instances.remove(new DatabaseCacheKey(name, null));

			if (db == null)
				db = new FileConfigDatabase(baseDir, name);

			db.purge();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	private static class DatabaseMetadata {
		private String name;
		private Date created = new Date();

		public DatabaseMetadata() {
		}

		public DatabaseMetadata(String name) {
			this.name = name;
		}
	}

	private static class DatabaseCacheKey {
		private String name;
		private Integer rev;

		public DatabaseCacheKey(String name, Integer rev) {
			this.name = name;
			this.rev = rev;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((rev == null) ? 0 : rev.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DatabaseCacheKey other = (DatabaseCacheKey) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (rev == null) {
				if (other.rev != null)
					return false;
			} else if (!rev.equals(other.rev))
				return false;
			return true;
		}
	}

	@Override
	public void addListener(ConfigServiceListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should not be null");
		listeners.add(listener);
	}

	@Override
	public void removeListener(ConfigServiceListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener should not be null");
		listeners.remove(listener);
	}
}
