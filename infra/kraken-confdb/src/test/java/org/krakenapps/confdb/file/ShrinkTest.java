/*
 * Copyright 2012 Future Systems
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

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;

public class ShrinkTest {
	private File workingDir;
	private FileConfigDatabase db;
	private FileConfigCollection col1;
	private FileConfigCollection col2;
	private ConfigService conf;

	@Before
	public void start() throws IOException {
		workingDir = new File(System.getProperty("user.dir"));
		this.db = new FileConfigDatabase(workingDir, "testdb");
		conf = new FileConfigService();
		conf.ensureDatabase(db.getName());

		this.col1 = (FileConfigCollection) db.ensureCollection("testcol1");
	}

	@After
	public void teardown() throws IOException {
		db.purge();
		for (File f : new File(workingDir, "testdb").listFiles()) {
			if (f.getName().startsWith("old_"))
				f.delete();
		}
	}

	// no exist manifest and changeset, import data
	@Test
	public void testEmptyImport() throws IOException, ParseException, JSONException {
		db = new FileConfigDatabase(workingDir, "testdb");

		col1 = (FileConfigCollection) db.ensureCollection("testcol1");
		col1.add(createObject(100));

		int rev = (int) db.getCommitLogs().get(0).getRev();
		assertEquals(2, db.getCommitCount());
		List<Object> oldDocs = getDocument("testcol1");
		export(rev);
		db.purge();
		importData(rev);
		List<Object> newDocs = getDocument("testcol1");

		for (int index = 0; index < oldDocs.size(); index++) {
			if (oldDocs.get(index) instanceof List)
				break;
			assertTrue(compareObject(oldDocs.get(index), newDocs.get(index)));
		}
		new File(db.getDbDirectory(), "export_" + rev + ".txt").delete();
	}

	// Exist manifest and changeset, import data
	@Test
	public void testExistImport() throws IOException, ParseException, JSONException {
		for (int id = 0; id < 10; id++)
			col1.add(createObject(id));

		List<CommitLog> logs = db.getCommitLogs();
		Comparator<CommitLog> comparator = new Comparator<CommitLog>() {
			@Override
			public int compare(CommitLog o1, CommitLog o2) {
				Long rev1 = o1.getRev();
				return rev1.compareTo(o2.getRev());
			}
		};

		assertEquals(11, db.getCommitCount());
		assertEquals(1, db.getManifest(null).getCollectionId("testcol1"));

		Collections.sort(logs, comparator);

		export((int) logs.get(logs.size() - 1).getRev());
		db.purge();

		this.col2 = (FileConfigCollection) db.ensureCollection("testcol2");
		col2.add(createObject(20));

		assertEquals(1, db.getCollectionNames().size());
		assertTrue(db.getCollectionNames().contains("testcol2"));
		assertFalse(db.getCollectionNames().contains("testcol1"));

		importData((int) logs.get(logs.size() - 1).getRev());

		assertEquals(1, db.getCollectionNames().size());
		assertTrue(db.getCollectionNames().contains("testcol1"));
		assertFalse(db.getCollectionNames().contains("testcol2"));
		assertEquals(2, db.getManifest(null).getCollectionId("testcol1"));

		importData((int) logs.get(logs.size() - 1).getRev());
		new File(db.getDbDirectory(), "export_" + logs.get(logs.size() - 1).getRev() + ".txt").delete();
	}

	@Test
	public void testCreateLogShrink() throws IOException {
		for (int id = 0; id < 10; id++)
			col1.add(createObject(id));

		assertEquals(11, db.getCommitCount());
		List<Object> oldDocs = getDocument("testcol1");
		db.shrink(1);
		assertEquals(1, db.getCommitCount());
		List<Object> newDocs = getDocument("testcol1");

		for (int index = 0; index < oldDocs.size(); index++) {
			if (oldDocs.get(index) instanceof List)
				break;
			assertTrue(compareObject(oldDocs.get(index), newDocs.get(index)));
		}

	}

	@Test
	public void testUpdateLogShrink() throws IOException {
		Config c = null;
		for (int id = 0; id < 10; id++)
			c = col1.add(createObject(id));

		c.setDocument(createObject(150));
		c.update();
		List<Object> oldDocs = getDocument("testcol1");
		assertEquals(12, db.getCommitCount());
		db.shrink(1);
		assertEquals(1, db.getCommitCount());

		List<Object> newDocs = getDocument("testcol1");

		for (int index = 0; index < oldDocs.size(); index++) {
			if (oldDocs.get(index) instanceof List)
				break;
			assertTrue(compareObject(oldDocs.get(index), newDocs.get(index)));
		}
	}

	@SuppressWarnings("unchecked")
	private boolean compareObject(Object oldDoc, Object newDoc) {
		Map<String, Object> oldMap = (Map<String, Object>) oldDoc;
		Map<String, Object> newMap = (Map<String, Object>) newDoc;

		if (!oldMap.get("int").equals(newMap.get("int")))
			return false;
		if (!oldMap.get("double").equals(newMap.get("double")))
			return false;
		if (!oldMap.get("float").equals(newMap.get("float")))
			return false;
		if (!oldMap.get("string").equals(newMap.get("string")))
			return false;
		if (!oldMap.get("date").toString().equals(newMap.get("date").toString()))
			return false;
		if (!oldMap.get("bool").equals(newMap.get("bool")))
			return false;
		if (!oldMap.get("ip4").equals(newMap.get("ip4")))
			return false;
		if (!oldMap.get("ip6").equals(newMap.get("ip6")))
			return false;
		if (!(oldMap.get("null") == null && newMap.get("null") == null))
			return false;
		if (!oldMap.get("long").equals(newMap.get("long")))
			return false;
		if (!(oldMap.get("byte") == null)) {
			byte[] oldByte = (byte[]) oldMap.get("byte");
			byte[] newByte = (byte[]) newMap.get("byte");
			return Arrays.equals(oldByte, newByte);
		}

		return true;
	}

	private List<Object> getDocument(String name) {
		List<Object> docs = new ArrayList<Object>();

		ConfigCollection col = db.getCollection(name);

		ConfigIterator it = col.findAll();
		try {
			while (it.hasNext()) {
				Object doc = it.next().getDocument();
				docs.add(doc);
			}
		} finally {
			it.close();
		}
		docs.add(docs);

		return docs;
	}

	private Object createObject(int id) throws UnknownHostException {
		double doubleData = Math.PI;
		float floatData = Float.MAX_VALUE;
		String stringData = "test number " + id;
		boolean boolData = (id % 2) == 0 ? true : false;
		Date dateData = new Date();
		Object nullData = null;
		Inet4Address ip4Data = (Inet4Address) Inet4Address.getByName("172.0.0.1");
		Inet6Address ip6Data = (Inet6Address) Inet6Address.getByName("1080:0:0:0:8:800:200C:417A");
		Object o1 = "object1";
		Object o2 = "object2";
		long longData = Long.MAX_VALUE;
		Short shortData = Short.MAX_VALUE;
		Object[] os = new Object[2];
		os[0] = o1;
		os[1] = o2;
		int[] i = new int[] { 1, 2, 3 };

		Map<String, Object> m = new HashMap<String, Object>();

		m.put("int", id);
		m.put("double", doubleData);
		m.put("float", floatData);
		m.put("string", stringData);
		m.put("bool", boolData);
		m.put("date", dateData);
		m.put("null", nullData);
		m.put("ip4", ip4Data);
		m.put("ip6", ip6Data);
		m.put("byte", createByteArray());
		m.put("object", os);
		m.put("long", longData);
		m.put("short", shortData);
		m.put("int_array", i);

		return m;
	}

	private Object createByteArray() {
		byte[] binary = new byte[256];
		byte b = Byte.MIN_VALUE;
		int i = 0;
		while (true) {
			binary[i++] = b++;
			if (b == Byte.MAX_VALUE)
				break;
		}

		return binary;
	}

	private void export(int rev) throws IOException {
		File exportFile = new File(System.getProperty("user.dir") + File.separatorChar + db.getName(), "export_" + rev + ".txt");
		if (exportFile.exists())
			exportFile.delete();
		FileConfigDatabase fdb = new FileConfigDatabase(new File(System.getProperty("user.dir")), "testdb", rev);
		OutputStream os = new FileOutputStream(exportFile);

		fdb.exportData(os);
		os.close();
	}

	private void importData(int rev) throws IOException {
		InputStream is = new FileInputStream(new File(db.getDbDirectory(), "export_" + rev + ".txt"));

		db.importData(is);
		is.close();
	}
}