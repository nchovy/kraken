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
import static org.junit.Assert.*;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Fields;

public class FieldsParserTest {
	@Test
	public void testSelectSingleField() {
		FieldsParser p = new FieldsParser();
		Fields fields = (Fields) p.parse(null, "fields sport");

		assertEquals(1, fields.getFields().size());
		assertTrue(fields.isSelector());
		assertEquals("sport", fields.getFields().get(0));
	}

	@Test
	public void testSelectMultiFields() {
		FieldsParser p = new FieldsParser();
		Fields fields = (Fields) p.parse(null, "fields sip,sport, dip, dport ");

		assertEquals(4, fields.getFields().size());
		assertTrue(fields.isSelector());
		assertEquals("sip", fields.getFields().get(0));
		assertEquals("sport", fields.getFields().get(1));
		assertEquals("dip", fields.getFields().get(2));
		assertEquals("dport", fields.getFields().get(3));
	}

	@Test
	public void testFilterFields() {
		FieldsParser p = new FieldsParser();
		Fields fields = (Fields) p.parse(null, "fields - note,user ");

		assertEquals(2, fields.getFields().size());
		assertFalse(fields.isSelector());
		assertEquals("note", fields.getFields().get(0));
		assertEquals("user", fields.getFields().get(1));
	}

	@Test
	public void testBrokenFields() {
		try {
			FieldsParser p = new FieldsParser();
			p.parse(null, "fields - ");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("no-field-args", e.getType());
		}
	}
}
