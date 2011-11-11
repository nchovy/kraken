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
		private Map<String, Object> terms;

		public KeyMatch(String key, Object value) {
			this.terms = new HashMap<String, Object>();
			this.terms.put(key, value);
		}

		public KeyMatch(Map<String, Object> terms) {
			this.terms = terms;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean eval(Config c) {
			Object doc = c.getDocument();
			if (!(doc instanceof Map))
				return false;

			Map<String, Object> m = (Map<String, Object>) doc;
			for (String k : terms.keySet()) {
				String key = PrimitiveConverter.toUnderscoreName(k);
				Object value = terms.get(k);

				Map<String, Object> m2 = m;
				String[] splittedKey = key.split("/");
				for (int i = 0; i < splittedKey.length - 1; i++) {
					if (!m2.containsKey(splittedKey[i]))
						return false;
					if (!(m2.get(splittedKey[i]) instanceof Map))
						return false;
					m2 = (Map<String, Object>) m2.get(splittedKey[i]);
				}
				key = splittedKey[splittedKey.length - 1];

				if (!m2.containsKey(key))
					return false;

				Object v = m2.get(key);
				if (v == null && value != null)
					return false;

				if (v == null && value == null)
					continue;

				if (!v.equals(value))
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

}
