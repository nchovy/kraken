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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.krakenapps.codec.Base64;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.CollectionEntry;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.ConfigChange;
import org.krakenapps.confdb.ConfigEntry;
import org.krakenapps.confdb.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Importer {
	private final Logger logger = LoggerFactory.getLogger(Importer.class.getName());
	FileConfigDatabase db;

	public Importer(FileConfigDatabase db) {
		this.db = db;
	}

	public void importData(InputStream is) throws IOException, ParseException {
		if (is == null)
			throw new IllegalArgumentException("import input stream cannot be null");
		
		logger.debug("kraken confdb: start import data");
		db.lock();

		try {
			JSONTokener t = new JSONTokener(new InputStreamReader(is, Charset.forName("utf-8")));

			Map<String, Object> metadata = parseMetadata(t);
			Integer version = (Integer) metadata.get("version");
			if (version != 1)
				throw new ParseException("unsupported confdb data format version: " + version, -1);

			Manifest manifest = db.getManifest(null);
			List<ConfigChange> configChanges = new ArrayList<ConfigChange>();

			char comma = t.nextClean();
			if (comma == ',')
				parseCollections(t, manifest, configChanges);

			writeManifestLog(manifest);
			writeChangeLog(configChanges, manifest.getId());
			logger.debug("kraken confdb: import complete");
		} catch (JSONException e) {
			throw new ParseException(e.getMessage(), 0);
		} finally {
			db.unlock();
		}
	}

	private void parseCollections(JSONTokener t, Manifest manifest, List<ConfigChange> configChanges) throws JSONException,
			ParseException, IOException {
		Object key = t.nextValue();
		if (!key.equals("collections"))
			throw new ParseException("collections should be placed after metadata: token is " + key, -1);

		// "collections":{"COLNAME":["list",[...]]}
		t.nextClean(); // :
		t.nextClean(); // {

		if (t.nextClean() == '}')
			return;
		t.back();

		int i = 0;
		List<String> importColNames = new ArrayList<String>();
		while (true) {
			if (i++ != 0)
				t.nextClean();

			String colName = (String) t.nextValue();
			importColNames.add(colName);
			CollectionEntry collectionEntry = checkCollectionEntry(manifest, colName);
			manifest.add(collectionEntry);

			t.nextTo('[');
			t.nextClean();

			// type token (should be 'list')
			t.nextValue();
			t.nextTo("[");
			t.nextClean();

			// check empty config list
			char c = t.nextClean();
			if (c == ']') {
				t.nextClean(); // last ']'
				char marker = t.nextClean(); // ',' or '}'
				if (marker == '}')
					break;
				else
					t.back();

				continue;
			}

			t.back();

			int collectionId = collectionEntry.getId();
			RevLogWriter writer = null;
			try {
				File logFile = new File(db.getDbDirectory(), "col" + collectionId + ".log");
				File datFile = new File(db.getDbDirectory(), "col" + collectionId + ".dat");

				writer = new RevLogWriter(logFile, datFile);

				while (true) {
					@SuppressWarnings("unchecked")
					Object doc = removeType((List<Object>) parse((JSONArray) t.nextValue()));
					ConfigEntry configEntry = writeConfigEntry(writer, doc, collectionId);
					configChanges.add(new ConfigChange(CommitOp.CreateDoc, colName, collectionEntry.getId(), configEntry
							.getDocId()));
					manifest.add(configEntry);

					// check next list item
					char delimiter = t.nextClean();
					if (delimiter == ']')
						break;
				}
			} finally {
				if (writer != null)
					writer.close();
			}

			// end of list
			t.nextClean();

			char delimiter = t.nextClean();
			if (delimiter == '}')
				break;
		}

		for (String colName : db.getCollectionNames()) {
			if (importColNames.contains(colName))
				continue;
			configChanges.add(new ConfigChange(CommitOp.DropCol, colName, 0, 0));
			manifest.remove(new CollectionEntry(db.getCollectionId(colName), colName));
		}
	}

	private Map<String, Object> parseMetadata(JSONTokener x) throws JSONException, IOException {
		if (x.nextClean() != '{') {
			throw x.syntaxError("A JSONObject text must begin with '{'");
		}

		Object key = x.nextValue();
		if (!key.equals("metadata"))
			throw x.syntaxError("confdb metadata should be placed first");

		x.nextClean();
		return parse((JSONObject) x.nextValue());
	}

	private CollectionEntry checkCollectionEntry(Manifest manifest, String colName) {
		CollectionEntry collectionEntry = manifest.getCollectionEntry(colName);
		if (collectionEntry == null) {
			int collectionId = db.nextCollectionId();
			collectionEntry = new CollectionEntry(collectionId, colName);
		}

		return collectionEntry;
	}

	private void writeChangeLog(List<ConfigChange> configChanges, int manifestId) throws IOException {
		File changeLogFile = new File(db.getDbDirectory(), "changeset.log");
		File changeDatFile = new File(db.getDbDirectory(), "changeset.dat");
		RevLogWriter changeLogWriter = null;
		try {
			changeLogWriter = new RevLogWriter(changeLogFile, changeDatFile);
			ChangeSetWriter.log(changeLogWriter, configChanges, manifestId, null, "import", new Date());
		} finally {
			if (changeLogWriter != null)
				changeLogWriter.close();
		}
	}

	private int writeManifestLog(Manifest newManifest) throws IOException {
		File manifestLogFile = new File(db.getDbDirectory(), "manifest.log");
		File manifestDatFile = new File(db.getDbDirectory(), "manifest.dat");
		int manifestId = 0;
		RevLogWriter manifestWriter = null;

		try {
			manifestWriter = new RevLogWriter(manifestLogFile, manifestDatFile);
			manifestId = FileManifest.writeManifest(newManifest, manifestWriter).getId();
		} finally {
			if (manifestWriter != null)
				manifestWriter.close();
		}
		return manifestId;
	}

	private ConfigEntry writeConfigEntry(RevLogWriter writer, Object doc, int collectionId) throws IOException {

		ByteBuffer bb = encodeDocument(doc);
		RevLog log = new RevLog();
		log.setDoc(bb.array());
		log.setRev(1);
		log.setOperation(CommitOp.CreateDoc);
		int docId = writer.write(log);
		int index = writer.count() - 1;

		return new ConfigEntry(collectionId, docId, 0, index);
	}

	private ByteBuffer encodeDocument(Object doc) {
		int len = EncodingRule.lengthOf(doc);
		ByteBuffer bb = ByteBuffer.allocate(len);
		EncodingRule.encode(bb, doc);
		return bb;
	}

	private Map<String, Object> parse(JSONObject jsonObject) throws IOException {
		Map<String, Object> m = new HashMap<String, Object>();
		String[] names = JSONObject.getNames(jsonObject);
		if (names == null)
			return m;

		for (String key : names) {
			try {
				Object value = jsonObject.get(key);
				if (value == JSONObject.NULL)
					value = null;
				else if (value instanceof JSONArray)
					value = parse((JSONArray) value);
				else if (value instanceof JSONObject)
					value = parse((JSONObject) value);

				m.put(key, value);
			} catch (JSONException e) {
				logger.error("kraken confdb: cannot parse json", e);
				throw new IOException(e);
			}
		}

		return m;
	}

	private Object parse(JSONArray jsonarray) throws IOException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < jsonarray.length(); i++) {
			try {
				Object o = jsonarray.get(i);
				if (o == JSONObject.NULL)
					list.add(null);
				else if (o instanceof JSONArray)
					list.add(parse((JSONArray) o));
				else if (o instanceof JSONObject)
					list.add(parse((JSONObject) o));
				else
					list.add(o);
			} catch (JSONException e) {
				logger.error("kraken confdb: cannot parse json", e);
				throw new IOException(e);
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private Object removeType(List<Object> l) throws ParseException {
		if (l == null)
			throw new ParseException("list can not be null", 0);

		if (l.size() != 2)
			throw new ParseException("list size should be 2", 0);

		String type = (String) l.get(0);
		Object value = l.get(1);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		try {

			if (type.equals("string")) {
				return (String) value;
			} else if (type.equals("int")) {
				return (Integer) value;
			} else if (type.equals("long")) {
				return Long.valueOf(value.toString());
			} else if (type.equals("bool")) {
				return (Boolean) value;
			} else if (type.equals("ip4")) {
				return (Inet4Address) Inet4Address.getByName((String) value);
			} else if (type.equals("ip6")) {
				return (Inet6Address) Inet6Address.getByName((String) value);
			} else if (type.equals("double")) {
				return (Double) value;
			} else if (type.equals("float")) {
				return Float.valueOf(value.toString());
			} else if (type.equals("date")) {
				return dateFormat.parse((String) value);
			} else if (type.equals("short")) {
				return Short.valueOf(value.toString());
			} else if (type.equals("map")) {
				Map<String, Object> m = (Map<String, Object>) value;

				for (String name : m.keySet()) {
					List<Object> v = (List<Object>) m.get(name);
					m.put(name, removeType(v));
				}

				return m;
			} else if (type.equals("list")) {
				List<Object> newList = new ArrayList<Object>();
				List<Object> values = (List<Object>) value;
				for (Object o : values)
					newList.add(removeType((List<Object>) o));

				return newList;
			} else if (type.equals("null")) {
				return null;
			} else if (type.equals("blob")) {
				String byteString = (String) value;

				return Base64.decode(byteString);
			} else {
				throw new IllegalArgumentException("unsupported value [" + value + "], type [" + type + "]");
			}
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("invalid host [" + value + "]", e);
		}
	}
}
