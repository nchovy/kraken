package org.krakenapps.confdb.file;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCache;

public class FileConfigCache implements ConfigCache {
	// collection name to id maping cache
	private ConcurrentMap<String, Integer> nameMap;

	// collection id to config cache map
	private ConcurrentMap<Integer, SoftReference<ConcurrentMap<ConfigKey, Config>>> cache;

	private FileConfigDatabase db;

	public FileConfigCache(FileConfigDatabase db) {
		this.db = db;
		this.nameMap = new ConcurrentHashMap<String, Integer>();
		this.cache = new ConcurrentHashMap<Integer, SoftReference<ConcurrentMap<ConfigKey, Config>>>();
	}

	@Override
	public Config findEntry(String colName, int docId, long rev) {
		Integer colId = getCollectionId(colName);
		if (colId == null)
			return null;

		SoftReference<ConcurrentMap<ConfigKey, Config>> ref = cache.get(colId);
		if (ref == null)
			return null;

		ConcurrentMap<ConfigKey, Config> map = ref.get();
		if (map == null)
			return null;

		Config c = map.get(new ConfigKey(docId, rev));
		if (c != null) {
			return c.duplicate();
		}
		return null;
	}

	@Override
	public void putEntry(String colName, Config c) {
		Integer colId = getCollectionId(colName);
		if (colId == null)
			return;

		SoftReference<ConcurrentMap<ConfigKey, Config>> ref = cache.get(colId);
		ConcurrentMap<ConfigKey, Config> configCache = null;
		if (ref == null) {
			configCache = createCollectionCache(colId);
		} else {
			configCache = ref.get();
			if (configCache == null)
				configCache = createCollectionCache(colId);
		}

		configCache.put(new ConfigKey(c.getId(), c.getRevision()), c);
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
		SoftReference<ConcurrentMap<ConfigKey, Config>> ref = new SoftReference<ConcurrentMap<ConfigKey, Config>>(configCache);
		cache.put(colId, ref);
		return configCache;
	}

	private static class ConfigKey {
		private int id;
		private long rev;

		public ConfigKey(int id, long rev) {
			this.id = id;
			this.rev = rev;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
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
			if (rev != other.rev)
				return false;
			return true;
		}
	}
}
