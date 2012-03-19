package org.krakenapps.util.directoryfile.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.krakenapps.util.directoryfile.DirectoryFileArchive;
import org.krakenapps.util.directoryfile.SplitDirectoryFileInputStream;
import org.krakenapps.util.directoryfile.SplitDirectoryFileOutputStream;

public class SplitFileAndDirectoryFileArchiveTest {
	@Test
	public void testSplitDirectoryFileOutputStream() throws IOException {
		File base = new File(System.getProperty("kraken.data.dir"), "test\\ds\\ttt");
		DirectoryFileArchive dfa = DirectoryFileArchive.open(base.getAbsolutePath());
		SplitDirectoryFileOutputStream stream = new SplitDirectoryFileOutputStream(1024768, dfa, new File(base,
				"numbers.dat"));

		ArrayList<Integer> numbers = new ArrayList<Integer>(NUMBER_COUNT);
		writeRandomNumbers(numbers, stream);
		stream.close();

		SplitDirectoryFileInputStream sfis = new SplitDirectoryFileInputStream(1024768, dfa, new File(base,
				"numbers.dat"));
		ByteBuffer buf = ByteBuffer.allocate(8192);
		buf.limit(0);
		for (int i = 0; i < NUMBER_COUNT; ++i) {
			if (buf.remaining() < 4) {
				int remaining = buf.remaining();
				buf.compact();
				int read = sfis.read(buf.array(), remaining, buf.capacity() - remaining);
				buf.limit(remaining + read);
			}
			assertEquals((int) numbers.get(i), buf.getInt());
		}
		sfis.close();
		dfa.close();

	}

	private final int NUMBER_COUNT = 1024768;

	private void writeRandomNumbers(List<Integer> numbers, SplitDirectoryFileOutputStream sfos) throws IOException {
		Random r = new Random();
		ByteBuffer buf = ByteBuffer.allocate(8192);
		for (int i = 0; i < NUMBER_COUNT; ++i) {
			int rn = r.nextInt();
			if (numbers != null)
				numbers.add(rn);
			if (buf.remaining() < 4) {
				sfos.write(buf.array(), 0, buf.position());
				buf.clear();
			}
			buf.putInt(rn);
		}
		sfos.write(buf.array(), 0, buf.position());
	}
}
