package org.krakenapps.confdb.file;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigIterator;

import static org.junit.Assert.*;

public class CollectionLogTest {
	private File logFile;
	private File datFile;

	private FileConfigDatabase db;
	private FileConfigCollection col;
	private CollectionLogWriter writer;
	private CollectionLogReader reader;

	@Before
	public void setup() throws IOException {
		File workingDir = new File(System.getProperty("user.dir"));

		db = new FileConfigDatabase(workingDir, "testdb");
		col = (FileConfigCollection) db.ensureCollection("testcol");

		logFile = new File(db.getDbDirectory(), "col1.log");
		datFile = new File(db.getDbDirectory(), "col1.dat");

		logFile.delete();
		datFile.delete();

		writer = new CollectionLogWriter(logFile, datFile);
		reader = new CollectionLogReader(db, col, logFile, datFile);
	}

	@After
	public void teardown() {
		writer.close();
		reader.close();

		logFile.delete();
		datFile.delete();
	}

	@Test
	public void testAddAndReadConfig() throws IOException {
		col.add("hello world");
		col.add("goodbye world");

		ConfigIterator it = col.find(null);
		assertEquals("hello world", it.next().getDocument());
		assertEquals("goodbye world", it.next().getDocument());
		it.close();

	}

	@Test
	public void testAddAndDelete() throws IOException {
		col.add("hello world");
		Config c2 = col.add("goodbye world");
		col.remove(c2);

		ConfigIterator it = col.find(null);
		assertEquals("hello world", it.next().getDocument());
		assertFalse(it.hasNext());
		it.close();
	}
	
	@Test
	public void testAddAndUpdate() throws IOException {
		Config c1 = col.add("hello world");
		col.add("goodbye world");
		
		c1.setDocument("hello, world");
		col.update(c1);
		
		ConfigIterator it = col.find(null);
		assertEquals("hello, world", it.next().getDocument());
		it.close();
	}
	
	@Test
	public void testFindOne() throws IOException {
		
	}

	@Test
	public void testNotFound() throws IOException {
		// create files
		writer.close();

		// test non-existing revision
		CollectionLog read1 = reader.findRev(1000);
		assertNull(read1);

		CollectionLog read2 = reader.findRev(-1000);
		assertNull(read2);
	}

	@Test
	public void testFindRev() throws IOException {
		CollectionLog log1 = newLog(1, 0, "hello world");
		CollectionLog log2 = newLog(2, 1, "goodbye world");

		int doc1 = writer.write(log1);
		int doc2 = writer.write(log2);

		CollectionLog read1 = reader.findRev(1);
		CollectionLog read2 = reader.findRev(2);

		assertEquals(doc1, read1.getDocId());
		assertEquals(doc2, read2.getDocId());
	}

	@Test
	public void testFirstCommit() throws IOException {
		CollectionLog log = newLog(1, 0, "goodbye world");

		writer.write(log);

		CollectionLog read = reader.read(0);

		assertEquals(1, read.getRev());
		assertEquals(0, read.getPrevRev());
		assertEquals(1, read.getDocId());
		assertEquals(CommitOp.CreateDoc, read.getOperation());
	}

	private CollectionLog newLog(int rev, int prev, String doc) {
		CollectionLog log = new CollectionLog();
		log.setRev(rev);
		log.setPrevRev(prev);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(doc.getBytes());
		return log;
	}
}
