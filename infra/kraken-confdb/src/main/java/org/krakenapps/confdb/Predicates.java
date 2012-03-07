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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.api.PrimitiveConverter;

public class Predicates {
	public static Predicate eq(Object o) {
		return new EqObject(o);
	}

	public static Predicate has(String field) {
		return new HasKey(field);
	}

	public static Predicate field(String field, Object value) {
		return new KeyMatch(field, value);
	}

	public static Predicate field(Map<String, Object> terms) {
		return new KeyMatch(terms);
	}

	public static Predicate in(String field, Collection<? extends Object> values) {
		return new KeyContains(field, values);
	}

	public static Predicate and(Predicate... pred) {
		return new Conjunction(pred);
	}

	public static Predicate or(Predicate... pred) {
		return new Disjunction(pred);
	}

	public static Predicate not(Predicate pred) {
		return new Not(pred);
	}

	private static Object getValue(Config c, String key) {
		return getValue(c, key, false);
	}

	@SuppressWarnings("unchecked")
	private static Object getValue(Config c, String key, boolean throwException) {
		key = PrimitiveConverter.toUnderscoreName(key);
		Object value = c.getDocument();
		for (String k : key.split("/")) {
			if (!(value instanceof Map)) {
				if (throwException)
					throw new IllegalArgumentException();
				return null;
			}

			Map<String, Object> m = (Map<String, Object>) value;
			if (!m.containsKey(k)) {
				if (throwException)
					throw new IllegalArgumentException();
				return null;
			}

			value = m.get(k);
		}
		return value;
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

	private static class HasKey implements Predicate {
		private String key;

		public HasKey(String key) {
			this.key = key;
		}

		@Override
		public boolean eval(Config c) {
			try {
				getValue(c, key, true);
				return true;
			} catch (IllegalArgumentException e) {
				return false;
			}
		}
	}

	private static class KeyMatch implements Predicate {
		private Map<String, Object> terms;

		public KeyMatch(String key, Object value) {
			this.terms = new HashMap<String, Object>();
			this.terms.put(key, value);
		}

		public KeyMatch(Map<String, Object> terms) {
			this.terms = terms;
		}

		@Override
		public boolean eval(Config c) {
			Object doc = c.getDocument();
			if (!(doc instanceof Map))
				return false;

			for (String k : terms.keySet()) {
				Object value = getValue(c, k);
				Object comp = terms.get(k);

				if (value == null && comp != null)
					return false;

				if (value == null && comp == null)
					continue;

				if (!value.equals(comp))
					return false;
			}

			return true;
		}
	}

	private static class KeyContains implements Predicate {
		private String key;
		private Collection<? extends Object> values;

		public KeyContains(String key, Collection<? extends Object> values) {
			this.key = key;
			this.values = values;
		}

		@Override
		public boolean eval(Config c) {
			Object v = getValue(c, key);
			return values.contains(v);
		}
	}

	private static class Conjunction implements Predicate {
		private Predicate[] pred;

		public Conjunction(Predicate... pred) {
			this.pred = pred;
		}

		@Override
		public boolean eval(Config c) {
			for (Predicate p : pred) {
				if (!p.eval(c))
					return false;
			}
			return true;
		}
	}

	private static class Disjunction implements Predicate {
		private Predicate[] pred;

		public Disjunction(Predicate... pred) {
			this.pred = pred;
		}

		@Override
		public boolean eval(Config c) {
			for (Predicate p : pred) {
				if (p.eval(c))
					return true;
			}
			return false;
		}
	}

	private static class Not implements Predicate {
		private Predicate pred;

		public Not(Predicate pred) {
			this.pred = pred;
		}

		@Override
		public boolean eval(Config c) {
			return !pred.eval(c);
		}
	}
}
