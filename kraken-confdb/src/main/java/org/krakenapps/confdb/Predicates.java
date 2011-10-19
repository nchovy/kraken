package org.krakenapps.confdb;

public class Predicates {
	public static Predicate eq(Object o) {
		return new EqObject(o);
	}

	public static Predicate and(Predicate lhs, Predicate rhs) {
		return new Conjunction(lhs, rhs);
	}

	public static Predicate or(Predicate lhs, Predicate rhs) {
		return new Disjunction(lhs, rhs);
	}

	private static class EqObject implements Predicate {
		private Object o;

		public EqObject(Object o) {
			this.o = o;
		}

		@Override
		public boolean eval(Config c) {
			return c.getDocument().equals(o);
		}
	}

	private static class KeyMatch implements Predicate {

		@Override
		public boolean eval(Config c) {
			return false;
		}
	}

	private static class Conjunction implements Predicate {
		private Predicate lhs;
		private Predicate rhs;

		public Conjunction(Predicate lhs, Predicate rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public boolean eval(Config c) {
			return lhs.eval(c) && rhs.eval(c);
		}
	}

	private static class Disjunction implements Predicate {
		private Predicate lhs;
		private Predicate rhs;

		public Disjunction(Predicate lhs, Predicate rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public boolean eval(Config c) {
			return lhs.eval(c) || rhs.eval(c);
		}
	}

}
