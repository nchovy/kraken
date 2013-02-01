package org.krakenapps.logdb.query.expr;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.command.NumberUtil;

public class Sub extends BinaryExpression {
	public Sub(Expression lhs, Expression rhs) {
		super(lhs, rhs);
	}

	@Override
	public Object eval(LogMap map) {
		return NumberUtil.sub(lhs.eval(map), rhs.eval(map));
	}

	@Override
	public String toString() {
		return "(" + lhs + " - " + rhs + ")";
	}
}
