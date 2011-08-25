package org.krakenapps.logstorage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.logstorage.query.FileBufferList;
import org.krakenapps.logstorage.query.FileBufferMap;
import org.krakenapps.logstorage.query.command.Function;
import org.krakenapps.logstorage.query.command.FunctionCodec;

@SuppressWarnings("unused")
// @Ignore
public class BufferTest {
	// @Test
	public void concurrentTest() {
		try {
			List<Integer> l = new FileBufferList<Integer>();
			Thread t1 = new Thread(new ReadThread(l));
			Thread t2 = new Thread(new WriteThread(l));
			t1.start();
			t2.start();
			while (t1.isAlive() && t2.isAlive())
				;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ReadThread implements Runnable {
		private List<Integer> l;

		private ReadThread(List<Integer> l) {
			this.l = l;
		}

		@Override
		public void run() {
			try {
				while (true) {
					for (Integer i : l)
						;
					Thread.sleep(2);
				}
			} catch (Exception e) {
				System.out.println("READER DEATH");
				e.printStackTrace();
			}
			System.out.println("READER END");
		}
	}

	private class WriteThread implements Runnable {
		private List<Integer> l;

		private WriteThread(List<Integer> l) {
			this.l = l;
		}

		@Override
		public void run() {
			try {
				Random r = new Random();
				while (true) {
					l.add(r.nextInt(10000));
					Thread.sleep(1);
				}
			} catch (Exception e) {
				System.out.println("WRITER DEATH");
				e.printStackTrace();
			}
			System.out.println("WRITER END");
		}
	}

	@Test
	public void list() throws IOException {
		Random r = new Random();
		List<Integer> random = new ArrayList<Integer>();
		for (int i = 0; i < 1000000; i++) {
			int rand = r.nextInt(1000000000);
			random.add(rand);
		}

		// @formatter:off
//		List<Integer> l = new FileBufferList<Integer>();
		List<Integer> l = new FileBufferList<Integer>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		// @formatter:on
		long begin = System.currentTimeMillis();
		for (Integer i : random) {
			l.add(i);
		}
		long end = System.currentTimeMillis();
		System.out.println("add : " + (end - begin));

		begin = System.currentTimeMillis();
		for (Integer i : l)
			;
		end = System.currentTimeMillis();
		System.out.println("print : " + (end - begin));
		
//		System.out.println("file read count: " + ((FileBufferList<Integer>) l).readCount);
	}

	// @Test
	public void map() throws IOException {
		FileBufferMap<String, Function> m = new FileBufferMap<String, Function>(3, new FunctionCodec());
		m.put("test1", Function.getFunction("avg", "test"));
		m.put("test2", Function.getFunction("sum", "test"));
		m.put("test3", Function.getFunction("count", "test"));
		m.put("test4", Function.getFunction("dc", "test"));
		m.put("test5", Function.getFunction("avg", "test"));
		m.put("test6", Function.getFunction("avg", "test"));
		m.put("test7", Function.getFunction("avg", "test"));
		for (String key : m.keySet())
			System.out.printf("%s => %s\n", key, m.get(key));
		m.close();
	}

	private static class DefaultComparator implements Comparator<Map<String, String>> {
		@Override
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			return o1.size() - o2.size();
		}
	}
}
