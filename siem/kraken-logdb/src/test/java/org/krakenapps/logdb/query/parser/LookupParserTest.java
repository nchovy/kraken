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
import org.krakenapps.logdb.query.command.Lookup;

public class LookupParserTest {
	@Test
	public void testSimpleCase() {
		// lookup <name> <lookup key field> OUTPUT <lookup target field>
		LookupParser p = new LookupParser(null);
		Lookup lookup = (Lookup) p.parse(null, "lookup code output auth_code_desc");
		assertEquals("code", lookup.getSourceField());
		assertEquals("code", lookup.getLookupInputField());
		assertEquals("auth_code_desc", lookup.getLookupOutputField());
		assertEquals("auth_code_desc", lookup.getTargetField());
	}

	@Test
	public void testFullArgs() {
		// lookup <name> <lookup key field> AS <renamed field> OUTPUT <lookup
		// target field> AS <renamed target name>
		LookupParser p = new LookupParser(null);
		Lookup lookup = (Lookup) p.parse(null, "lookup code AS in OUTPUT auth_code_desc as out");
		assertEquals("code", lookup.getSourceField());
		assertEquals("in", lookup.getLookupInputField());
		assertEquals("auth_code_desc", lookup.getLookupOutputField());
		assertEquals("out", lookup.getTargetField());
	}
}
