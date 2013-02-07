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
import org.krakenapps.logdb.query.aggregator.AggregationField;
import org.krakenapps.logdb.query.command.Stats;

public class StatsParserTest {
	@Test
	public void testCount() {
		StatsParser p = new StatsParser();
		Stats stats = (Stats) p.parse(null, "stats count");
		assertEquals(1, stats.getAggregationFields().size());
		assertEquals("count", stats.getAggregationFields().get(0).getName());

		// abbreviation form
		stats = (Stats) p.parse(null, "stats c");
		assertEquals(1, stats.getAggregationFields().size());
		assertEquals("count", stats.getAggregationFields().get(0).getName());
	}

	@Test
	public void testSumMin() {
		StatsParser p = new StatsParser();
		Stats stats = (Stats) p.parse(null, "stats sum(min(10000, sport)) as foo");
		AggregationField field = stats.getAggregationFields().get(0);
		assertEquals("foo", field.getName());
	}

	@Test
	public void testSingleClauses() {
		StatsParser p = new StatsParser();
		Stats stats = (Stats) p.parse(null, "stats sum(rcvd) by sip");
		assertEquals(1, stats.getAggregationFields().size());
		assertEquals("sum", stats.getAggregationFields().get(0).getName());
	}

	@Test
	public void testMultiAggregationsAndClauses() {
		StatsParser p = new StatsParser();
		Stats stats = (Stats) p.parse(null, "stats sum(rcvd) as rcvd, sum(sent) as sent by sip, dip");
		assertEquals(2, stats.getAggregationFields().size());
		assertEquals(2, stats.getClauses().size());
		assertEquals("rcvd", stats.getAggregationFields().get(0).getName());
		assertEquals("sent", stats.getAggregationFields().get(1).getName());
		assertEquals("sip", stats.getClauses().get(0));
		assertEquals("dip", stats.getClauses().get(1));
	}

	@Test
	public void testMissingClause() {
		StatsParser p = new StatsParser();
		try {
			p.parse(null, "stats sum(rcvd) as rcvd, sum(sent) as sent by sip,");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("missing-clause", e.getType());
			assertEquals(50, (int) e.getOffset());
		}

	}
}
