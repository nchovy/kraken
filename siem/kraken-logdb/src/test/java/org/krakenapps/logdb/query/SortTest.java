package org.krakenapps.logdb.query;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.krakenapps.logdb.sort.ParallelMergeSorter;

public class SortTest {
	public static void main(String[] args) throws Exception {
		new SortTest().run();
	}

	public void run() throws IOException {
		ParallelMergeSorter sorter = new ParallelMergeSorter(new ObjectComparator());

		Date begin = new Date();

		int max = 50000000;
		for (int i = max; i >= 0; i--) {
			sorter.add(i);
			if (i % 10000000 == 0)
				System.out.println(i + " passed");
		}

		Iterator<Object> it = sorter.sort();
		System.out.println("start result check");
		int i = 0;
		boolean suppress = false;
		while (it.hasNext()) {
			Object next = it.next();
			if (!next.equals(i) && !suppress) {
				System.out.println("###################### bug check: " + next + ", " + i);
				suppress = true;
			}
			i++;
		}

		if (i <= max)
			System.out.println("############ bug check, last " + i);

		System.out.println("count: " + i);

		long elapsed = new Date().getTime() - begin.getTime();
		System.out.println("elapsed " + elapsed);
	}
}
