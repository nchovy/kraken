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

		// TODO: remove after manifest implementation
		new File(db.getDbDirectory(), "col1.log").delete();
		new File(db.getDbDirectory(), "col1.dat").delete();
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

	@Test
	public void testUpdate() {
		List<CommitLog> logs = db.getCommitLogs();
		assertEquals(0, logs.size());

		Config c = col.add("hello world", "xeraph", "first commit");
		c.setDocument("hello, world");
		col.update(c, false, "stania", "added missing comma");

		logs = db.getCommitLogs();
		assertEquals(2, logs.size());
		CommitLog log1 = logs.get(0);
		CommitLog log2 = logs.get(1);

		// TODO: test rev id and created timestamp
		assertEquals("xeraph", log1.getCommitter());
		assertEquals("first commit", log1.getMessage());

		assertEquals("stania", log2.getCommitter());
		assertEquals("added missing comma", log2.getMessage());

		// test doc content
		ConfigIterator it = col.findAll();
		Config c1 = it.next();

		assertEquals("hello, world", c1.getDocument());
		assertFalse(it.hasNext());

		it.close();
	}

	@Test
	public void testDelete() {
		List<CommitLog> logs = db.getCommitLogs();
		assertEquals(0, logs.size());

		Config c = col.add("hello world", "xeraph", "first commit");
		col.remove(c, false, "stania", "removed hello world");

		logs = db.getCommitLogs();
		assertEquals(2, logs.size());
		CommitLog log1 = logs.get(0);
		CommitLog log2 = logs.get(1);

		// TODO: test rev id and created timestamp
		assertEquals("xeraph", log1.getCommitter());
		assertEquals("first commit", log1.getMessage());

		assertEquals("stania", log2.getCommitter());
		assertEquals("removed hello world", log2.getMessage());

		// test doc content
		ConfigIterator it = col.findAll();
		assertFalse(it.hasNext());

		it.close();
	}
}
