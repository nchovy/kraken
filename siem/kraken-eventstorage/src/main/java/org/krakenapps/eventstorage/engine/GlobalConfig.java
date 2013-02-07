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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;

public class GlobalConfig {
	public static enum Key {
		StorageDirectory("storage_directory", String.class), //
		CheckInterval("check_interval", Integer.class, 1000), // 1sec
		FlushInterval("flush_interval", Integer.class, 60000), // 1min
		MaxIdleTime("max_idle_time", Integer.class, 300000), // 5min
		NextEventId("next_event_id", Map.class, new HashMap<String, String>()); //

		static {
			File storageDir = new File(System.getProperty("kraken.data.dir"), "kraken-eventstorage/log/");
			StorageDirectory.initialValue = storageDir.getAbsolutePath();
		}

		private String name;
		private Class<?> type;
		private Object initialValue;

		private Key(String name, Class<?> type) {
			this(name, type, null);
		}

		private Key(String name, Class<?> type, Object initialValue) {
			this.name = name;
			this.type = type;
			this.initialValue = initialValue;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}

		public Object getInitialValue() {
			return initialValue;
		}
	}

	private static final String COMMITTER = "kraken-eventstorage";

	public static Object get(ConfigService confsvc, Key key) {
		Config c = getGlobalConfig(getCollection(confsvc));
		if (c == null)
			return null;

		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) c.getDocument();
		Object result = m.get(key.getName());
		return (result != null) ? result : key.getInitialValue();
	}

	public static void set(ConfigService confsvc, Key key, Object value) {
		set(confsvc, key, value, false);
	}

	public static void set(ConfigService confsvc, Key key, Object value, boolean simpleLog) {
		if (!key.getType().isAssignableFrom(value.getClass()))
			throw new IllegalArgumentException("invalid value type");

		ConfigCollection col = getCollection(confsvc);
		Config c = getGlobalConfig(col);

		@SuppressWarnings("unchecked")
		Map<String, Object> doc = (c != null) ? ((Map<String, Object>) c.getDocument()) : new HashMap<String, Object>();
		Object before = doc.get(key.getName());
		if (before != null && before.equals(value))
			return;
		doc.put(key.getName(), value);

		if (c == null) {
			String log = "add new global config";
			if (!simpleLog)
				log += String.format(" [%s] to [%s]", key, value);
			col.add(doc, COMMITTER, log);
		} else {
			String log = "update global config";
			if (!simpleLog)
				log += String.format(" [%s] to [%s] (before value [%s])", key, value, before);
			col.update(c, false, COMMITTER, log);
		}
	}

	public static void unset(ConfigService confsvc, Key key) {
		ConfigCollection col = getCollection(confsvc);
		Config c = getGlobalConfig(col);

		@SuppressWarnings("unchecked")
		Map<String, Object> doc = (c != null) ? ((Map<String, Object>) c.getDocument()) : new HashMap<String, Object>();
		if (doc.get(key.getName()) == null)
			return;
		doc.remove(key.getName());

		String log = String.format("unset global config [%s]", key);
		col.update(c, false, COMMITTER, log);
	}

	private static ConfigCollection getCollection(ConfigService confsvc) {
		ConfigDatabase db = confsvc.ensureDatabase("kraken-eventstorage");
		return db.ensureCollection("global_configs");
	}

	private static Config getGlobalConfig(ConfigCollection col) {
		ConfigIterator it = col.findAll();
		try {
			if (!it.hasNext())
				return null;
			return it.next();
		} finally {
			it.close();
		}
	}
}
