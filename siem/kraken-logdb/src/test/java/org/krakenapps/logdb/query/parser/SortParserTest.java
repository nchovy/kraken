/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.query.parser;

import org.junit.Test;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Sort;

import static org.junit.Assert.*;

public class SortParserTest {
	@Test
	public void testSingleColumn() {
		String command = "sort field1";

		Sort sort = (Sort) new SortParser().parse(null, command);
		assertEquals(1, sort.getFields().length);
		assertEquals("field1", sort.getFields()[0].getName());
		assertEquals(true, sort.getFields()[0].isAsc());
		assertNull(sort.getLimit());
	}

	@Test
	public void testMultiColumns() {
		String command = "sort field1,field2, field3";

		Sort sort = (Sort) new SortParser().parse(null, command);
		assertEquals(3, sort.getFields().length);
		assertEquals("field1", sort.getFields()[0].getName());
		assertEquals(true, sort.getFields()[0].isAsc());
		assertEquals("field2", sort.getFields()[1].getName());
		assertEquals(true, sort.getFields()[1].isAsc());
		assertEquals("field3", sort.getFields()[2].getName());
		assertEquals(true, sort.getFields()[2].isAsc());
		assertNull(sort.getLimit());
	}

	@Test
	public void testOrder() {
		String command = "sort -field1,+field2, field3";

		Sort sort = (Sort) new SortParser().parse(null, command);
		assertEquals(3, sort.getFields().length);
		assertEquals("field1", sort.getFields()[0].getName());
		assertEquals(false, sort.getFields()[0].isAsc());
		assertEquals("field2", sort.getFields()[1].getName());
		assertEquals(true, sort.getFields()[1].isAsc());
		assertEquals("field3", sort.getFields()[2].getName());
		assertEquals(true, sort.getFields()[2].isAsc());
		assertNull(sort.getLimit());
	}

	@Test
	public void testLimitAndSingleColumn() {
		String command = "sort limit=10 -field1";

		Sort sort = (Sort) new SortParser().parse(null, command);
		assertEquals(1, sort.getFields().length);
		assertEquals("field1", sort.getFields()[0].getName());
		assertEquals(false, sort.getFields()[0].isAsc());
		assertEquals(10, (int) sort.getLimit());
	}

	@Test
	public void testComplexCase() {
		String command = "sort limit=20 field1, +field2,-field3";

		Sort sort = (Sort) new SortParser().parse(null, command);
		assertEquals(3, sort.getFields().length);

		assertEquals("field1", sort.getFields()[0].getName());
		assertEquals(true, sort.getFields()[0].isAsc());

		assertEquals("field2", sort.getFields()[1].getName());
		assertEquals(true, sort.getFields()[1].isAsc());

		assertEquals("field3", sort.getFields()[2].getName());
		assertEquals(false, sort.getFields()[2].isAsc());

		assertEquals(20, (int) sort.getLimit());
	}

	@Test
	public void testBrokenCase() {
		String command = "sort limit=20";

		try {
			new SortParser().parse(null, command);
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("need-column", e.getType());
			assertEquals(13, (int) e.getOffset());
		}
	}

	@Test
	public void testBrokenCase2() {
		String command = "sort ";

		try {
			new SortParser().parse(null, command);
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("need-column", e.getType());
			assertEquals(4, (int) e.getOffset());
		}
	}
}
