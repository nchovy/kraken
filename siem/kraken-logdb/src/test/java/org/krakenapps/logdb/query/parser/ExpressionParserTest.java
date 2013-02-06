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

import java.util.Calendar;

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

	@Test
	public void testGreaterThanEqual() {
		Expression exp = ExpressionParser.parse("10 >= 3");
		assertTrue((Boolean) exp.eval(null));

		exp = ExpressionParser.parse("3 >= 3");
		assertTrue((Boolean) exp.eval(null));
	}

	@Test
	public void testGreaterThan() {
		Expression exp = ExpressionParser.parse("10 > 3");
		assertTrue((Boolean) exp.eval(null));

		exp = ExpressionParser.parse("3 > 3");
		assertFalse((Boolean) exp.eval(null));
	}

	@Test
	public void testLesserThanEqual() {
		Expression exp = ExpressionParser.parse("3 <= 5");
		assertTrue((Boolean) exp.eval(null));

		exp = ExpressionParser.parse("3 <= 3");
		assertTrue((Boolean) exp.eval(null));
	}

	@Test
	public void testLesserThan() {
		Expression exp = ExpressionParser.parse("3 < 5");
		assertTrue((Boolean) exp.eval(null));

		exp = ExpressionParser.parse("3 < 3");
		assertFalse((Boolean) exp.eval(null));
	}

	@Test
	public void testBooleanArithmeticPrecendence() {
		Expression exp = ExpressionParser.parse("1 == 3-2 or 2 == 2");
		assertTrue((Boolean) exp.eval(null));
	}

	@Test
	public void testEq() {
		Expression exp = ExpressionParser.parse("1 == 0");
		assertFalse((Boolean) exp.eval(null));
	}

	@Test
	public void testAnd() {
		Expression exp = ExpressionParser.parse("10 >= 3 and 1 == 0");
		assertFalse((Boolean) exp.eval(null));
	}

	@Test
	public void testAndOr() {
		Expression exp = ExpressionParser.parse("10 >= 3 and (1 == 0 or 2 == 2)");
		assertTrue((Boolean) exp.eval(null));
	}

	@Test
	public void testIf() {
		Expression exp = ExpressionParser.parse("if(field >= 10, 10, field)");

		LogMap m1 = new LogMap();
		m1.put("field", 15);
		assertEquals(10L, exp.eval(m1));

		LogMap m2 = new LogMap();
		m2.put("field", 3);
		assertEquals(3, exp.eval(m2));
	}

	@Test
	public void testCase() {
		Expression exp = ExpressionParser.parse("case(field >= 10, 10, field < 10, field)");

		LogMap m1 = new LogMap();
		m1.put("field", 15);
		assertEquals(10L, exp.eval(m1));

		LogMap m2 = new LogMap();
		m2.put("field", 3);
		assertEquals(3, exp.eval(m2));
	}

	@Test
	public void testConcat() {
		Expression exp = ExpressionParser.parse("concat(\"hello\", \"world\")");
		assertEquals("helloworld", exp.eval(null));
	}

	@Test
	public void testToDate() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, 1);
		c.set(Calendar.DAY_OF_MONTH, 6);
		c.set(Calendar.HOUR_OF_DAY, 11);
		c.set(Calendar.MINUTE, 26);
		c.set(Calendar.SECOND, 33);
		c.set(Calendar.MILLISECOND, 0);

		LogMap m = new LogMap();
		m.put("date", "2013-02-06 11:26:33");

		Expression exp = ExpressionParser.parse("date(date, \"yyyy-MM-dd HH:mm:ss\")");
		Object v = exp.eval(m);
		assertEquals(c.getTime(), v);
	}

	@Test
	public void testSubstr() {
		String s = "abcdefg";

		LogMap m = new LogMap();
		m.put("line", s);

		Expression exp = ExpressionParser.parse("substr(line,0,7)");
		assertEquals("abcdefg", exp.eval(m));

		exp = ExpressionParser.parse("substr(line,0,0)");
		assertEquals("", exp.eval(m));

		exp = ExpressionParser.parse("substr(line,8,10)");
		assertNull(exp.eval(m));

		exp = ExpressionParser.parse("substr(line,3,6)");
		assertEquals("def", exp.eval(m));
	}

}
