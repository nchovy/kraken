package org.krakenapps.logstorage.query.command;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.query.ObjectComparator;

public class Eval extends LogQueryCommand {
	private Integer limit;
	private List<Term> terms;

	public Eval(List<Term> terms) {
		this(null, terms);
	}

	public Eval(Integer limit, List<Term> terms) {
		this.limit = limit;
		this.terms = terms;
	}

	@Override
	public void push(Map<String, Object> m) {
		for (Term term : terms) {
			if (!term.eval(this, m))
				return;
		}

		if (limit != null && --limit == 0) {
			eof();
			return;
		}

		write(m);
	}

	public static class Term {
		public static enum Operator {
			Eq, Neq, Gt, Lt, Ge, Le
		}

		private static Comparator<Object> comp = new ObjectComparator();
		private Object lh;
		private boolean isLhString = false;
		private Operator operator;
		private Object rh;
		private boolean isRhString = true;

		public boolean eval(Eval eval, Map<String, Object> m) {
			Object l = isLhString ? lh : eval.getData(lh.toString(), m);
			Object r = isRhString ? rh : eval.getData(rh.toString(), m);

			try {
				int cmp = comp.compare(l, r);
				switch (operator) {
				case Eq:
					return cmp == 0;
				case Neq:
					return cmp != 0;
				case Gt:
					return cmp > 0;
				case Lt:
					return cmp < 0;
				case Ge:
					return cmp >= 0;
				case Le:
					return cmp <= 0;
				}
			} catch (Exception e) {
			}

			return false;
		}

		public Object getLh() {
			return lh;
		}

		public void setLh(Object lh) {
			this.lh = lh;
		}

		public boolean isLhString() {
			return isLhString;
		}

		public void setLhString(boolean isLhString) {
			this.isLhString = isLhString;
		}

		public Operator getOperator() {
			return operator;
		}

		public void setOperator(Operator operator) {
			this.operator = operator;
		}

		public Object getRh() {
			return rh;
		}

		public void setRh(Object rh) {
			this.rh = rh;
		}

		public boolean isRhString() {
			return isRhString;
		}

		public void setRhString(boolean isRhString) {
			this.isRhString = isRhString;
		}
	}
}
