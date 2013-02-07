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

import org.krakenapps.logdb.query.command.NumberUtil;
import org.krakenapps.logdb.query.expr.Expression;

public abstract class PerTime extends Sum {

	private long amount;

	public PerTime(List<Expression> exprs) {
		super(exprs);
	}

	abstract protected long getTimeLength();

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	@Override
	public Object eval() {
		if (super.eval() == null)
			return null;
		return NumberUtil.div(super.eval(), (double) amount / (double) getTimeLength());
	}

	@Override
	public Object[] serialize() {
		Object[] l = new Object[2]; // including sum field
		l[0] = super.sum;
		l[1] = amount;
		return l;
	}

	@Override
	public void deserialize(Object[] values) {
		super.deserialize(values);
		this.amount = (Long) values[4];
	}

	@Override
	public void merge(AggregationFunction func) {
		PerTime other = (PerTime) func;
		if (this.amount == 0)
			this.amount = other.amount;
	}
}
