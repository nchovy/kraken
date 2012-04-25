package org.krakenapps.confdb.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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

	@SuppressWarnings("unchecked")
	public void importData(InputStream is) throws IOException, ParseException {
		logger.debug("kraken confdb: start import data");
		db.lock();

		try {
			JSONObject jsonObject = new JSONObject(getJsonString(is));
			Map<String, Object> collections = (Map<String, Object>) parse(jsonObject.optJSONObject("collections"));

			Manifest manifest = db.getManifest(null);
			List<ConfigChange> configChanges = new ArrayList<ConfigChange>();

			for (String colName : collections.keySet()) {
				CollectionEntry collectionEntry = checkCollectionEntry(manifest, colName);
				manifest.add(collectionEntry);
				List<Object> docs = (List<Object>) removeType((List<Object>) collections.get(colName));

				for (Object o : docs) {
					ConfigEntry configEntry = writeConfigEntry(o, collectionEntry.getId());
					configChanges.add(new ConfigChange(CommitOp.CreateDoc, colName, collectionEntry.getId(),
							configEntry.getDocId()));
					manifest.add(configEntry);
				}

			}

			writeManifestLog(manifest);
			writeChangeLog(configChanges, manifest.getId());
			logger.debug("kraken confdb: import complete");
		} catch (JSONException e) {
			throw new ParseException(e.getMessage(), 0);
		} finally {
			db.unlock();
		}
	}

	private String getJsonString(InputStream is) throws IOException {
		BufferedReader in = null;
		StringBuilder json = null;

		in = new BufferedReader(new InputStreamReader(is, "utf-8"));

		json = new StringBuilder();
		String line = null;

		while ((line = in.readLine()) != null) {
			json.append(line);
		}

		return json.toString();
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

	private ConfigEntry writeConfigEntry(Object doc, int collectionId) throws IOException {

		RevLogWriter writer = null;
		try {
			File logFile = new File(db.getDbDirectory(), "col" + collectionId + ".log");
			File datFile = new File(db.getDbDirectory(), "col" + collectionId + ".dat");

			writer = new RevLogWriter(logFile, datFile);

			ByteBuffer bb = encodeDocument(doc);
			RevLog log = new RevLog();
			log.setDoc(bb.array());
			log.setOperation(CommitOp.CreateDoc);
			int docId = writer.write(log);
			int index = writer.count() - 1;

			return new ConfigEntry(collectionId, docId, 0, index);
		} finally {
			if (writer != null)
				writer.close();
		}
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
