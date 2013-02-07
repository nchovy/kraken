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
package org.krakenapps.logdb.query.aggregator;

import java.util.List;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.expr.Expression;

public class First implements AggregationFunction {
	private List<Expression> exprs;
	private Object first;

	public First(List<Expression> exprs) {
		if (exprs.size() != 1) {
			String note = exprs.size() + " parameters to first function";
			throw new LogQueryParseException("invalid-parameter-count", -1, note);
		}

		this.exprs = exprs;
	}

	@Override
	public String getName() {
		return "first";
	}

	@Override
	public List<Expression> getArguments() {
		return exprs;
	}

	@Override
	public void apply(LogMap map) {
		Object obj = exprs.get(0).eval(map);

		if (first == null && obj != null)
			first = obj;
	}

	public Object getFirst() {
		return first;
	}

	public void setFirst(Object first) {
		this.first = first;
	}

	@Override
	public Object eval() {
		return first;
	}

	@Override
	public void clean() {
		first = null;
	}

	@Override
	public AggregationFunction clone() {
		First f = new First(exprs);
		f.first = first;
		return f;
	}

	@Override
	public void merge(AggregationFunction func) {
		// ignore subsequent items
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[1];
		l[0] = first;
		return l;
	}

	@Override
	public void deserialize(Object[] values) {
		first = values[0];
	}
}
