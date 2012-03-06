package org.krakenapps.bloomfilter;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class BloomFilterTest {
	private BloomFilter<String> filter = null;
	private Set<String> map = null;

	// failed performance : 0 - 4, 5, 6, 7, 8, 2-3
	// success : 0-1, 2, 3, 1-2, 3, 2-1, 3-1
	@SuppressWarnings("unchecked")
	public void init(int num, boolean doMap) {
		filter = new BloomFilter<String>(
				GeneralHashFunction.stringHashFunctions[2],
				GeneralHashFunction.stringHashFunctions[1]);
		map = new HashSet<String>(num);

		String in;
		for (int i = 0; i < num; i++) {
			in = VMIDGen.next();
			filter.add(in);
			if (doMap)
				map.add(in);
		}
		for (int i = 0; i < num; i++) {
			in = UIDGen.next();
			filter.add(in);
			if (doMap)
				map.add(in);
		}
	}

	@Test
	public void contains() {
		System.out.println(new Date());
		init(50000, true);
		for (String key : map) {
			if (map.contains(key) == false)
				fail();
		}
		System.out.println(new Date());
	}

	@Test
	public void doesNotContain() {
		System.out.println(new Date());
		init(500000, false);
		int count = 0;
		for (int i = 0; i < 50000; i++) {
			if (filter.contains(VMIDGen.next()))
				count++;
		}
		for (int i = 0; i < 50000; i++) {
			if (filter.contains(UIDGen.next()))
				count++;
		}

		System.out.printf("false positive count: %d, rate : %f\n", count,
				count / 5000D);
		assertTrue(count < 1000);
		System.out.println(new Date());
	}

	public abstract static class VMIDGen {
		static String next() {
			return new java.rmi.dgc.VMID().toString();
		}
	}

	public abstract static class UIDGen {
		static String next() {
			return new java.rmi.server.UID().toString();
		}
	}
}
