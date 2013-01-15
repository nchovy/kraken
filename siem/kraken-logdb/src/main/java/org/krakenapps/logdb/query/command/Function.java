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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.krakenapps.logdb.LogQueryCommand.LogMap;
import org.krakenapps.logdb.query.ObjectComparator;
import org.krakenapps.logdb.query.command.Function.EvaledField.EvalType;
import org.krakenapps.logdb.query.command.Function;
import org.krakenapps.logdb.query.command.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Function {
	private static Logger logger = LoggerFactory.getLogger(Function.class);
	private static Map<String, Class<? extends Function>> mapping;
	private static Pattern p = Pattern.compile("[0-9]+$");

	static {
		mapping = new HashMap<String, Class<? extends Function>>();
		mapping.put("avg", Average.class);
		mapping.put("count", Count.class);
		mapping.put("c", Count.class);
		mapping.put("first", First.class);
		mapping.put("last", Last.class);
		mapping.put("max", Max.class);
		mapping.put("min", Min.class);
		mapping.put("mean", Average.class);
		mapping.put("range", Range.class);
		mapping.put("sum", Sum.class);
	}

	private String name;
	private Integer suffix;
	private String keyName;
	private String target;
	private EvaledField evaled;
	private Map<String, Class<? extends Function>> extClass;

	private Function() {
	}

	public static Function getFunction(String name, String target) {
		return getFunction(name, target, null, null);
	}

	public static Function getFunction(String name, String target, String keyName) {
		return getFunction(name, target, keyName, null);
	}

	public static Function getFunction(String name, String target, Map<String, Class<? extends Function>> extClass) {
		return getFunction(name, target, null, extClass);
	}

	public static Function getFunction(String name, String target, String keyName, Map<String, Class<? extends Function>> extClass) {
		if (name == null)
			return null;

		// String functionName = name.split("[0-9]+$")[0];
		String functionName = p.split(name)[0];
		Class<? extends Function> cls = mapping.get(functionName.toLowerCase());
		if (cls == null && extClass != null)
			cls = extClass.get(functionName.toLowerCase());

		if (cls == null)
			return null;

		try {
			Function f = cls.newInstance();
			f.name = name;
			if (!functionName.equals(name))
				f.suffix = Integer.parseInt(name.substring(functionName.length()));
			f.keyName = keyName;
			if (target != null && target.startsWith("eval(") && target.endsWith(")")) {
				String evalString = target.substring(5, target.length() - 1);
				EvalType type = null;
				String lh = null;
				String operator = null;
				String rh = null;
				int index = 0;

				if (evalString.contains("+")) {
					index = evalString.indexOf("+");
					type = EvalType.Arithmetic;
					operator = "+";
				} else if (evalString.contains("-")) {
					index = evalString.indexOf("-");
					type = EvalType.Arithmetic;
					operator = "-";
				} else if (evalString.contains("*")) {
					index = evalString.indexOf("*");
					type = EvalType.Arithmetic;
					operator = "*";
				} else if (evalString.contains("/")) {
					index = evalString.indexOf("/");
					type = EvalType.Arithmetic;
					operator = "/";
				} else if (evalString.contains("%")) {
					index = evalString.indexOf("%");
					type = EvalType.Arithmetic;
					operator = "%";
				}

				lh = evalString.substring(0, index);
				rh = evalString.substring(index + 1);

				f.evaled = new EvaledField(type, lh, operator, rh);
			}
			f.target = target;
			f.extClass = extClass;
			return f;
		} catch (Exception e) {
			logger.error("kraken logdb: failed create function.", e);
		}

		return null;
	}

	public String getName() {
		return name;
	}

	public Integer getSuffix() {
		return suffix;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getTarget() {
		return target;
	}

	@Override
	public Function clone() {
		return getFunction(name, target, keyName, extClass);
	}

	public void put(LogMap row) {
		Object value = null;

		if (evaled != null)
			value = evaled.eval(row);
		else
			value = row.get(target);

		if (value != null || target == null)
			put(value);
	}

	abstract protected void put(Object obj);

	abstract public Object getResult();

	abstract public void clean();

	// when save serialized data to disk
	public Object[] serialize() {
		Object[] l = new Object[3];
		serialize(l);
		return l;
	}

	protected int serialize(Object[] arr) {
		arr[0] = name;
		arr[1] = target;
		arr[2] = keyName;
		return 3;
	}

	// when load swapped data from disk
	public void load(Object value) {
		Object[] l = (Object[]) value;
		name = (String) l[0];
		target = (String) l[1];
		keyName = (String) l[2];
	}

	abstract public Function merge(Function func);

	@Override
	public String toString() {
		if (keyName == null)
			return String.format("%s%s", name, (target != null) ? ("(" + target + ")") : "");
		else
			return keyName;
	}

	public static class EvaledField {
		public static enum EvalType {
			Arithmetic, Boolean
		}

		private EvalType type;
		private String lh;
		private String operator;
		private String rh;

		public EvaledField(EvalType type, String lh, String operator, String rh) {
			this.type = type;
			this.lh = lh;
			this.operator = operator;
			this.rh = rh;
		}

		public Object eval(LogMap row) {
			Object l = row.get(lh);
			if (type == EvalType.Arithmetic) {
				Object r = row.get(rh);

				if (l == null && r == null)
					return null;

				if (operator.equals("+"))
					return NumberUtil.add(l, r);
				else if (operator.equals("-"))
					return NumberUtil.sub(l, r);
				else if (operator.equals("*"))
					return NumberUtil.mul(l, r);
				else if (operator.equals("/"))
					return NumberUtil.div(l, r);
				else if (operator.equals("%"))
					return NumberUtil.mod(l, r);
			} else if (type == EvalType.Boolean) {
				// TODO
			}

			return null;
		}
	}

	protected static class Average extends Function {
		private Double d;
		private int count;

		@Override
		protected void put(Object obj) {
			d = NumberUtil.add(d, obj).doubleValue();
			count++;
		}

		public Double getD() {
			return d;
		}

		public void setD(Double d) {
			this.d = d;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public Object getResult() {
			if (d == null)
				return null;
			return d / count;
		}

		@Override
		public void clean() {
			d = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[5];
			int i = super.serialize(l);
			l[i++] = d;
			l[i++] = count;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			this.d = (Double) l[3];
			this.count = (Integer) l[4];
		}

		@Override
		public Function merge(Function func) {
			// d should not be null here (do not allow null merge set)
			Average other = (Average) func;
			this.d += other.d;
			this.count += other.count;
			return this;
		}
	}

	protected static class Count extends Function {
		private long result = 0;

		@Override
		protected void put(Object obj) {
			result++;
		}

		public void setResult(long result) {
			this.result = result;
		}

		@Override
		public Object getResult() {
			return result;
		}

		@Override
		public void clean() {
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = result;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			result = (Long) l[3];
		}

		@Override
		public Function merge(Function func) {
			Count c = (Count) func;
			this.result += c.result;
			return this;
		}
	}

	protected static class First extends Function {
		private Object first;

		@Override
		protected void put(Object obj) {
			if (first == null && obj != null)
				first = obj;
		}

		public Object getFirst() {
			return first;
		}

		public void setFirst(Object first) {
			this.first = first;
		}

		@Override
		public Object getResult() {
			return first;
		}

		@Override
		public void clean() {
			first = null;
		}

		@Override
		public Function merge(Function func) {
			// ignore subsequent items
			return this;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = first;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			first = l[3];
		}
	}

	protected static class Last extends Function {
		private Object last;

		@Override
		protected void put(Object obj) {
			if (obj != null)
				last = obj;
		}

		public Object getLast() {
			return last;
		}

		public void setLast(Object last) {
			this.last = last;
		}

		@Override
		public Object getResult() {
			return last;
		}

		@Override
		public void clean() {
			last = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = last;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			last = l[3];
		}

		@Override
		public Function merge(Function func) {
			Last last = (Last) func;
			this.last = last.last;
			return this;
		}
	}

	protected static class Max extends Function {
		private ObjectComparator comp = new ObjectComparator();
		private Object max;

		@Override
		protected void put(Object obj) {
			if (max == null || comp.compare(max, obj) < 0)
				max = obj;
		}

		public Object getMax() {
			return max;
		}

		public void setMax(Object max) {
			this.max = max;
		}

		@Override
		public Object getResult() {
			return max;
		}

		@Override
		public void clean() {
			max = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = max;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			this.max = l[3];
		}

		@Override
		public Function merge(Function func) {
			Max other = (Max) func;
			put(other.max);
			return this;
		}
	}

	protected static class Min extends Function {
		private ObjectComparator comp = new ObjectComparator();
		private Object min;

		@Override
		protected void put(Object obj) {
			if (min == null || (comp.compare(min, obj) > 0 && obj != null))
				min = obj;
		}

		public Object getMin() {
			return min;
		}

		public void setMin(Object min) {
			this.min = min;
		}

		@Override
		public Object getResult() {
			return min;
		}

		@Override
		public void clean() {
			min = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = min;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			this.min = l[3];
		}

		@Override
		public Function merge(Function func) {
			Min other = (Min) func;
			put(other.min);
			return this;
		}
	}

	protected static class Range extends Function {
		private Number min;
		private Number max;

		@Override
		protected void put(Object obj) {
			min = NumberUtil.min(min, obj);
			max = NumberUtil.max(max, obj);
		}

		public Number getMin() {
			return min;
		}

		public void setMin(Number min) {
			this.min = min;
		}

		public Number getMax() {
			return max;
		}

		public void setMax(Number max) {
			this.max = max;
		}

		@Override
		public Object getResult() {
			if (max == null && min == null)
				return null;

			return NumberUtil.sub(max, min);
		}

		@Override
		public void clean() {
			min = null;
			max = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[5];
			int i = super.serialize(l);
			l[i++] = min;
			l[i++] = max;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			min = (Number) l[3];
			max = (Number) l[4];
		}

		@Override
		public Function merge(Function func) {
			Range other = (Range) func;
			this.min = NumberUtil.min(min, other.min);
			this.max = NumberUtil.max(max, other.max);
			return this;
		}
	}

	protected static class Sum extends Function {
		private Number sum = 0L;

		@Override
		protected void put(Object obj) {
			sum = NumberUtil.add(sum, obj);
		}

		public Number getSum() {
			return sum;
		}

		public void setSum(Number sum) {
			this.sum = sum;
		}

		@Override
		public Object getResult() {
			return sum;
		}

		@Override
		public void clean() {
			sum = null;
		}

		@Override
		public Object[] serialize() {
			Object[] l = new Object[4];
			int i = super.serialize(l);
			l[i++] = sum;
			return l;
		}

		@Override
		public void load(Object value) {
			super.load(value);
			Object[] l = (Object[]) value;
			this.sum = (Number) l[3];
		}

		@Override
		public Function merge(Function func) {
			Sum other = (Sum) func;
			this.sum = NumberUtil.add(sum, other.sum);
			return this;
		}
	}

}
