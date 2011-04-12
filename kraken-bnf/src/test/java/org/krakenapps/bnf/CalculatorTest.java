/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.bnf;

import static org.krakenapps.bnf.Syntax.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CalculatorTest {
	@SuppressWarnings("unchecked")
	@Test
	public void test() throws ParseException {
		Syntax s = new Syntax();
		s.add("expr", new ExprParser(), ref("factor"), option(repeat(rule(choice(t("+"), t("-")), ref("factor")))));
		s.add("factor", new FactorParser(), uint());
		s.addRoot("expr");

		Expr expr = (Expr) s.eval("1");
		assertEquals(1, expr.eval());

		List<Expr> exprs = (List<Expr>) s.eval("1 + 2 - 3 - 4");
		System.out.println(exprs);

		int eval = 0;
		for (Expr e : exprs) {
			eval += e.eval();
		}

		assertEquals(-4, eval);
	}

	private static class Expr {
		private Integer value;
		private String sign;
		private Expr expr;

		public Expr(int value) {
			this.value = value;
		}

		public Expr(String sign, Expr expr) {
			this.sign = sign;
			this.expr = expr;
		}

		public int eval() {
			if (value != null)
				return value;

			return sign.equals("-") ? -expr.eval() : expr.eval();
		}

		@Override
		public String toString() {
			if (value != null)
				return value.toString();

			return (sign.equals("-") ? "-" : "") + expr;
		}
	}

	private static class ExprParser implements Parser {
		@Override
		public Object parse(Binding b) {
			List<Expr> exprs = new ArrayList<Expr>();
			if (b.getChildren() == null)
				return b.getValue();

			exprs.add((Expr) b.getChildren()[0].getValue());
			trace(b.getChildren()[1], exprs);
			return exprs;
		}

		private void trace(Binding b, List<Expr> exprs) {
			Binding[] children = b.getChildren();

			if (children[0].getChildren() == null) {
				String sign = (String) children[0].getValue();
				Expr expr = (Expr) children[1].getValue();
				exprs.add(new Expr(sign, expr));
				return;
			}

			String sign = (String) children[0].getChildren()[0].getValue();
			Expr expr = (Expr) children[0].getChildren()[1].getValue();
			exprs.add(new Expr(sign, expr));

			trace(children[1], exprs);
		}
	}

	private static class FactorParser implements Parser {
		@Override
		public Object parse(Binding b) {
			return new Expr((Integer) b.getValue());
		}
	}
}
