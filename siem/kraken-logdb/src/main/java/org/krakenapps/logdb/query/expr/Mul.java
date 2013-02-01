package org.krakenapps.logdb.query.expr;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.command.NumberUtil;

public class Mul extends BinaryExpression {
	public Mul(Expression lhs, Expression rhs) {
		super(lhs, rhs);
	}

	@Override
	public Object eval(LogMap map) {
		return NumberUtil.mul(lhs.eval(map), rhs.eval(map));
	}

	@Override
	public String toString() {
		return "(" + lhs + " * " + rhs + ")";
	}
}
