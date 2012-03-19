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

import org.junit.Test;
import org.krakenapps.btree.types.IntegerKey;
import org.krakenapps.btree.types.IntegerKeyFactory;
import org.krakenapps.btree.types.IntegerValue;
import org.krakenapps.btree.types.IntegerValueFactory;

import static org.junit.Assert.*;

public class PageTest {

	@Test
	public void orderedInsert() {
		Schema schema = createSchema();

		Page p = new Page(schema);
		p.insert(new IntegerKey(1), new IntegerValue(100));
		p.insert(new IntegerKey(2), new IntegerValue(200));
		p.insert(new IntegerKey(3), new IntegerValue(300));
		p.insert(new IntegerKey(4), new IntegerValue(400));

		assertEquals(4, p.getRecordCount());

		for (int i = 0; i < p.getRecordCount(); i++)
			assertEquals(new IntegerValue((i + 1) * 100), p.getValue(i));
	}

	@Test
	public void unorderedInsert() {
		Schema schema = createSchema();

		Page p = new Page(schema);

		p.insert(new IntegerKey(4), new IntegerValue(400));
		p.insert(new IntegerKey(2), new IntegerValue(200));
		p.insert(new IntegerKey(3), new IntegerValue(300));
		p.insert(new IntegerKey(1), new IntegerValue(100));

		assertEquals(4, p.getRecordCount());

		for (int i = 0; i < p.getRecordCount(); i++)
			assertEquals(new IntegerValue((i + 1) * 100), p.getValue(i));
	}

	@Test
	public void duplicatedInsert() {
		Schema schema = createSchema();

		Page p = new Page(schema);

		p.insert(new IntegerKey(1), new IntegerValue(100));
		p.insert(new IntegerKey(4), new IntegerValue(400));
		p.insert(new IntegerKey(2), new IntegerValue(200));
		p.insert(new IntegerKey(3), new IntegerValue(300));
		p.insert(new IntegerKey(3), new IntegerValue(301));

		assertEquals(5, p.getRecordCount());

		for (int i = 0; i < p.getRecordCount(); i++) {
			int key = ((IntegerKey) p.getKey(i)).getValue();
			assertEquals(key, ((IntegerValue) p.getValue(i)).getValue() / 100);
		}
	}

	@Test
	public void testFreeSpace() {
		Schema schema = createSchema();

		Page p = new Page(schema);

		// capacity: 1024, page header: 26, slot metadata: 4,
		// record header: 2, record data: 8
		p.insert(new IntegerKey(4), new IntegerValue(400));
		assertEquals(984, p.getFreeSpace());

		// capacity: 1024, page header: 26, slot metadata: 4 * 2,
		// record header: 2 * 2, record data: 8 * 2
		p.insert(new IntegerKey(3), new IntegerValue(300));
		assertEquals(970, p.getFreeSpace());
	}

	@Test
	public void testLowSpace() {
		Schema schema = createSchema(62);
		Page p = new Page(schema);
		boolean ret = false;

		// one record = 20 bytes = slot 8 + record header 4 + data 8
		ret = p.insert(new IntegerKey(4), new IntegerValue(400));
		assertTrue(ret);
		ret = p.insert(new IntegerKey(3), new IntegerValue(300));
		assertTrue(ret);

		// will be unavailable
		ret = p.insert(new IntegerKey(2), new IntegerValue(200));
		assertFalse(ret);
	}

	@Test
	public void testDeleteSlot() {
		Schema schema = createSchema(62);
		Page p = new Page(schema);
		boolean ret = false;

		ret = p.insert(new IntegerKey(4), new IntegerValue(400));
		assertTrue(ret);
		ret = p.insert(new IntegerKey(3), new IntegerValue(300));
		assertTrue(ret);

		RowEntry entry = p.getValue(0);
		assertEquals(new IntegerValue(300), entry);

		ret = p.delete(2);
		assertFalse(ret);
		assertEquals(2, p.getRecordCount());

		ret = p.delete(0);
		assertTrue(ret);
		assertEquals(1, p.getRecordCount());

		assertEquals(new IntegerValue(400), p.getValue(0));
		assertNull(p.getValue(1));
	}

	@Test
	public void testDeleteLastSlot() {
		Schema schema = createSchema(100);
		Page p = new Page(schema);
		boolean ret = false;

		ret = p.insert(new IntegerKey(4), new IntegerValue(400));
		assertTrue(ret);
		ret = p.insert(new IntegerKey(2), new IntegerValue(200));
		assertTrue(ret);
		ret = p.insert(new IntegerKey(3), new IntegerValue(300));
		assertTrue(ret);

		// before delete
		RowEntry entry = p.getValue(2);
		assertEquals(new IntegerValue(400), entry);

		ret = p.delete(2);
		assertTrue(ret);
		assertEquals(2, p.getRecordCount());

		// after delete
		assertEquals(new IntegerValue(200), p.getValue(0));
		assertEquals(new IntegerValue(300), p.getValue(1));
		assertNull(p.getValue(2));
	}

	private Schema createSchema() {
		return createSchema(1024);
	}

	private Schema createSchema(int capacity) {
		Schema schema = new Schema(capacity, new Class<?>[] { Integer.class });
		schema.setRowKeyFactory(new IntegerKeyFactory());
		schema.setRowValueFactory(new IntegerValueFactory());
		return schema;
	}

	public void trace(Page p) {
		for (int i = 0; i < p.getRecordCount(); i++)
			System.out.println(p.getValue(i));
	}

}
