/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage.index;

import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class IndexMergeTest {

	@Test
	public void testMerge() throws IOException {
		File indexFile1 = new File("index1.pos");
		File dataFile1 = new File("index1.seg");

		File indexFile2 = new File("index2.pos");
		File dataFile2 = new File("index2.seg");

		File mergedIndexFile = new File("merged.pos");
		File mergedDataFile = new File("merged.seg");

		File[] files = new File[] { indexFile1, dataFile1, indexFile2, dataFile2, mergedIndexFile, mergedDataFile };
		purgeFiles(files);

		try {
			InvertedIndexWriter writer1 = new InvertedIndexWriter(indexFile1, dataFile1);
			InvertedIndexWriter writer2 = new InvertedIndexWriter(indexFile2, dataFile2);

			long timestamp = System.currentTimeMillis();
			writer1.write(new InvertedIndexItem("table1", timestamp, 1, new String[] { "one", "two" }));
			writer1.write(new InvertedIndexItem("table1", timestamp, 2, new String[] { "one", "three" }));
			writer1.close();

			writer2.write(new InvertedIndexItem("table1", timestamp, 3, new String[] { "three", "four" }));
			writer2.write(new InvertedIndexItem("table1", timestamp, 4, new String[] { "five", "six" }));
			writer2.close();

			InvertedIndexFileSet older = new InvertedIndexFileSet(indexFile1, dataFile1);
			InvertedIndexFileSet newer = new InvertedIndexFileSet(indexFile2, dataFile2);
			InvertedIndexFileSet merged = new InvertedIndexFileSet(mergedIndexFile, mergedDataFile);

			InvertedIndexUtil.merge(older, newer, merged);

			InvertedIndexReader reader = new InvertedIndexReader(merged);

			assertItems(reader, "one", new long[] { 2, 1 });
			assertItems(reader, "two", new long[] { 1 });
			assertItems(reader, "three", new long[] { 3, 2 });
			assertItems(reader, "four", new long[] { 3 });
			assertItems(reader, "five", new long[] { 4 });

		} finally {
			purgeFiles(files);
		}
	}

	private void assertItems(InvertedIndexReader reader, String term, long[] idlist) throws IOException {
		InvertedIndexCursor cursor = reader.openCursor(term);
		for (long id : idlist)
			assertEquals(id, cursor.next());
		assertFalse(cursor.hasNext());

	}

	private void purgeFiles(File[] files) {
		for (File f : files)
			f.delete();
	}
}
