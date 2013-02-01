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
