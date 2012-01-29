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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.FieldOption;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.Manifest;

class FileManifest implements Manifest {
	private int id;

	@CollectionTypeHint(CollectionEntry.class)
	private List<CollectionEntry> cols = new ArrayList<CollectionEntry>();

	@CollectionTypeHint(ConfigEntry.class)
	private List<ConfigEntry> configs = new ArrayList<ConfigEntry>();

	/**
	 * just for existence test. we cannot change configs type (need ordering)
	 */
	@FieldOption(skip = true)
	private Map<ConfigMatchKey, Long> tester = new HashMap<ConfigMatchKey, Long>();

	public FileManifest() {
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void add(CollectionEntry e) {
		cols.remove(e); // remove duplicates
		cols.add(e);
	}

	@Override
	public void remove(CollectionEntry e) {
		cols.remove(e);
	}

	@Override
	public void add(ConfigEntry e) {
		configs.remove(e); // remove duplicates
		configs.add(e);
		tester.put(new ConfigMatchKey(e), e.getRev());
	}

	@Override
	public void remove(ConfigEntry e) {
		configs.remove(e);
		tester.remove(new ConfigMatchKey(e));
	}

	@Override
	public int getCollectionId(String name) {
		CollectionEntry e = getCollectionEntry(name);
		if (e == null)
			throw new IllegalArgumentException("collection not found: " + name);

		return e.getId();
	}

	@Override
	public CollectionEntry getCollectionEntry(String name) {
		for (CollectionEntry e : cols)
			if (e.getName().equals(name))
				return e;

		return null;
	}

	@Override
	public List<ConfigEntry> getConfigEntries(String colName) {
		int colId = getCollectionId(colName);

		List<ConfigEntry> entries = new LinkedList<ConfigEntry>();
		for (ConfigEntry e : configs)
			if (e.getColId() == colId)
				entries.add(e);
		return entries;
	}

	public String getCollectionName(int id) {
		for (CollectionEntry e : cols)
			if (e.getId() == id)
				return e.getName();

		return null;
	}

	@Override
	public boolean containsDoc(String colName, int docId, long rev) {
		CollectionEntry col = getCollectionEntry(colName);
		ConfigMatchKey key = new ConfigMatchKey(col.getId(), docId);
		Long r = tester.get(key);
		return r != null && r == rev;
	}

	@Override
	public byte[] serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("cols", serializeCols());
		m.put("configs", serializeConfigs());

		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOfMap(m));
		EncodingRule.encodeMap(bb, m);
		return bb.array();
	}

	private List<Object> serializeCols() {
		List<Object> l = new ArrayList<Object>(cols.size());
		for (CollectionEntry e : cols)
			l.add(new Object[] { e.getId(), e.getName() });
		return l;
	}

	private List<Object> serializeConfigs() {
		List<Object> l = new ArrayList<Object>(configs.size());
		for (ConfigEntry e : configs)
			l.add(new Object[] { e.getColId(), e.getDocId(), e.getRev() });
		return l;
	}

	public static FileManifest deserialize(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);
		FileManifest manifest = new FileManifest();

		// manual coding instead of primitive converter for performance
		for (Object o : (Object[]) m.get("cols")) {
			if (o instanceof Map) {
				// legacy format support
				@SuppressWarnings("unchecked")
				Map<String, Object> col = (Map<String, Object>) o;
				manifest.add(new CollectionEntry((Integer) col.get("id"), (String) col.get("name")));
			} else {
				Object[] arr = (Object[]) o;
				manifest.add(new CollectionEntry((Integer) arr[0], (String) arr[1]));
			}
		}

		for (Object o : (Object[]) m.get("configs")) {
			if (o instanceof Map) {
				// legacy format support
				@SuppressWarnings("unchecked")
				Map<String, Object> c = (Map<String, Object>) o;
				manifest.add(new ConfigEntry((Integer) c.get("col_id"), (Integer) c.get("doc_id"), (Long) c.get("rev")));
			} else {
				Object[] arr = (Object[]) o;
				manifest.add(new ConfigEntry((Integer) arr[0], (Integer) arr[1], (Long) arr[2]));
			}
		}

		return manifest;
	}

	@Override
	public Set<String> getCollectionNames() {
		Set<String> names = new TreeSet<String>();
		for (CollectionEntry e : cols)
			names.add(e.getName());
		return names;
	}

	private static class ConfigMatchKey {
		private int colId;
		private int docId;

		public ConfigMatchKey(ConfigEntry e) {
			this(e.getColId(), e.getDocId());
		}

		public ConfigMatchKey(int colId, int docId) {
			this.colId = colId;
			this.docId = docId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + colId;
			result = prime * result + docId;
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
			ConfigMatchKey other = (ConfigMatchKey) obj;
			if (colId != other.colId)
				return false;
			if (docId != other.docId)
				return false;
			return true;
		}
	}
}
