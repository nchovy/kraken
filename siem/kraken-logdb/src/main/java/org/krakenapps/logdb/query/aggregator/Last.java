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
import org.krakenapps.logdb.query.expr.Expression;

public class Last implements AggregationFunction {
	private List<Expression> exprs;
	private Object last;

	public Last(List<Expression> exprs) {
		this.exprs = exprs;
	}

	@Override
	public String getName() {
		return "last";
	}

	@Override
	public List<Expression> getArguments() {
		return exprs;
	}

	@Override
	public void apply(LogMap map) {
		Object obj = exprs.get(0).eval(map);
		if (obj != null)
			last = obj;
	}

	public Object getLast() {
		return last;
	}

	public void setLast(Object last) {
		this.last = last;
	}

	@Override
	public Object eval() {
		return last;
	}

	@Override
	public void clean() {
		last = null;
	}

	@Override
	public AggregationFunction clone() {
		Last f = new Last(exprs);
		f.last = last;
		return f;
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[1];
		l[0] = last;
		return l;
	}

	@Override
	public void deserialize(Object[] values) {
		last = values[0];
	}

	@Override
	public void merge(AggregationFunction func) {
		Last last = (Last) func;
		this.last = last.last;
	}
}
