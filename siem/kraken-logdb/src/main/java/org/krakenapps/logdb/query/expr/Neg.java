package org.krakenapps.logdb.query.expr;

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class Neg implements Expression {
	private Expression expr;

	public Neg(Expression expr) {
		this.expr = expr;
	}

	@Override
	public Object eval(LogMap map) {
		Object o = expr.eval(map);
		if (o == null)
			return null;

		if (o instanceof Integer)
			return -(Integer) o;
		if (o instanceof Long)
			return -(Long) o;
		if (o instanceof Double)
			return -(Double) o;
		if (o instanceof Float)
			return -(Float) o;
		return null;
	}

	@Override
	public String toString() {
		return "-" + expr;
	}
}
