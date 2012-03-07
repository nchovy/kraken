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
import java.util.TreeMap;
import java.util.TreeSet;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.Manifest;

class FileManifest implements Manifest {
	private int version;

	private int id;

	private Map<Integer, CollectionEntry> colMap = new TreeMap<Integer, CollectionEntry>();

	// col id -> (doc id -> entry) map
	private Map<Integer, Map<Integer, ConfigEntry>> configMap = new TreeMap<Integer, Map<Integer, ConfigEntry>>();

	public FileManifest() {
	}

	@Override
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
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
		colMap.put(e.getId(), e);
		configMap.put(e.getId(), new TreeMap<Integer, ConfigEntry>());
	}

	@Override
	public void remove(CollectionEntry e) {
		colMap.remove(e.getId());
		configMap.remove(e.getId());
	}

	@Override
	public void add(ConfigEntry e) {
		Map<Integer, ConfigEntry> m = configMap.get(e.getColId());
		if (m == null)
			throw new IllegalStateException("col not found: " + e.getColId());

		m.put(e.getDocId(), e);
	}

	@Override
	public void remove(ConfigEntry e) {
		Map<Integer, ConfigEntry> m = configMap.get(e.getColId());
		if (m == null)
			throw new IllegalStateException("col not found: " + e.getColId());

		m.remove(e.getDocId());
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
		for (CollectionEntry e : colMap.values())
			if (e.getName().equals(name))
				return e;

		return null;
	}

	@Override
	public List<ConfigEntry> getConfigEntries(String colName) {
		int colId = getCollectionId(colName);
		Map<Integer, ConfigEntry> m = configMap.get(colId);
		if (m == null)
			throw new IllegalStateException("col not found: " + colName);

		return new ArrayList<ConfigEntry>(m.values());
	}

	public String getCollectionName(int id) {
		CollectionEntry e = colMap.get(id);
		if (e == null)
			return null;
		return e.getName();
	}

	@Override
	public boolean containsDoc(String colName, int docId, long rev) {
		CollectionEntry col = getCollectionEntry(colName);
		Map<Integer, ConfigEntry> m = configMap.get(col.getId());
		if (m == null)
			throw new IllegalStateException("col not found: " + colName);

		ConfigEntry ce = m.get(docId);
		return ce != null && ce.getRev() == rev;
	}

	@Override
	public byte[] serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("ver", 2);
		m.put("cols", serializeCols());

		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOfMap(m));
		EncodingRule.encodeMap(bb, m);
		return bb.array();
	}

	private List<Object> serializeCols() {
		List<Object> cols = new ArrayList<Object>(colMap.size());
		for (CollectionEntry col : colMap.values()) {
			List<Object> configs = new LinkedList<Object>();
			Map<Integer, ConfigEntry> m = configMap.get(col.getId());
			for (ConfigEntry c : m.values()) {
				configs.add(new Object[] { c.getDocId(), c.getRev(), c.getIndex() });
			}
			cols.add(new Object[] { col.getId(), col.getName(), configs });
		}
		return cols;
	}

	public static FileManifest deserialize(byte[] b) {
		return deserialize(b, false);
	}

	public static FileManifest deserialize(byte[] b, boolean noConfigs) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);
		FileManifest manifest = new FileManifest();
		if (m.containsKey("ver"))
			manifest.setVersion((Integer) m.get("ver"));
		else
			manifest.setVersion(1);

		// manual coding instead of primitive converter for performance
		for (Object o : (Object[]) m.get("cols")) {
			if (o instanceof Map) {
				// legacy format support
				@SuppressWarnings("unchecked")
				Map<String, Object> col = (Map<String, Object>) o;
				manifest.add(new CollectionEntry((Integer) col.get("id"), (String) col.get("name")));
			} else {
				Object[] arr = (Object[]) o;

				if (manifest.getVersion() == 1) {
					manifest.add(new CollectionEntry((Integer) arr[0], (String) arr[1]));
				} else if (manifest.getVersion() == 2) {
					int colId = (Integer) arr[0];
					manifest.add(new CollectionEntry(colId, (String) arr[1]));
					for (Object o2 : (Object[]) arr[2]) {
						Object[] config = (Object[]) o2;
						manifest.add(new ConfigEntry(colId, (Integer) config[0], (Long) config[1], (Integer) config[2]));
					}
				}
			}
		}

		// for ensureCollection() acceleration
		if (noConfigs)
			return manifest;

		if (manifest.getVersion() == 1) {
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
		}

		return manifest;
	}

	@Override
	public Set<String> getCollectionNames() {
		Set<String> names = new TreeSet<String>();
		for (CollectionEntry e : colMap.values())
			names.add(e.getName());
		return names;
	}
}
