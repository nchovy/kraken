package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import static org.junit.Assert.*;

public class ConfigCacheTest {
	private FileConfigDatabase db;
	private ConfigCollection col;
	private FileConfigCache cache;

	@Before
	public void start() throws IOException {
		File workingDir = new File(System.getProperty("user.dir"));

		db = new FileConfigDatabase(workingDir, "testdb1");
		col = db.ensureCollection("testcol1");
		cache = new FileConfigCache(db);
	}

	@After
	public void teardown() throws IOException {
		db.purge();
	}

	private static class CustomType {
		public String name;
		public Map<String, Object> ext;
	}

	private static class CustomField {
		public String field1;

		public CustomField(String field1) {
			this.field1 = field1;
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCustomTypeCache() {
		FileConfig config = new FileConfig(db, col, 1, 1, 0, null);
		CustomType doc = new CustomType();
		doc.ext = new HashMap<String, Object>();
		doc.ext.put("custom", new CustomField("hello"));
		config.setDocument(doc);

		db.add(doc);

		db.findAll(CustomType.class).getDocuments();

		Config c = db.findOne(CustomType.class, null);
		assertNotNull(c);

		// check original data
		Map<String, Object> fetched = (Map<String, Object>) c.getDocument();
		Map<String, Object> extMap = (Map<String, Object>) fetched.get("ext");
		Map<String, Object> customMap = (Map<String, Object>) extMap.get("custom");
		System.out.println(fetched);
		assertEquals("hello", customMap.get("field1"));

		// should not be changed by external modification
		Map<String, Object> m = (Map<String, Object>) fetched;
		m.put("custom", new CustomField("helloworld2"));

		CustomType casted = c.getDocument(CustomType.class);
		casted.ext.put("custom", new CustomField("new_helloworld"));

		// should not be changed by external modification
		c = db.findOne(CustomType.class, null);
		fetched = (Map<String, Object>) c.getDocument();
		extMap = (Map<String, Object>) fetched.get("ext");
		customMap = (Map<String, Object>) extMap.get("custom");
		assertEquals("hello", customMap.get("field1"));
	}

	@Test
	public void testCacheImmutability() {
		putConfig();

		Config before = cache.findEntry(col.getName(), 0, 1);

		Object o = before.getDocument();
		int[] i = (int[]) o;

		assertEquals(i[0], 0);
		assertEquals(i[1], 1);
		assertEquals(i[2], 2);

		i[0] = 2;
		o = i;

		Config after = cache.findEntry(col.getName(), 0, 1);

		Object o2 = after.getDocument();
		int[] i2 = (int[]) o2;

		assertEquals(i2[0], 0);
		assertEquals(i2[1], 1);
		assertEquals(i2[2], 2);
	}

	private void putConfig() {
		int[] intArr = new int[3];
		intArr[0] = 0;
		intArr[1] = 1;
		intArr[2] = 2;

		Config c = new FileConfig(db, col, 0, 1, 0, intArr);

		cache.putEntry(col.getName(), c);
	}
}
