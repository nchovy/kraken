package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.CommitLog;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigIterator;

import static org.junit.Assert.*;

public class DatabaseTest {

	private FileConfigDatabase db;
	private FileConfigCollection col;

	@Before
	public void setup() throws IOException {
		File workingDir = new File(System.getProperty("user.dir"));

		db = new FileConfigDatabase(workingDir, "testdb2");
		col = (FileConfigCollection) db.ensureCollection("testcol2");
	}

	@After
	public void teardown() {
		db.purge();
	}

	@Test
	public void testCommitLog() {
		List<CommitLog> logs = db.getCommitLogs();
		assertEquals(0, logs.size());

		col.add("hello world", "xeraph", "first commit");
		col.add("goodbye world", "xeraph", "second commit");

		logs = db.getCommitLogs();
		assertEquals(2, logs.size());
		CommitLog log1 = logs.get(0);
		CommitLog log2 = logs.get(1);

		// TODO: test rev id and created timestamp
		assertEquals("xeraph", log1.getCommitter());
		assertEquals("first commit", log1.getMessage());

		assertEquals("xeraph", log2.getCommitter());
		assertEquals("second commit", log2.getMessage());
		
		// test doc content
		ConfigIterator it = col.findAll();
		Config c1 = it.next();
		Config c2 = it.next();
		
		assertEquals("hello world", c1.getDocument());
		assertEquals("goodbye world", c2.getDocument());
		
		it.close();
	}
}
