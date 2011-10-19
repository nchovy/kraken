/*
 * Copyright 2011 Future Systems, Inc.
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
package org.krakenapps.confdb;

import java.util.Map;

public class Predicates {
	public static Predicate eq(Object o) {
		return new EqObject(o);
	}
	
	public static Predicate key(String key, Object value) {
		return new KeyMatch(key, value);
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

		private String key;
		private Object value;
		
		public KeyMatch(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean eval(Config c) {
			Object doc = c.getDocument();
			if (!(doc instanceof Map))
				return false;

			Map<String, Object> m = (Map<String, Object>) doc;
			if (!m.containsKey(key))
				return false;

			Object v = m.get(key);
			if (v == null && value != null)
				return false;

			if (v == null && value == null)
				return true;

			return v.equals(value);
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
