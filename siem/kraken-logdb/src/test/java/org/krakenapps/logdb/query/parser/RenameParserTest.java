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

import static org.junit.Assert.*;

import org.junit.Test;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Rename;

public class RenameParserTest {
	@Test
	public void testParse() {
		RenameParser p = new RenameParser();
		Rename rename = (Rename) p.parse(null, "rename sent as Sent");
		assertEquals("sent", rename.getFrom());
		assertEquals("Sent", rename.getTo());
	}

	@Test
	public void testBrokenCase1() {
		RenameParser p = new RenameParser();
		try {
			p.parse(null, "rename sent");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("as-token-not-found", e.getType());
			assertEquals(11, (int) e.getOffset());
		}
	}

	@Test
	public void testBrokenCase2() {
		RenameParser p = new RenameParser();
		try {
			p.parse(null, "rename sent as ");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("to-field-not-found", e.getType());
			assertEquals(15, (int) e.getOffset());
		}
	}
}
