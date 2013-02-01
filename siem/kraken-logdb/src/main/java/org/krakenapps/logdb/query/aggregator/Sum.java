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

public class Sum implements AggregationFunction {
	protected List<Expression> exprs;
	protected Number sum = 0L;
	private Expression expr;

	public Sum(List<Expression> exprs) {
		this.exprs = exprs;
		this.expr = exprs.get(0);
	}

	@Override
	public String getName() {
		return "sum";
	}

	@Override
	public List<Expression> getArguments() {
		return exprs;
	}

	@Override
	public void apply(LogMap map) {
		Object obj = expr.eval(map);
		sum = NumberUtil.add(sum, obj);
	}

	@Override
	public Object eval() {
		return sum;
	}

	@Override
	public void merge(AggregationFunction func) {
		Sum other = (Sum) func;
		this.sum = NumberUtil.add(sum, other.sum);
	}

	@Override
	public void deserialize(Object[] values) {
		this.sum = (Number) values[0];
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[1];
		l[0] = sum;
		return l;
	}

	@Override
	public void clean() {
		sum = null;
	}

	@Override
	public AggregationFunction clone() {
		Sum s = new Sum(this.exprs);
		s.sum = this.sum;
		return s;
	}
}
