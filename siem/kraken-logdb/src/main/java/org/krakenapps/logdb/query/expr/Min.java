package org.krakenapps.logdb.query.expr;

import java.util.List;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.ObjectComparator;

public class Min implements Expression {
	private ObjectComparator cmp = new ObjectComparator();
	private List<Expression> exprs;

	public Min(List<Expression> exprs) {
		this.exprs = exprs;
	}

	@Override
	public Object eval(LogMap map) {
		Object min = null;

		for (Expression expr : exprs) {
			Object o = expr.eval(map);
			if (min == null)
				min = o;
			else if (cmp.compare(min, o) > 0)
				min = o;
		}

		return min;
	}

	@Override
	public String toString() {
		return "min(" + exprs + ")";
	}
}
