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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.api.PrimitiveParseCallback;
import org.krakenapps.codec.UnsupportedTypeException;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigParser;

class FileConfig implements Config {
	private ConfigDatabase db;
	private ConfigCollection col;
	private int id;
	private long rev;
	private long prevRev;
	private Object doc;
	private ConfigParser parser;

	public FileConfig(ConfigDatabase db, ConfigCollection col, int id, long rev, long prevRev, Object doc) {
		this(db, col, id, rev, prevRev, doc, null);
	}

	public FileConfig(ConfigDatabase db, ConfigCollection col, int id, long rev, long prevRev, Object doc, ConfigParser parser) {
		this.db = db;
		this.col = col;
		this.id = id;
		this.rev = rev;
		this.prevRev = prevRev;
		this.doc = doc;
		this.parser = parser;
	}

	@Override
	public ConfigDatabase getDatabase() {
		return db;
	}

	@Override
	public ConfigCollection getCollection() {
		return col;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public long getRevision() {
		return rev;
	}

	@Override
	public long getPrevRevision() {
		return prevRev;
	}

	@Override
	public Object getDocument() {
		return doc;
	}

	@Override
	public <T> T getDocument(Class<T> cls) {
		return getDocument(cls, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getDocument(Class<T> cls, PrimitiveParseCallback callback) {
		if (parser != null) {
			Object o = parser.parse(doc, callback);
			if (o == null || cls.isAssignableFrom(o.getClass()))
				return (T) o;
		}
		return PrimitiveConverter.parse(cls, doc, callback);
	}

	@Override
	public void setDocument(Object doc) {
		this.doc = doc;
	}

	@Override
	public void update() {
		col.update(this);
	}

	@Override
	public void remove() {
		col.remove(this);
	}

	@Override
	public String toString() {
		return "id=" + id + ", rev=" + rev + ", prev=" + prevRev + ", doc=" + doc;
	}

	@Override
	public Config duplicate() {
		return new FileConfig(db, col, id, rev, prevRev, duplicateDoc(doc));
	}

	@SuppressWarnings("unchecked")
	private Object duplicateDoc(Object doc) {
		if (doc == null)
			return null;

		if (doc instanceof Map) {
			Map<String, Object> m = (Map<String, Object>) doc;
			HashMap<String, Object> n = new HashMap<String, Object>(m.size());

			for (Entry<String, Object> pair : m.entrySet()) {
				Object v = pair.getValue();
				// fast path
				if (v == null)
					n.put(pair.getKey(), null);
				else if (v.getClass() == String.class)
					n.put(pair.getKey(), v);
				else
					n.put(pair.getKey(), duplicateDoc(v));
			}

			return n;
		} else if (doc instanceof Collections) {
			Collection<Object> c = (Collection<Object>) doc;
			ArrayList<Object> n = new ArrayList<Object>(c.size());

			for (Object o : c)
				n.add(duplicateDoc(o));

			return n;

		} else if (doc.getClass().isArray()) {
			Class c = doc.getClass().getComponentType();
			if (c == byte.class) {
				byte[] arr = (byte[]) doc;
				byte[] n = new byte[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else if (c == int.class) {
				int[] arr = (int[]) doc;
				int[] n = new int[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;

			} else if (c == double.class) {
				double[] arr = (double[]) doc;
				double[] n = new double[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else if (c == float.class) {
				float[] arr = (float[]) doc;
				float[] n = new float[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else if (c == boolean.class) {
				boolean[] arr = (boolean[]) doc;
				boolean[] n = new boolean[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else if (c == char.class) {
				throw new UnsupportedTypeException("unsupported data type [" + c.getName() + "]");
			} else if (c == long.class) {
				long[] arr = (long[]) doc;
				long[] n = new long[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else if (c == short.class) {
				short[] arr = (short[]) doc;
				short[] n = new short[arr.length];
				n = Arrays.copyOf(arr, arr.length);

				return n;
			} else {
				Object[] os = (Object[]) doc;
				Object[] n = new Object[os.length];

				for (int i = 0; i < os.length; i++)
					n[i] = duplicateDoc(os[i]);

				return n;
			}
		} else
			return doc;
	}
}
