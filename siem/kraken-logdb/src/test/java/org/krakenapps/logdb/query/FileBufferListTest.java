package org.krakenapps.logdb.query;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class FileBufferListTest {
	private static int STATIC_LEN = 1000000;
	private static int STATIC_CACHE = 50000;

	private static int RANDOM_LEN = 1000000;
	private static int RANDOM_CACHE = 50000;

	@Test
	public void staticAddTest() throws IOException {
		System.gc();

		FileBufferList<String> fbl = new FileBufferList<String>(STATIC_CACHE);
		String str = "The quick brown fox jumps over the lazy dog.";
		try {
			long begin = System.currentTimeMillis();
			for (int i = 0; i < STATIC_LEN; i++)
				fbl.add(str);
			System.out.println("[staticAddTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<String> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[staticAddTest] read " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			assertEquals(STATIC_LEN, fbl.size());
			ListIterator<String> it2 = fbl.listIterator();
			while (it2.hasNext())
				assertEquals("obj " + (it2.nextIndex() + 1), str, it2.next());
		} finally {
			fbl.close();
		}
	}

	@Test
	public void randomAddTest() throws IOException {
		System.gc();

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < RANDOM_LEN; i++)
			list.add(randomString());

		FileBufferList<String> fbl = new FileBufferList<String>(RANDOM_CACHE);
		try {
			long begin = System.currentTimeMillis();
			fbl.addAll(list);
			System.out.println("[randomAddTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<String> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[randomAddTest] read " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			ListIterator<String> it1 = list.listIterator();
			ListIterator<String> it2 = fbl.listIterator();
			while (it1.hasNext())
				assertEquals("obj " + (it1.nextIndex() + 1), it1.next(), it2.next());
			assertEquals(false, it2.hasNext());
		} finally {
			fbl.close();
		}
	}

	@Test
	public void staticFrontSortTest() throws IOException {
		System.gc();

		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};

		FileBufferList<Integer> fbl = new FileBufferList<Integer>(STATIC_CACHE, comparator);
		try {
			long begin = System.currentTimeMillis();
			for (int i = 1; i <= STATIC_LEN; i++)
				fbl.add(i);
			System.out.println("[staticFrontSortTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<Integer> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[staticFrontSortTest] read " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			assertEquals(STATIC_LEN, fbl.size());
			ListIterator<Integer> it2 = fbl.listIterator();
			while (it2.hasNext()) {
				Integer i = it2.nextIndex();
				assertEquals("obj " + (i + 1), i, it2.next());
			}
		} finally {
			fbl.close();
		}
	}

	@Test
	public void staticReverseSortTest() throws IOException {
		System.gc();

		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		};

		FileBufferList<Integer> fbl = new FileBufferList<Integer>(STATIC_CACHE, comparator);
		try {
			long begin = System.currentTimeMillis();
			for (int i = 0; i < STATIC_LEN; i++)
				fbl.add(STATIC_LEN - i);
			System.out.println("[staticReverseSortTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<Integer> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[staticReverseSortTest] readed " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			assertEquals(STATIC_LEN, fbl.size());
			ListIterator<Integer> it2 = fbl.listIterator();
			while (it2.hasNext()) {
				Integer i = it2.nextIndex();
				assertEquals("obj " + (i + 1), i, it2.next());
			}
		} finally {
			fbl.close();
		}
	}

	@Test
	public void randomSortTest() throws IOException {
		System.gc();

		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < RANDOM_LEN; i++)
			list.add(randomString());

		FileBufferList<String> fbl = new FileBufferList<String>(RANDOM_CACHE, comparator);
		try {
			long begin = System.currentTimeMillis();
			fbl.addAll(list);
			System.out.println("[randomSortTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<String> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[randomSortTest] read " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			Collections.sort(list, comparator);

			ListIterator<String> it1 = list.listIterator();
			ListIterator<String> it2 = fbl.listIterator();
			while (it1.hasNext())
				assertEquals("obj " + (it1.nextIndex() + 1), it1.next(), it2.next());
			assertEquals(false, it2.hasNext());
		} finally {
			fbl.close();
		}
	}

	@Test
	public void multiThreadTest() throws IOException {
		System.gc();

		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		};

		FileBufferList<String> fbl = new FileBufferList<String>(RANDOM_CACHE, comparator);
		try {
			List<ThreadRunner> runners = new ArrayList<ThreadRunner>();
			int threadCount = Runtime.getRuntime().availableProcessors();

			for (int i = 0; i < threadCount; i++)
				runners.add(new ThreadRunner(RANDOM_LEN / threadCount, fbl));
			for (ThreadRunner runner : runners)
				new Thread(runner).start();

			long begin = System.currentTimeMillis();
			wait: while (true) {
				for (ThreadRunner runner : runners) {
					if (!runner.end)
						continue wait;
				}
				break;
			}
			System.out.println("[multiThreadTest] add " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			begin = System.currentTimeMillis();
			Iterator<String> it = fbl.iterator();
			while (it.hasNext())
				it.next();
			System.out.println("[multiThreadTest] read " + fbl.size() + " : " + (System.currentTimeMillis() - begin) + " ms");

			List<String> list = new ArrayList<String>();
			for (ThreadRunner runner : runners)
				list.addAll(runner.strings);
			Collections.sort(list, comparator);

			ListIterator<String> it1 = list.listIterator();
			ListIterator<String> it2 = fbl.listIterator();
			while (it1.hasNext())
				assertEquals("obj " + (it1.nextIndex() + 1), it1.next(), it2.next());
			assertEquals(false, it2.hasNext());
		} finally {
			fbl.close();
		}
	}

	private class ThreadRunner implements Runnable {
		private List<String> strings = new ArrayList<String>();
		private FileBufferList<String> fbl;
		private boolean end = false;

		public ThreadRunner(int len, FileBufferList<String> fbl) {
			for (int i = 0; i < len; i++)
				strings.add(randomString());
			this.fbl = fbl;
		}

		@Override
		public void run() {
			try {
				fbl.addAll(strings);
			} finally {
				end = true;
			}
		}
	}

	private char[] chars = "!#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_abcdefghijklmnopqrstuvwxyz{|}".toCharArray();

	private String randomString() {
		Random r = new Random();
		char[] c = new char[r.nextInt(200) + 1];
		for (int i = 0; i < c.length; i++)
			c[i] = chars[r.nextInt(chars.length)];
		return new String(c);
	}
}
