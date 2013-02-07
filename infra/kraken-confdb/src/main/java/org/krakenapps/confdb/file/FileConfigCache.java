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

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCache;

public class FileConfigCache implements ConfigCache {
	// collection name to id maping cache
	private ConcurrentMap<String, Integer> nameMap;

	// collection id to config cache map
	private ConcurrentMap<Integer, WeakReference<ConcurrentMap<ConfigKey, Config>>> cache;

	private FileConfigDatabase db;

	public FileConfigCache(FileConfigDatabase db) {
		this.db = db;
		this.nameMap = new ConcurrentHashMap<String, Integer>();
		this.cache = new ConcurrentHashMap<Integer, WeakReference<ConcurrentMap<ConfigKey, Config>>>();
	}

	/**
	 * CAUTION: you should NOT MODIFY config object. mutable for iterator
	 * performance
	 */
	@Override
	public Config findEntry(String colName, int manifestId, int docId, long rev) {
		Integer colId = getCollectionId(colName);
		if (colId == null)
			return null;

		WeakReference<ConcurrentMap<ConfigKey, Config>> ref = cache.get(colId);
		if (ref == null)
			return null;

		ConcurrentMap<ConfigKey, Config> map = ref.get();
		if (map == null)
			return null;

		Config c = map.get(new ConfigKey(manifestId, docId, rev));
		if (c != null) {
			return c;
		}
		return null;
	}

	@Override
	public void putEntry(String colName, int manifestId, Config c) {
		Integer colId = getCollectionId(colName);
		if (colId == null)
			return;

		WeakReference<ConcurrentMap<ConfigKey, Config>> ref = cache.get(colId);
		ConcurrentMap<ConfigKey, Config> configCache = null;
		if (ref == null) {
			configCache = createCollectionCache(colId);
		} else {
			configCache = ref.get();
			if (configCache == null)
				configCache = createCollectionCache(colId);
		}

		configCache.put(new ConfigKey(manifestId, c.getId(), c.getRevision()), c);
	}

	private Integer getCollectionId(String colName) {
		Integer colId = nameMap.get(colName);
		if (colId == null) {
			colId = db.getCollectionId(colName);
			if (colId != null)
				nameMap.put(colName, colId);
		}
		return colId;
	}

	private ConcurrentMap<ConfigKey, Config> createCollectionCache(int colId) {
		ConcurrentMap<ConfigKey, Config> configCache = new ConcurrentHashMap<FileConfigCache.ConfigKey, Config>();
		WeakReference<ConcurrentMap<ConfigKey, Config>> ref = new WeakReference<ConcurrentMap<ConfigKey, Config>>(configCache);
		cache.put(colId, ref);
		return configCache;
	}

	private static class ConfigKey {
		private int manifestId;
		private int id;
		private long rev;

		public ConfigKey(int manifestId, int id, long rev) {
			this.manifestId = manifestId;
			this.id = id;
			this.rev = rev;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + manifestId;
			result = prime * result + (int) (rev ^ (rev >>> 32));
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
			ConfigKey other = (ConfigKey) obj;
			if (id != other.id)
				return false;
			if (manifestId != other.manifestId)
				return false;
			if (rev != other.rev)
				return false;
			return true;
		}
	}
}
