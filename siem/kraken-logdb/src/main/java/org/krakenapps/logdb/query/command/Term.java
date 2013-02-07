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
import java.util.regex.Pattern;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.LogQueryParseException;
import org.krakenapps.logdb.query.ObjectComparator;

public class Term {
	public static enum Operator {
		Eq("=="), Neq("!="), Gt(">"), Lt("<"), Ge(">="), Le("<="), Contain("contain"), Regexp("regexp"), In("in"), IsNull(
				"is null"), NotNull("not null");

		private String str;

		public static Operator find(String str) {
			for (Operator o : values()) {
				if (o.toString().equalsIgnoreCase(str))
					return o;
			}
			throw new LogQueryParseException("unsupported-operator", -1, str);
		}

		private Operator(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	private static Comparator<Object> comp = new ObjectComparator();
	private Object lh;
	private boolean isLhString = false;
	private Operator operator;
	private Object rh;
	private boolean isRhString = false;
	private Pattern p;

	public boolean eval(LogMap m) {
		Object l = isLhString ? lh : m.get(lh.toString());
		Object r = isRhString ? rh : m.get(rh.toString());

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
			case Regexp: {
				if (l == null)
					return false;
				if (isRhString)
					return p.matcher(l.toString()).find();
				else {
					Pattern pp = Pattern.compile(m.get(rh.toString()).toString(), Pattern.MULTILINE);
					return pp.matcher(l.toString()).find();
				}
			}
			case In:
				return Arrays.asList(r.toString().replaceAll(",( )*", ",").split(",")).contains(l.toString());
			case IsNull:
				return (l == null || l.toString().isEmpty());
			case NotNull:
				return (l != null && !l.toString().isEmpty());
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
		if (operator == Operator.Regexp && isRhString) {
			this.p = Pattern.compile(rh.toString());
		}
	}

	public boolean isRhString() {
		return isRhString;
	}

	public void setRhString(boolean isRhString) {
		this.isRhString = isRhString;
	}

	@Override
	public String toString() {
		return String.format("%s %s %s", lh, operator, rh);
	}
}
