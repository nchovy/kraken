package org.krakenapps.logdb.query.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.command.Eval;

public class EvalParserTest {
	@Test
	public void testConstantExpr() {
		EvalParser p = new EvalParser();
		Eval eval = (Eval) p.parse(null, "eval n=1+2+min(10, 4, 34, -9)");
		Object o = eval.getExpression().eval(null);
		assertEquals(-6L, o);
	}

	@Test
	public void testBrokenEval1() {
		EvalParser p = new EvalParser();
		try {
			p.parse(null, "eval ");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("assign-token-not-found", e.getType());
		}
	}

	@Test
	public void testBrokenEval2() {
		EvalParser p = new EvalParser();
		try {
			p.parse(null, "eval =");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("field-name-not-found", e.getType());
		}
	}

	@Test
	public void testBrokenEval3() {
		EvalParser p = new EvalParser();
		try {
			p.parse(null, "eval n=");
			fail();
		} catch (LogQueryParseException e) {
			assertEquals("expression-not-found", e.getType());
		}
	}
}
