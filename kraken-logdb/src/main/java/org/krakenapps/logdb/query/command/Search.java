/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.query.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.krakenapps.logdb.query.ObjectComparator;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.command.Search;

public class Search extends LogQueryCommand {
	private Integer limit;
	private Integer count;
	private List<Term> terms;

	public Search(List<Term> terms) {
		this(null, terms);
	}

	public Search(Integer limit, List<Term> terms) {
		this.limit = limit;
		this.terms = terms;
	}

	@Override
	public void init() {
		super.init();
		count = 0;
	}

	@Override
	public void push(Map<String, Object> m) {
		for (Term term : terms) {
			if (!term.eval(this, m))
				return;
		}

		write(m);

		if (limit != null && ++count == limit) {
			eof();
			return;
		}
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	public static class Term {
		public static enum Operator {
			Eq, Neq, Gt, Lt, Ge, Le, Contain, Regexp, In
		}

		private static Comparator<Object> comp = new ObjectComparator();
		private Object lh;
		private boolean isLhString = false;
		private Operator operator;
		private Object rh;
		private boolean isRhString = true;

		public boolean eval(Search eval, Map<String, Object> m) {
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
				case Contain:
					return l.toString().contains(r.toString());
				case Regexp:
					return Pattern.matches(r.toString(), l.toString());
				case In:
					return Arrays.asList(r.toString().replaceAll(",( )*", ",").split(",")).contains(l.toString());
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
