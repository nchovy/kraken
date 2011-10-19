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

class Manifest {
	@CollectionTypeHint(CollectionEntry.class)
	private List<CollectionEntry> cols = new ArrayList<CollectionEntry>();

	@CollectionTypeHint(ConfigEntry.class)
	private List<ConfigEntry> configs = new ArrayList<ConfigEntry>();
	
	public Manifest() {
	}

	public void add(CollectionEntry e) {
		cols.remove(e); // remove duplicates
		cols.add(e);
	}

	public void remove(CollectionEntry e) {
		cols.remove(e);
	}

	public void add(ConfigEntry e) {
		configs.remove(e); // remove duplicates
		configs.add(e);
	}

	public void remove(ConfigEntry e) {
		configs.remove(e);
	}

	/**
	 * generate next collection id (max + 1)
	 * 
	 * @return the next collection id
	 */
	public int nextCollectionId() {
		int max = 0;
		for (CollectionEntry e : cols)
			if (max < e.getId())
				max = e.getId();

		return max + 1;
	}

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

	public byte[] serialize() {
		Object o = PrimitiveConverter.serialize(this);
		ByteBuffer bb = ByteBuffer.allocate(EncodingRule.lengthOf(o));
		EncodingRule.encode(bb, o);
		return bb.array();
	}

	public static Manifest deserialize(byte[] b) {
		ByteBuffer bb = ByteBuffer.wrap(b);
		Map<String, Object> m = EncodingRule.decodeMap(bb);
		return PrimitiveConverter.parse(Manifest.class, m);
	}

	public Set<String> getCollectionNames() {
		Set<String> names = new TreeSet<String>();
		for (CollectionEntry e : cols)
			names.add(e.getName());
		return names;
	}
}
