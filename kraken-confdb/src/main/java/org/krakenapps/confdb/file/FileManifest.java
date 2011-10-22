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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.krakenapps.api.CollectionTypeHint;
import org.krakenapps.api.PrimitiveConverter;
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
	}

	@Override
	public void remove(ConfigEntry e) {
		configs.remove(e);
	}

	@Override
	public int getCollectionId(String name) {
		CollectionEntry e = getCollectionEntry(name);
		if (e == null)
			throw new IllegalArgumentException("collection not found:" + name);

		return e.getId();
	}

	@Override
	public CollectionEntry getCollectionEntry(String name) {
		for (CollectionEntry e : cols)
			if (e.getName().equals(name))
				return e;

		return null;
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

		for (ConfigEntry e : configs)
			if (e.getColId() == col.getId() && e.getDocId() == docId && e.getRev() == rev)
				return true;

		return false;
	}

	@Override
	public byte[] serialize() {
		Object o = PrimitiveConverter.serialize(this);
		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(o));
		EncodingRule.encode(bb, o);
		return bb.array();
	}

	public static FileManifest deserialize(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);
		return PrimitiveConverter.parse(FileManifest.class, m);
	}

	@Override
	public Set<String> getCollectionNames() {
		Set<String> names = new TreeSet<String>();
		for (CollectionEntry e : cols)
			names.add(e.getName());
		return names;
	}
}
