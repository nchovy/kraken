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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileManifest implements Manifest {
	private int version;

	private int id;

	private Map<Integer, CollectionEntry> colMap = new TreeMap<Integer, CollectionEntry>();

	// col id -> (doc id -> entry) map
	private Map<Integer, Map<Integer, ConfigEntry>> configMap = new TreeMap<Integer, Map<Integer, ConfigEntry>>();

	public FileManifest duplicate() {
		FileManifest dup = new FileManifest();

		dup.id = id;
		dup.version = version;
		dup.colMap = new TreeMap<Integer, CollectionEntry>(colMap);
		dup.configMap = new TreeMap<Integer, Map<Integer, ConfigEntry>>();

		for (Integer key : configMap.keySet()) {
			Map<Integer, ConfigEntry> value = configMap.get(key);
			dup.configMap.put(key, new TreeMap<Integer, ConfigEntry>(value));
		}

		return dup;
	}

	public static void upgradeManifest(FileManifest manifest, File dbDir) {
		final Logger logger = LoggerFactory.getLogger(FileManifest.class);
		for (String name : manifest.getCollectionNames()) {
			CollectionEntry col = manifest.getCollectionEntry(name);
			File logFile = new File(dbDir, "col" + col.getId() + ".log");
			File datFile = new File(dbDir, "col" + col.getId() + ".dat");

			if (!logFile.exists() || !datFile.exists())
				continue;

			RevLogReader reader = null;
			try {
				reader = new RevLogReader(logFile, datFile);
				// build map
				Map<ConfigEntry, Long> indexMap = new HashMap<ConfigEntry, Long>();
				long count = reader.count();
				for (long i = 0; i < count; i++) {
					RevLog revLog = reader.read(i);
					indexMap.put(new ConfigEntry(col.getId(), revLog.getDocId(), revLog.getRev()), i);
				}

				for (ConfigEntry c : manifest.getConfigEntries(col.getName())) {
					Long index = indexMap.get(c);
					if (index != null) {
						c.setIndex(index.intValue());
						logger.debug("kraken confdb: set index for " + c);
					} else
						logger.warn("kraken confdb: cannot find index for " + c);
				}
			} catch (IOException e) {
				logger.error("kraken confdb: cannot upgrade format", e);
			} finally {
				if (reader != null)
					reader.close();
			}
		}
	}

	public static Manifest writeManifest(Manifest manifest, File manifestLogFile, File manifestDatFile) throws IOException {
		RevLogWriter writer = null;
		try {
			writer = new RevLogWriter(manifestLogFile, manifestDatFile);
			return writeManifest(manifest, writer);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	public static Manifest writeManifest(Manifest manifest, RevLogWriter writer) throws IOException {
		RevLog log = new RevLog();
		log.setRev(1);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(manifest.serialize());

		manifest.setId(writer.write(log));
		return manifest;

	}

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

		if (m.size() > 65535)
			throw new IllegalStateException("cannot add more than 65535 docs");
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
		FileManifestCodec codec = new FileManifestCodec();
		int len = EncodingRule.lengthOf(this, codec);
		ByteBuffer bb = ByteBuffer.allocate(len);
		EncodingRule.encode(bb, this, codec);
		return bb.array();
	}

	public static FileManifest deserialize(byte[] b) {
		return deserialize(b, false);
	}

	public static FileManifest deserialize(byte[] b, boolean noConfigs) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Object doc = EncodingRule.decode(bb, new FileManifestCodec());
		if (doc instanceof FileManifest)
			return (FileManifest) doc;

		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) doc;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + version;
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
		FileManifest other = (FileManifest) obj;
		if (id != other.id)
			return false;
		if (version != other.version)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String manifest = "version=" + getVersion() + ", id=" + getId() + "\n" + "collections\n";

		manifest += "-------------------------------\n";
		if (colMap != null) {
			for (CollectionEntry c : colMap.values()) {
				manifest += c.toString() + ", count=" + configMap.get(c.getId()).values().size() + "\n";
			}
		}

		return manifest;
	}
}
