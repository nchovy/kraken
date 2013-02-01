package org.krakenapps.logdb.query.aggregator;

import java.util.List;

import org.krakenapps.logdb.query.expr.Expression;

public class PerSecond extends PerTime {
	public PerSecond(List<Expression> exprs) {
		super(exprs);
	}

	@Override
	protected long getTimeLength() {
		return 1000L;
	}
}
