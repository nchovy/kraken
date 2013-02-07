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
import org.krakenapps.logdb.query.command.NumberUtil;
import org.krakenapps.logdb.query.expr.Expression;

public class Range implements AggregationFunction {
	private List<Expression> exprs;

	private Number min;
	private Number max;

	public Range(List<Expression> exprs) {
		this.exprs = exprs;
	}

	@Override
	public String getName() {
		return "range";
	}

	@Override
	public List<Expression> getArguments() {
		return exprs;
	}

	@Override
	public void apply(LogMap map) {
		Object obj = exprs.get(0).eval(map);
		min = NumberUtil.min(min, obj);
		max = NumberUtil.max(max, obj);
	}

	public Number getMin() {
		return min;
	}

	public void setMin(Number min) {
		this.min = min;
	}

	public Number getMax() {
		return max;
	}

	public void setMax(Number max) {
		this.max = max;
	}

	@Override
	public Object eval() {
		if (max == null && min == null)
			return null;

		return NumberUtil.sub(max, min);
	}

	@Override
	public void clean() {
		min = null;
		max = null;
	}

	@Override
	public AggregationFunction clone() {
		Range f = new Range(exprs);
		f.min = min;
		f.max = max;
		return f;
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[2];
		l[0] = min;
		l[1] = max;
		return l;
	}

	@Override
	public void deserialize(Object[] values) {
		min = (Number) values[0];
		max = (Number) values[1];
	}

	@Override
	public void merge(AggregationFunction func) {
		Range other = (Range) func;
		this.min = NumberUtil.min(min, other.min);
		this.max = NumberUtil.max(max, other.max);
	}
}
