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
import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.aggregator.AggregationField;
import org.krakenapps.logdb.query.command.Timechart;
import org.krakenapps.logdb.query.command.Timechart.TimeUnit;
import org.krakenapps.logdb.query.expr.Expression;

import static org.junit.Assert.*;

public class TimechartParserTest {
	@Test
	public void testInsufficientCommand() {
		TimechartParser p = new TimechartParser();
		try {
			p.parse(null, "timechart");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("need-aggregation-field", e.getType());
		}
	}

	@Test
	public void testMostSimpleCase() {
		TimechartParser p = new TimechartParser();
		Timechart tc = (Timechart) p.parse(null, "timechart count");

		assertEquals(1, tc.getAggregationFields().size());
		assertEquals("count", tc.getAggregationFields().get(0).getName());
		assertEquals(1, tc.getTimeSpan().amount);
		assertEquals(TimeUnit.Day, tc.getTimeSpan().unit);
	}

	@Test
	public void testCount() {
		TimechartParser p = new TimechartParser();
		Timechart tc = (Timechart) p.parse(null, "timechart span=1d count");

		assertEquals(1, tc.getAggregationFields().size());
		assertEquals("count", tc.getAggregationFields().get(0).getName());
		assertEquals(1, tc.getTimeSpan().amount);
		assertEquals(TimeUnit.Day, tc.getTimeSpan().unit);
	}

	@Test
	public void testCountWithClause() {
		TimechartParser p = new TimechartParser();
		Timechart tc = (Timechart) p.parse(null, "timechart span=1d count by sip");

		assertEquals(1, tc.getAggregationFields().size());
		assertEquals("count", tc.getAggregationFields().get(0).getName());
		assertEquals(1, tc.getTimeSpan().amount);
		assertEquals(TimeUnit.Day, tc.getTimeSpan().unit);
		assertEquals("sip", tc.getKeyField());
	}

	@Test
	public void testNestedSum() {
		TimechartParser p = new TimechartParser();
		Timechart tc = (Timechart) p.parse(null, "timechart span=1m sum(sport / 2)");

		AggregationField agg = tc.getAggregationFields().get(0);
		assertEquals(1, tc.getAggregationFields().size());
		assertEquals("sum", agg.getName());

		Expression arg1 = agg.getFunction().getArguments().get(0);
		LogMap m = new LogMap();
		m.put("sport", 100);
		assertEquals(50.0, arg1.eval(m));
	}

}
