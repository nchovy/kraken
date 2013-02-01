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

public class Count implements AggregationFunction {
	private List<Expression> exprs;
	private long result = 0;

	public Count(List<Expression> exprs) {
		this.exprs = exprs;
	}

	@Override
	public List<Expression> getArguments() {
		return exprs;
	}

	@Override
	public String getName() {
		return "count";
	}

	@Override
	public void apply(LogMap map) {
		result++;
	}

	@Override
	public Object eval() {
		return result;
	}

	@Override
	public void clean() {
		result = 0;
	}

	@Override
	public AggregationFunction clone() {
		Count c = new Count(exprs);
		c.result = result;
		return c;
	}

	@Override
	public void merge(AggregationFunction func) {
		Count c = (Count) func;
		this.result += c.result;
	}

	@Override
	public void deserialize(Object[] values) {
		result = (Long) values[0];
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[1];
		l[0] = result;
		return l;
	}

}
