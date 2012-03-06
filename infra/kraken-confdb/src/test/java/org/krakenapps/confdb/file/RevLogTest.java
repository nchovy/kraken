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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.confdb.CommitOp;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;

import static org.junit.Assert.*;

public class RevLogTest {
	private File logFile;
	private File datFile;

	private FileConfigDatabase db;
	private FileConfigCollection col;
	private RevLogWriter writer;
	private RevLogReader reader;

	@Before
	public void setup() throws IOException {
		File workingDir = new File(System.getProperty("user.dir"));

		db = new FileConfigDatabase(workingDir, "testdb");
		col = (FileConfigCollection) db.ensureCollection("testcol");

		logFile = new File(db.getDbDirectory(), "test.log");
		datFile = new File(db.getDbDirectory(), "test.dat");

		logFile.delete();
		datFile.delete();

		writer = new RevLogWriter(logFile, datFile);
		reader = new RevLogReader(logFile, datFile);
	}

	@After
	public void teardown() throws IOException {
		writer.close();
		reader.close();

		db.purge();
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

		assertNull(col.findOne(Predicates.eq("hello world")));
		assertNotNull(col.findOne(Predicates.eq("hello, world")));
	}

	@Test
	public void testFindOne() throws IOException {
		col.add("one");
		col.add("two");
		col.add("three");
		col.add("four");
		col.add("five");

		Config c = col.findOne(Predicates.eq("two"));
		assertEquals("two", c.getDocument());
	}

	@Test
	public void testFind() throws IOException {
		col.add("one");
		col.add("two");
		col.add("three");
		col.add("four");
		col.add("five");

		ConfigIterator it = col.find(Predicates.or(Predicates.eq("two"), Predicates.eq("four")));
		assertEquals("two", it.next().getDocument());
		assertEquals("four", it.next().getDocument());
		assertFalse(it.hasNext());

		it.close();
	}

	@Test
	public void testNotFound() throws IOException {
		// create files
		writer.close();

		// test non-existing revision
		RevLog read1 = reader.findRev(1000);
		assertNull(read1);

		RevLog read2 = reader.findRev(-1000);
		assertNull(read2);
	}

	@Test
	public void testFindRev() throws IOException {
		assertEquals(0, writer.count());
		RevLog log1 = newLog(1, 0, "hello world");
		RevLog log2 = newLog(2, 1, "goodbye world");

		int doc1 = writer.write(log1);
		int doc2 = writer.write(log2);
		writer.sync();
		assertEquals(2, writer.count());

		RevLog read1 = reader.findRev(1);
		RevLog read2 = reader.findRev(2);

		assertEquals(doc1, read1.getDocId());
		assertEquals(doc2, read2.getDocId());
	}

	@Test
	public void testFirstCommit() throws IOException {
		RevLog log = newLog(1, 0, "goodbye world");

		writer.write(log);
		writer.sync();

		RevLog read = reader.read(0);

		assertEquals(1, read.getRev());
		assertEquals(0, read.getPrevRev());
		assertEquals(1, read.getDocId());
		assertEquals(CommitOp.CreateDoc, read.getOperation());
	}

	private RevLog newLog(int rev, int prev, String doc) {
		RevLog log = new RevLog();
		log.setRev(rev);
		log.setPrevRev(prev);
		log.setOperation(CommitOp.CreateDoc);
		log.setDoc(doc.getBytes());
		return log;
	}
}
