package org.krakenapps.logstorage.index;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

public class IndexTest {
	@Test
	public void writeReadTest() throws IOException {
		File indexFile = new File("index.pos");
		File dataFile = new File("index.seg");

		indexFile.delete();
		dataFile.delete();

		InvertedIndexWriter writer = null;
		InvertedIndexReader reader = null;

		try {
			writer = new InvertedIndexWriter(indexFile, dataFile);
			long now = System.currentTimeMillis();

			for (int i = 1; i < 10000; i++)
				writer.write(new InvertedIndexItem("test", now, i, new String[] { "token", "token" + i }));

			// flush and close
			writer.close();

			reader = new InvertedIndexReader(indexFile, dataFile);
			InvertedIndexCursor cursor = reader.openCursor("token");

			int expected = 9999;
			while (cursor.hasNext())
				assertEquals(expected--, cursor.next());

			for (int i = 1; i < 10; i++) {
				int n = new Random().nextInt(10000);
				System.out.println("test search #" + n);
				cursor = reader.openCursor("token" + n);
				assertEquals(n, cursor.next());
			}

		} finally {
			if (writer != null)
				writer.close();

			if (reader != null)
				reader.close();

			indexFile.delete();
			dataFile.delete();
		}
	}
}
