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

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;

public class FileConfigService implements ConfigService {
	private File baseDir;
	private ConcurrentMap<String, ConfigDatabase> instances;
	private ConfigDatabase metadb;

	public FileConfigService() throws IOException {
		baseDir = new File(System.getProperty("kraken.data.dir"), "kraken-confdb");
		baseDir.mkdirs();
		metadb = new FileConfigDatabase(baseDir, "confdb");
		instances = new ConcurrentHashMap<String, ConfigDatabase>();
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
		try {
			ConfigCollection col = metadb.ensureCollection("database");
			Config c = col.findOne(Predicates.field("name", name));
			if (c == null)
				return null;

			ConfigDatabase db = new FileConfigDatabase(baseDir, name);
			ConfigDatabase old = instances.putIfAbsent(name, db);
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
	public void createDatabase(String name) {
		try {
			ConfigCollection col = metadb.ensureCollection("database");
			Config c = col.findOne(Predicates.field("name", name));
			if (c != null)
				throw new IllegalStateException("db already exists: " + name);

			DatabaseMetadata meta = new DatabaseMetadata(name);
			col.add(PrimitiveConverter.serialize(meta));

			ConfigDatabase db = new FileConfigDatabase(baseDir, name);
			instances.putIfAbsent(name, db);
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

			FileConfigDatabase db = (FileConfigDatabase) instances.remove(name);

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
}
