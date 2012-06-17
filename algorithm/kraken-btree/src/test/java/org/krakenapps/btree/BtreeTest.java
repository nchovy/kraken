/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.btree;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.krakenapps.btree.types.CompositeKey;
import org.krakenapps.btree.types.IntegerKey;
import org.krakenapps.btree.types.IntegerValue;
import org.krakenapps.btree.types.IntegerValueFactory;

import static org.junit.Assert.*;

public class BtreeTest {
	private File file = new File("btree.dat");
	private Schema schema;
	private Btree btree;

	public void setup(int pageSize) throws IOException {
		setup(pageSize, new Class<?>[] { Integer.class });
	}

	public void setup(int pageSize, Class<?>... keyTypes) throws IOException {
		file.delete();

		schema = new Schema(pageSize, keyTypes);
		schema.setRowValueFactory(new IntegerValueFactory());

		btree = BtreeImpl.create(file, schema);
		btree.setRowValueFactory(new IntegerValueFactory());
	}

	@After
	public void teardown() throws IOException {
		btree.close();
		file.delete();
		btree = null;
	}

	@Test
	public void testSimpleInsert() throws IOException {
		setup(80);

		PageFile pf = btree.getPageFile();
		assertEquals(1, pf.getPageCount());
		assertEquals(1, pf.getRootPage());

		RowEntry value = btree.get(new IntegerKey(1));
		assertNull(value);

		// three row can be added to 76 byte page
		for (int i = 1; i <= 3; i++) {
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));
			value = btree.get(new IntegerKey(i));
			assertEquals(new IntegerValue(i * 100), value);
		}
	}

	@Test
	public void testSimpleSplit() throws IOException {
		setup(80);

		PageFile pf = btree.getPageFile();
		assertEquals(1, pf.getPageCount());
		assertEquals(1, pf.getRootPage());

		RowEntry value = btree.get(new IntegerKey(1));
		assertNull(value);

		// three row can be added to 76 byte page
		for (int i = 1; i <= 7; i++) {
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));
			value = btree.get(new IntegerKey(i));
			assertEquals(new IntegerValue(i * 100), value);
		}

	}

	@Test
	public void testDescInsert() throws IOException {
		setup(80);

		PageFile pf = btree.getPageFile();
		assertEquals(1, pf.getPageCount());
		assertEquals(1, pf.getRootPage());

		RowEntry value = btree.get(new IntegerKey(1));
		assertNull(value);

		for (int i = 100; i > 0; i--) {
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));
			value = btree.get(new IntegerKey(i));
			assertEquals(new IntegerValue(i * 100), value);
		}
	}

	@Test
	public void testRandomOrder() throws IOException {
		setup(80);

		// 1, 3, 4, 6, 7, 8, 9, 10, 18
		btree.insert(new IntegerKey(6), new IntegerValue(600));
		btree.insert(new IntegerKey(7), new IntegerValue(700));
		btree.insert(new IntegerKey(8), new IntegerValue(800));
		btree.insert(new IntegerKey(9), new IntegerValue(900));
		btree.insert(new IntegerKey(10), new IntegerValue(1000));
		btree.insert(new IntegerKey(3), new IntegerValue(300));
		btree.insert(new IntegerKey(18), new IntegerValue(1800));
		btree.insert(new IntegerKey(4), new IntegerValue(400));
		btree.insert(new IntegerKey(1), new IntegerValue(100));

		btree.sync();

		assertEquals(new IntegerValue(600), btree.get(new IntegerKey(6)));
		assertEquals(new IntegerValue(700), btree.get(new IntegerKey(7)));
		assertEquals(new IntegerValue(800), btree.get(new IntegerKey(8)));
		assertEquals(new IntegerValue(900), btree.get(new IntegerKey(9)));
		assertEquals(new IntegerValue(1000), btree.get(new IntegerKey(10)));
		assertEquals(new IntegerValue(300), btree.get(new IntegerKey(3)));
		assertEquals(new IntegerValue(1800), btree.get(new IntegerKey(18)));
		assertEquals(new IntegerValue(400), btree.get(new IntegerKey(4)));
		assertEquals(new IntegerValue(100), btree.get(new IntegerKey(1)));
	}

	@Test
	public void testDuplicates() throws IOException {
		setup(80);

		for (int i = 1; i <= 5; i++) {
			btree.insert(new IntegerKey(5 + i), new IntegerValue(i));
		}

		btree.insert(new IntegerKey(3), new IntegerValue(300));
		btree.insert(new IntegerKey(18), new IntegerValue(800));
		btree.insert(new IntegerKey(4), new IntegerValue(400));

		btree.insert(new IntegerKey(1), new IntegerValue(11));
		btree.insert(new IntegerKey(1), new IntegerValue(12));
		btree.insert(new IntegerKey(1), new IntegerValue(13));
		btree.insert(new IntegerKey(1), new IntegerValue(14));
		btree.insert(new IntegerKey(1), new IntegerValue(15));
		btree.insert(new IntegerKey(1), new IntegerValue(16));
		btree.insert(new IntegerKey(1), new IntegerValue(17));
		btree.sync();

		// seven 1s, 3, 4, [6, 7, 8, 9, 10], 18

		// count all 1s over several pages
		assertEquals(7, count1s(Cursor.ASC));
		assertEquals(7, count1s(Cursor.DESC));

		// check contiguous numbers
		for (int i = 1; i <= 5; i++) {
			assertEquals(new IntegerValue(i), btree.get(new IntegerKey(5 + i)));
		}

		// check other out-of-order numbers
		assertEquals(new IntegerValue(300), btree.get(new IntegerKey(3)));
		assertEquals(new IntegerValue(800), btree.get(new IntegerKey(18)));
		assertEquals(new IntegerValue(400), btree.get(new IntegerKey(4)));
	}

	private int count1s(int cursorType) throws IOException {
		int count = 0;
		IntegerKey searchKey = new IntegerKey(1);
		Cursor cursor = btree.openCursor(searchKey, cursorType);
		do {
			if (searchKey.equals(cursor.getKey())) {
				count++;
			} else
				break;
		} while (cursor.next());

		cursor.close();
		return count;
	}

	@Test
	public void testIteration() throws IOException {
		setup(80);

		int index = 1;

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		// test ascending iteration
		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index++;
		} while (c.next());

		c.close();

		// test descending iteration
		index = 10;
		c = btree.openCursor(Cursor.DESC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index--;
		} while (c.next());

		c.close();
	}

	@Test
	public void testCompositeKey() throws IOException {
		setup(100, String.class, Integer.class);

		CompositeKey k2 = new CompositeKey("test", 2);
		btree.insert(k2, new IntegerValue(200));

		CompositeKey k3 = new CompositeKey("test", 3);
		btree.insert(k3, new IntegerValue(300));

		CompositeKey k1 = new CompositeKey("test", 1);
		btree.insert(k1, new IntegerValue(100));

		assertEquals(new IntegerValue(100), btree.get(k1));
		assertEquals(new IntegerValue(200), btree.get(k2));
		assertEquals(new IntegerValue(300), btree.get(k3));

		Cursor c = btree.openCursor(Cursor.ASC);
		assertEquals(new IntegerValue(100), c.getValue());
		c.close();

		c = btree.openCursor(Cursor.DESC);
		assertEquals(new IntegerValue(300), c.getValue());
		c.close();
	}

	@Test
	public void testSimpleDelete() throws IOException {
		setup(80);

		int index = 1;

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		// delete 2 of 3 elements of last (rightmost) page
		btree.delete(new IntegerKey(8));
		btree.delete(new IntegerKey(10));

		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index++;

			if (index == 8 || index == 10)
				index++;
		} while (c.next());

		c.close();
	}

	@Test
	public void testDeleteLastPage() throws IOException {
		setup(80);

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		// delete all elements of last (rightmost) page
		btree.delete(new IntegerKey(9));
		btree.delete(new IntegerKey(10));
		btree.delete(new IntegerKey(8));

		// assert
		int index = 1;
		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index++;
		} while (c.next());

		c.close();
	}

	@Test
	public void testDeleteLeftmostPage() throws IOException {
		setup(80);

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		btree.delete(new IntegerKey(1));

		int index = 2; // test 2~10
		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index++;
		} while (c.next());

		c.close();
	}

	@Test
	public void testDeleteLeftmostTree() throws IOException {
		setup(80);

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		btree.delete(new IntegerKey(1));
		btree.delete(new IntegerKey(2));
		btree.delete(new IntegerKey(3));

		int index = 4; // test 4~10
		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			assertEquals(new IntegerKey(index), c.getKey());
			assertEquals(new IntegerValue(index * 100), c.getValue());
			index++;
		} while (c.next());

		c.close();
	}

	@Test
	public void testDeleteRightChildKey() throws IOException {
		setup(80);

		for (int i = 1; i <= 10; i++)
			btree.insert(new IntegerKey(i), new IntegerValue(i * 100));

		// bug case: delete 3, but 1 and 2 also be inaccessible.
		btree.delete(new IntegerKey(3));

		btree.sync();
		trace();

		// int index = 1;
		Cursor c = btree.openCursor(Cursor.ASC);
		do {
			// should be 1, 2, 5, 6, 8
			System.out.println(c.getKey() + " " + c.getValue());
			// assertEquals(new IntegerKey(index), c.getKey());
			// assertEquals(new IntegerValue(index * 100), c.getValue());
			// index++;
		} while (c.next());

		c.close();
	}

	private void trace() throws IOException {
		PageFile pf = btree.getPageFile();
		System.out.println("ROOT=" + pf.getRootPage());
		for (int i = 1; i <= pf.getPageCount(); i++) {
			Page p = pf.read(i);
			System.out.println("Page " + i + ", Type=" + p.getFlag() + ", RightChild=" + p.getRightChildPage()
					+ ", Right=" + p.getRightPage() + ", Upper=" + p.getUpperPage());
			for (int r = 0; r < p.getRecordCount(); r++) {
				System.out.println(p.getKey(r) + ", value=" + p.getValue(r));
			}
			System.out.println();
		}
	}
}
