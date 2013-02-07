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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class QueryTokenizerTest {
	@Test
	public void testParseCommands() {
		String q = "table limit=1000000 local\\arko-guro | search sip contain \"10.1.\" | stats count by sip";
		List<String> commands = QueryTokenizer.parseCommands(q);
		for (String c : commands)
			System.out.println(c);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOptions() {
		String query = "textfile offset=1 limit=10 sample.log";

		ParseResult r = QueryTokenizer.parseOptions(query, "textfile".length());
		Map<String, String> options = (Map<String, String>) r.value;

		assertEquals("1", options.get("offset"));
		assertEquals("10", options.get("limit"));
	}

	@Test
	public void testCsvParse() {
		int p = QueryTokenizer.findKeyword("sum(min(1, 2))", ",");
		assertEquals(-1, p);

		p = QueryTokenizer.findKeyword("sum(min(1, 2)), 1", ",");
		assertEquals(14, p);
	}
}
