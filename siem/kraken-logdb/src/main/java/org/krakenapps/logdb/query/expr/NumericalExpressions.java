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
package org.krakenapps.logdb.query.expr;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.command.NumberUtil;

public class NumericalExpressions {

	public static class Neg implements Expression {
		private Expression expr;

		public Neg(Expression expr) {
			this.expr = expr;
		}

		@Override
		public Object eval(LogMap map) {
			Object o = expr.eval(map);
			if (o == null)
				return null;

			if (o instanceof Integer)
				return -(Integer) o;
			if (o instanceof Long)
				return -(Long) o;
			if (o instanceof Double)
				return -(Double) o;
			if (o instanceof Float)
				return -(Float) o;
			return null;
		}

		@Override
		public String toString() {
			return "-" + expr;
		}
	}

	public static abstract class BinaryExpression implements Expression {
		protected Expression lhs;
		protected Expression rhs;

		public BinaryExpression(Expression lhs, Expression rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}
	}

	public static class Add extends BinaryExpression {
		public Add(Expression lhs, Expression rhs) {
			super(lhs, rhs);
		}

		@Override
		public Object eval(LogMap map) {
			return NumberUtil.add(lhs.eval(map), rhs.eval(map));
		}

		@Override
		public String toString() {
			return "(" + lhs + " + " + rhs + ")";
		}
	}

	public static class Sub extends BinaryExpression {
		public Sub(Expression lhs, Expression rhs) {
			super(lhs, rhs);
		}

		@Override
		public Object eval(LogMap map) {
			return NumberUtil.sub(lhs.eval(map), rhs.eval(map));
		}

		@Override
		public String toString() {
			return "(" + lhs + " - " + rhs + ")";
		}
	}

	public static class Mul extends BinaryExpression {
		public Mul(Expression lhs, Expression rhs) {
			super(lhs, rhs);
		}

		@Override
		public Object eval(LogMap map) {
			return NumberUtil.mul(lhs.eval(map), rhs.eval(map));
		}

		@Override
		public String toString() {
			return "(" + lhs + " * " + rhs + ")";
		}
	}

	public static class Div extends BinaryExpression {
		public Div(Expression lhs, Expression rhs) {
			super(lhs, rhs);
		}

		@Override
		public Object eval(LogMap map) {
			return NumberUtil.div(lhs.eval(map), rhs.eval(map));
		}

		@Override
		public String toString() {
			return "(" + lhs + " / " + rhs + ")";
		}
	}
}
