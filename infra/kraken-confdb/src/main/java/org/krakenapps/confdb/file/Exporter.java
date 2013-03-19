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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONWriter;
import org.krakenapps.codec.Base64;
import org.krakenapps.codec.UnsupportedTypeException;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exporter {
	private final Logger logger = LoggerFactory.getLogger(Exporter.class.getName());
	private FileConfigDatabase db;

	/**
	 * CAUTION: thread unsafe for performance reason.
	 */
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

	public Exporter(FileConfigDatabase db) {
		this.db = db;
	}

	public void exportData(OutputStream os) throws IOException {
		if (os == null)
			throw new IllegalArgumentException("export output stream cannot be null");
		
		logger.debug("kraken confdb: start export data");
		db.lock();
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os, Charset.forName("utf-8"));
			JSONWriter jw = new JSONWriter(writer);

			jw.object();

			jw.key("metadata");
			jw.object();
			jw.key("version").value(1);
			jw.key("date").value(sdf.format(new Date()));
			jw.endObject();

			jw.key("collections");
			jw.object();

			for (String name : db.getCollectionNames()) {
				ConfigCollection col = db.getCollection(name);

				// collection name
				jw.key(name);

				// typed doc list
				jw.array();

				jw.value("list");

				// doc list begin
				jw.array();

				ConfigIterator it = col.findAll();
				try {
					while (it.hasNext()) {
						Object doc = it.next().getDocument();
						jw.value(insertType(doc));
					}
				} finally {
					it.close();
				}

				// end of doc list
				jw.endArray();

				// end of typed list
				jw.endArray();
			}

			// end of collection
			jw.endObject();

			// end of master doc
			jw.endObject();
			writer.flush();
			logger.debug("kraken confdb: export complete");
		} catch (JSONException e) {
			throw new IOException(e);
		} finally {
			db.unlock();
		}
	}

	private Object insertType(Object doc) {
		if (doc instanceof String) {
			return createList("string", doc);
		} else if (doc instanceof Integer) {
			return createList("int", doc);
		} else if (doc instanceof Boolean) {
			return createList("bool", doc);
		} else if (doc instanceof Long) {
			return createList("long", doc);
		} else if (doc instanceof Inet4Address) {
			return createList("ip4", ((Inet4Address) doc).getHostAddress());
		} else if (doc instanceof Inet6Address) {
			return createList("ip6", ((Inet6Address) doc).getHostAddress());
		} else if (doc instanceof Double) {
			return createList("double", doc);
		} else if (doc instanceof Float) {
			return createList("float", doc);
		} else if (doc instanceof Date) {
			Date docDate = (Date) doc;
			return createList("date", sdf.format(docDate));
		} else if (doc instanceof Short) {
			return createList("short", doc);
		} else if (doc == null) {
			return createList("null", doc);
		} else if (doc instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) doc;

			for (String name : m.keySet()) {
				m.put(name, insertType(m.get(name)));
			}

			return createList("map", m);
		} else if (doc instanceof List) {
			@SuppressWarnings("unchecked")
			List<Object> l = (List<Object>) doc;
			List<Object> newList = new ArrayList<Object>();

			for (Object o : l) {
				newList.add(insertType(o));
			}

			return createList("list", newList);
		} else if (doc.getClass().isArray()) {
			return insertArrayType(doc);
		} else {
			throw new UnsupportedTypeException("unsupported value [" + doc + "], type [" + doc.getClass().getName() + "]");
		}
	}

	private List<Object> createList(String type, Object doc) {
		List<Object> l = new ArrayList<Object>(2);
		l.add(type);
		l.add(doc);

		return l;
	}

	private Object insertArrayType(Object doc) {
		Class<?> c = doc.getClass().getComponentType();
		if (c == byte.class) {
			byte[] arr = (byte[]) doc;
			return createList("blob", new String(Base64.encode(arr)));
		} else if (c == int.class || c == double.class || c == float.class || c == long.class || c == short.class
				|| c == boolean.class) {
			return createList("list", doc);
		} else if (c == char.class) {
			throw new UnsupportedTypeException("unsupported data type [" + c.getName() + "]");
		} else {
			Object[] os = (Object[]) doc;
			Object[] newOs = new Object[os.length];
			for (int index = 0; index < os.length; index++) {
				newOs[index] = insertType(os[index]);
			}

			return createList("list", newOs);
		}
	}
}
