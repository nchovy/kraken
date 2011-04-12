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
package org.krakenapps.sqlparser.ast;

public class NumericValueExpression implements RowValueConstructorElement {
	private Number value;

	private boolean negated;
	private NumericValueExpression expr;

	private NumericValueExpression lhs;
	private NumericOperator op;
	private NumericValueExpression rhs;

	public NumericValueExpression(Number value) {
		this.value = value;
	}

	public NumericValueExpression(NumericValueExpression expr) {
		this.expr = expr;
	}

	public NumericValueExpression(NumericValueExpression expr, boolean negate) {
		this.expr = expr;
		this.negated = negate;
	}

	public NumericValueExpression(NumericValueExpression lhs, NumericOperator op, NumericValueExpression rhs) {
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
	}

	public Number getValue() {
		return value;
	}

	public boolean isNegated() {
		return negated;
	}

	@Override
	public String toString() {
		if (value != null)
			return value.toString();
		else if (value == null && expr == null)
			return sign() + "(" + lhs + " " + op + " " + rhs + ")";
		else
			return sign() + expr;
	}

	private String sign() {
		return negated ? "-" : "";
	}
}
