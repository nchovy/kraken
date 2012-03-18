package org.krakenapps.util;

import static org.junit.Assert.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.*;

public class DirectoryMapTest {

	@Test
	public void testGetAndPutInRoot() {
		DirectoryMap<String> map = new DirectoryMap<String>();
		assertEquals(null, map.get("key that doesn't exist"));

		assertEquals(null, map.put("rootfile.txt", "'rootfile.txt' contents"));
		assertEquals("'rootfile.txt' contents", map.get("rootfile.txt"));

	}

	@Test
	public void testGetAnoPutInSubdir() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o = new Object();
		assertEquals(null, map.put("subdir/subdirObj.obj", o));
		assertEquals(o, map.get("subdir/subdirObj.obj"));
	}

	@Test
	public void testPutIfAbsent() {

		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o = new Object()
		{
			@Override 
			public String toString(){
				return "Initial Object.";
			}
		}
		;
		assertEquals(null, map.putIfAbsent("subdir/1.obj", o));
		assertEquals(o, map.get("subdir/1.obj"));
		Object o2 = new Object();
		assertEquals(o, map.putIfAbsent("subdir/1.obj", o2));
		assertEquals(o, map.get("subdir/1.obj"));

	}

	@Test
	public void testEntrySet() {

		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		Set<Entry<String, Object>> set = map.entrySet("*");
		assertEquals(3, set.size());

		set = map.entrySet("root.obj");
		Iterator<Entry<String, Object>> iter = set.iterator();
		Entry<String, Object> item = iter.next();
		assertEquals("root.obj", item.getKey());
		assertEquals(o0, item.getValue());
		assertEquals(1, set.size());

		set = map.entrySet("*.obj");
		iter = set.iterator();
		assertTrue(iter.hasNext());
		item = iter.next();
		assertEquals("root.obj", item.getKey());
		assertEquals(o0, item.getValue());
		assertEquals(1, set.size());
		assertFalse(iter.hasNext());

		set = map.entrySet("*.else");
		iter = set.iterator();
		assertFalse(iter.hasNext());

		set = map.entrySet("subdir/1.obj");
		iter = set.iterator();
		assertTrue(iter.hasNext());
		item = iter.next();
		assertEquals("subdir/1.obj", item.getKey());
		assertEquals(o1, item.getValue());
		assertEquals(1, set.size());
		assertFalse(iter.hasNext());

		set = map.entrySet("subdir/*.obj");
		iter = set.iterator();
		assertTrue(iter.hasNext());
		item = iter.next();
		assertEquals(2, set.size());
		assertTrue(iter.hasNext());

		assertEquals(null, map.put("subdir2/1.obj", o1));
		assertEquals(null, map.put("subdir3/1.obj", o1));
		assertEquals(null, map.put("subdir4/1.obj", o1));
		assertEquals(null, map.put("subdir5/1.obj", o1));

		set = map.entrySet("*/1.obj");
		Set<String> expected = new HashSet<String>();
		expected.add("subdir/1.obj");
		expected.add("subdir2/1.obj");
		expected.add("subdir3/1.obj");
		expected.add("subdir4/1.obj");
		expected.add("subdir5/1.obj");

		Set<String> result = new HashSet<String>();
		for (Entry<String, Object> e : set)
			result.add(e.getKey());
		assertEquals(expected, result);

		set = map.entrySet();
		int count = 0;
		for (iter = set.iterator(); iter.hasNext(); iter.next())
			count++;
		assertEquals(map.size(), count);

	}
	
	@Test
	public void testEntrySetWithDeepSearch() {
		DirectoryMap<String> map = new DirectoryMap<String>();
		assertEquals(null, map.put("A/1.obj", "OBJECT#1"));
		assertEquals(null, map.put("A/B/2.obj", "OBJECT#2"));
		assertEquals(null, map.put("A/B/C/3.obj", "OBJECT#3"));
		assertEquals(null, map.put("A/C/D/4.obj", "OBJECT#4"));
		assertEquals(null, map.put("B/C/D/4.obj", "OBJECT#5"));
		
		Set<String> expected = new HashSet<String>();
		expected.add("OBJECT#1");
		expected.add("OBJECT#2");
		expected.add("OBJECT#3");
		expected.add("OBJECT#4");
		
		Set<Entry<String, String>> set = map.entrySet("A/*");
		Set<String> result = new HashSet<String>();
		for(Entry<String, String> e: set) {
			result.add(e.getValue());
		}
		assertEquals(expected, result);
	}

	@Test
	public void testGetItems() {

		DirectoryMap<String> map = new DirectoryMap<String>();
		assertEquals(null, map.put("A/1.obj", "OBJECT#1"));
		assertEquals(null, map.put("A/B/2.obj", "OBJECT#2"));
		assertEquals(null, map.put("A/B/C/3.obj", "OBJECT#3"));
		assertEquals(null, map.put("A/C/D/4.obj", "OBJECT#4"));
		assertEquals(null, map.put("B/C/D/4.obj", "OBJECT#5"));
		
		Set<Entry<String, String>> items = map.getItems("A/B/");
		Entry<String, String> pair = items.iterator().next();
		assertEquals("OBJECT#2", pair.getValue());
	}
	
	@Test
	public void testRemoveObject() {

		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		assertEquals(null, map.remove("nodir/1.obj"));
		assertEquals(o1, map.remove("subdir/1.obj"));
	}

	@Test
	public void testRemoveObjectObject() {

		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		assertEquals(false, map.remove("nodir/1.obj", o1));
		assertEquals(false, map.remove("subdir/1.obj", o2));
		assertEquals(true, map.remove("root.obj", o0));
		assertEquals(true, map.remove("subdir/1.obj", o1));
	}

	@Test
	public void testReplaceStringT() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.replace("subdir/1.obj", o2));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));
		assertEquals(o1, map.replace("subdir/1.obj", o2));
	}

	@Test
	public void testReplaceStringTT() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(false, map.replace("subdir/1.obj", o1, o2));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));
		assertEquals(false, map.replace("subdir/1.obj", o2, o1));
		assertEquals(true, map.replace("subdir/1.obj", o1, o2));
	}

	@Test
	public void testIsEmpty() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		assertTrue(map.isEmpty());

		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		assertEquals(null, map.put("root.obj", o0));
		assertFalse(map.isEmpty());

		assertEquals(o0, map.remove("root.obj"));
		assertTrue(map.isEmpty());

	}

	@Test
	public void testClear() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		assertFalse(map.isEmpty());
		map.clear();
		assertTrue(map.isEmpty());
	}

	@Test
	public void testContainsKey() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertFalse(map.containsKey("subdir/1.obj"));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));
		assertTrue(map.containsKey("subdir/1.obj"));

	}

	@Test
	public void testContainsValue() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertFalse(map.containsValue(o1));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));
		assertTrue(map.containsValue(o1));
	}

	@Test
	public void testKeySet() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		Set<String> expected = new HashSet<String>(Arrays.asList("root.obj",
				"subdir/1.obj", "subdir/2.obj"));
		Set<String> set = map.keySet();

		assertEquals(expected, set);
	}

	@Test
	public void testPutAll() {

		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		DirectoryMap<Object> expected = new DirectoryMap<Object>();

		assertEquals(null, expected.put("root.obj", o0));
		assertEquals(null, expected.put("subdir/1.obj", o1));
		assertEquals(null, expected.put("subdir/2.obj", o2));

		DirectoryMap<Object> map = new DirectoryMap<Object>();
		assertEquals(null, map.put("root.obj", o0));

		Map<String, Object> addenda = new HashMap<String, Object>();
		assertEquals(null, addenda.put("subdir/1.obj", o1));
		assertEquals(null, addenda.put("subdir/2.obj", o2));

		map.putAll(addenda);

		assertEquals(expected.entrySet(), map.entrySet());

	}

	@Test
	public void testValues() {
		DirectoryMap<Object> map = new DirectoryMap<Object>();
		Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		assertEquals(new HashSet<Object>(Arrays.asList(o0, o1, o2)),
				new HashSet<Object>(map.values()));
	}

	@Test
	public void testRemoveAll() {

		DirectoryMap<String> map = new DirectoryMap<String>();

		ReferenceQueue<String> queue = new ReferenceQueue<String>();
		String c = new String("C RRD");
		String d = new String("D RRD");
		MarkedWeakReference cRef = new MarkedWeakReference("c.rrd", new String("C RRD"), queue);
		MarkedWeakReference dRef = new MarkedWeakReference("d.rrd", new String("D RRD"), queue);
		
		assertEquals(null, map.put("A/B/c.rrd", c));
		assertEquals(null, map.put("A/B/d.rrd", d));
		assertTrue(map.removeNode("A/B"));
		
		while(cRef.get() != null || dRef.get()!=null) {
            System.gc();
		}
		
		assertFalse(map.containsKey("A/B/c.rrd"));
		assertFalse(map.containsKey("A/B/d.rrd"));
		MarkedWeakReference ref = null;
		try{
			while(true) {
				try {
					ref = (MarkedWeakReference) queue.remove(100);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				if (ref==null) break;
				assertNull(ref.get());
				assertTrue("c.rrd".equals(ref.mark) || "d.rrd".equals(ref.mark));
				
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	
	private class MarkedWeakReference extends WeakReference<String> {
		public String mark;

		public MarkedWeakReference(String mark, String referent,
				ReferenceQueue<? super String> q) {
			super(referent, q);
			this.mark = mark;
		}
		
		@Override
		public String toString() {
			return mark;
		}
	}

	@Ignore
	@Test
	public void testConcurrency() {

		final DirectoryMap<Object> map = new DirectoryMap<Object>();
		final Object o0 = new Object() {
			@Override
			public String toString() {
				return "'Object #0'";
			}
		};
		final Object o1 = new Object() {
			@Override
			public String toString() {
				return "'Object #1'";
			}
		};
		final Object o2 = new Object() {
			@Override
			public String toString() {
				return "'Object #2'";
			}
		};

		assertEquals(null, map.put("root.obj", o0));
		// assertEquals(null, map.put("subdir/1.obj", o1));
		assertEquals(null, map.put("subdir/2.obj", o2));

		Runnable r1 = new Runnable() {
			@Override
			public void run() {
				assertEquals(o1, map.remove("subdir/1.obj"));
			}
		};

		Runnable r2 = new Runnable() {
			@Override
			public void run() {
				assertEquals(o2, map.remove("subdir/2.obj"));
			}
		};
		Thread t;
		for (int i = 0; i < 10000; i++) {
			t = new Thread(r2);
			t.start();
			assertEquals(null, map.put("subdir/1.obj", o1));
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			t = new Thread(r1);
			t.start();
			assertEquals(null, map.put("subdir/2.obj", o2));
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

}
