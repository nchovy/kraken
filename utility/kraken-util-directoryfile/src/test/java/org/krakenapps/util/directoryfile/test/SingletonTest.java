package org.krakenapps.util.directoryfile.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.krakenapps.util.directoryfile.DirectoryFileArchive;
import org.krakenapps.util.directoryfile.SplitDirectoryFileOutputStream;


public class SingletonTest {

	@Test
	public void test1() throws IOException {
		String testDir = "SingletonTest/test1";
		new File("SingletonTest").deleteOnExit();
		new File("SingletonTest/test1").deleteOnExit();
		
		DirectoryFileArchive dfa = DirectoryFileArchive.open(testDir);
		SplitDirectoryFileOutputStream os = new SplitDirectoryFileOutputStream(1024, dfa, new File(testDir, "/segment"));
		Random r = new Random();
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		
		writeRandomNumbers(573, numbers, os);
		
		dfa.close();
		
		os.close();
		
		os = null;
		dfa = null;
		
		for(int i = 0; i < 10; ++i) {
			System.gc();
		}

		new File("SingletonTest/test1/test1.jdi").deleteOnExit();

		boolean deleted = new File(testDir, "/test1.jdf").delete();
		if (!deleted) {		
			assertTrue(false);
		}			
	}
	
	private void writeRandomNumbers(int count, List<Integer> numbers, SplitDirectoryFileOutputStream sfos) throws IOException {
		Random r = new Random();
		ByteBuffer buf = ByteBuffer.allocate(1024);
		for (int i = 0; i < count; ++i) {
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
