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

import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import org.krakenapps.logdb.query.command.Search;
import org.krakenapps.logdb.query.command.Term;
import org.krakenapps.logdb.query.command.Term.Operator;

public class SearchParserTest {
	@Test
	public void testContain() {
		SearchParser p = new SearchParser();
		Search search = (Search) p.parse(null, "search sip contain \"10.1.\" ");
		List<Term> terms = search.getTerms();

		Term term = terms.get(0);
		assertEquals("sip", term.getLh());
		assertEquals("10.1.", term.getRh());
		assertFalse(term.isLhString());
		assertTrue(term.isRhString());
		assertEquals(Operator.Contain, term.getOperator());
	}
}
