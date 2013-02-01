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
import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.expr.Expression;

public class ExpressionParserTest {
	@Test
	public void testSimple() {
		Expression expr = ExpressionParser.parse("3+4*2/(1-5)");
		Object v = expr.eval(null);
		assertEquals(1.0, v);
	}

	@Test
	public void testFuncExpr() {
		Expression expr = ExpressionParser.parse("1 + abs(1-5*2)");
		Object v = expr.eval(null);
		assertEquals(10L, v);
	}

	@Test
	public void testFuncMultiArgs() {
		LogMap log = new LogMap();
		log.put("test", 1);

		Expression expr = ExpressionParser.parse("100 + min(3, 7, 2, 5, test) * 2");
		Object v = expr.eval(log);
		assertEquals(102L, v);
	}

	@Test
	public void testNestedFuncExpr() {
		Expression expr = ExpressionParser.parse("min(abs(1-9), 3, 10, 5)");
		Object v = expr.eval(null);
		assertEquals(3L, v);
	}

	@Test
	public void testNegation() {
		Expression expr = ExpressionParser.parse("-abs(1-9) * 2");
		Object v = expr.eval(null);
		assertEquals(-16L, v);
	}

	@Test
	public void testBrokenExpr() {
		try {
			ExpressionParser.parse("3+4*2/");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("broken-expression", e.getType());
		}
	}

}
