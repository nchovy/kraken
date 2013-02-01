package org.krakenapps.logdb.query.expr;

import org.krakenapps.logdb.LogQueryCommand.LogMap;

public class Abs implements Expression {
	private Expression expr;

	public Abs(Expression expr) {
		this.expr = expr;
	}

	@Override
	public Object eval(LogMap map) {
		Object o = expr.eval(map);
		if (o == null)
			return null;

		if (o instanceof Double)
			return Math.abs((Double) o);
		else if (o instanceof Integer)
			return Math.abs((Integer) o);
		else if (o instanceof Long)
			return Math.abs((Long) o);
		else if (o instanceof Float)
			return Math.abs((Float) o);
		else if (o instanceof Short)
			return Math.abs((Short) o);

		return null;
	}

	@Override
	public String toString() {
		return "abs(" + expr + ")";
	}
}