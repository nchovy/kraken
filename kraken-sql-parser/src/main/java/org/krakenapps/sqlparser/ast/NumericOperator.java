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

public enum NumericOperator {
	Plus("+"), Minus("-"), Multiply("*"), Divide("/");

	NumericOperator(String symbol) {
		this.symbol = symbol;
	}

	private String symbol;

	public static NumericOperator parse(String symbol) {
		for (NumericOperator op : values()) {
			if (op.toString().equals(symbol))
				return op;
		}

		throw new IllegalArgumentException("unsupported operator: " + symbol);
	}

	@Override
	public String toString() {
		return symbol;
	}

}
