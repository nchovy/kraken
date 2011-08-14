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
package org.krakenapps.logstorage.query.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logstorage.query.ObjectComparator;
import org.krakenapps.logstorage.query.command.Function.EvaledField.EvalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Function {
	private static Logger logger = LoggerFactory.getLogger(Function.class);
	private static Map<String, Class<? extends Function>> mapping;
	static {
		mapping = new HashMap<String, Class<? extends Function>>();
		mapping.put("avg", Average.class);
		mapping.put("count", Count.class);
		mapping.put("c", Count.class);
		mapping.put("distinct_count", DistinctCount.class);
		mapping.put("dc", DistinctCount.class);
		mapping.put("first", First.class);
		mapping.put("last", Last.class);
		mapping.put("list", List.class);
		mapping.put("max", Max.class);
		mapping.put("min", Min.class);
		mapping.put("mean", Average.class);
		mapping.put("mode", Mode.class);
		mapping.put("range", Range.class);
		mapping.put("sum", Sum.class);
		mapping.put("sumsq", SumSquare.class);
		mapping.put("values", Values.class);
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

	public static Function getFunction(String name, String target, String keyName,
			Map<String, Class<? extends Function>> extClass) {
		if (name == null)
			return null;

		String functionName = name.split("[0-9]+$")[0];
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
			if (target.startsWith("eval(") && target.endsWith(")")) {
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
			logger.error("kraken logstorage: failed create function.", e);
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

	public void put(Map<String, Object> row) {
		Object value = null;

		if (evaled != null)
			value = evaled.eval(row);
		else
			value = row.get(target);

		if (value != null)
			put(value);
	}

	abstract protected void put(Object obj);

	abstract public Object getResult();

	abstract public void clean();

	@Override
	public String toString() {
		if (keyName == null)
			return String.format("%s(%s)", name, target);
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

		public Object eval(Map<String, Object> row) {
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
				Object r = rh;
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
	}

	protected static class Count extends Function {
		private long result = 0;

		@Override
		protected void put(Object obj) {
			result++;
		}

		public void setResult(int result) {
			this.result = result;
		}

		@Override
		public Object getResult() {
			return result;
		}

		@Override
		public void clean() {
		}
	}

	protected static class DistinctCount extends Function {
		private ObjectComparator comp = new ObjectComparator();
		protected java.util.List<Object> objs = new ArrayList<Object>();

		@Override
		protected void put(Object obj) {
			int l = 0;
			int r = objs.size();
			while (l < r) {
				int m = (l + r) / 2;
				Object o = objs.get(m);
				int c = comp.compare(obj, o);

				if (c == 0)
					return;
				else if (c > 0)
					l = m + 1;
				else if (c < 0)
					r = m;
			}
			objs.add((l + r) / 2, obj);
		}

		public java.util.List<Object> getObjs() {
			return objs;
		}

		public void setObjs(java.util.List<Object> objs) {
			this.objs = objs;
		}

		@Override
		public Object getResult() {
			return objs.size();
		}

		@Override
		public void clean() {
			objs = null;
		}
	}

	protected static class First extends Function {
		private Object first;

		@Override
		protected void put(Object obj) {
			if (first == null)
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
	}

	protected static class Last extends Function {
		private Object last;

		@Override
		protected void put(Object obj) {
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
	}

	protected static class List extends Function {
		private java.util.List<Object> objs = new ArrayList<Object>();

		@Override
		protected void put(Object obj) {
			objs.add(obj);
		}

		public java.util.List<Object> getObjs() {
			return objs;
		}

		public void setObjs(java.util.List<Object> objs) {
			this.objs = objs;
		}

		@Override
		public Object getResult() {
			return objs;
		}

		@Override
		public void clean() {
			objs = null;
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
	}

	protected static class Mode extends Function {
		private java.util.List<Object> list = new ArrayList<Object>();
		private Object result;
		private int maxCount;
		private int nowCount;

		@Override
		protected void put(Object obj) {
			list.add(obj);
		}

		public java.util.List<Object> getList() {
			return list;
		}

		public void setList(java.util.List<Object> list) {
			this.list = list;
		}

		public int getMaxCount() {
			return maxCount;
		}

		public void setMaxCount(int maxCount) {
			this.maxCount = maxCount;
		}

		public int getNowCount() {
			return nowCount;
		}

		public void setNowCount(int nowCount) {
			this.nowCount = nowCount;
		}

		public void setResult(Object result) {
			this.result = result;
		}

		@Override
		public Object getResult() {
			Collections.sort(list, new ObjectComparator());

			if (list.size() > 0) {
				Object prev = null;
				Object now = null;
				for (int i = 0; i < list.size(); i++) {
					now = list.get(i);
					if (i == 0 || now.equals(prev))
						nowCount++;
					else {
						nowCount = 1;
					}

					if (nowCount > maxCount) {
						result = prev;
						maxCount = nowCount;
					}
					prev = now;
				}
			}

			return result;
		}

		@Override
		public void clean() {
			list = null;
			result = null;
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
	}

	protected static class Sum extends Function {
		private Number sum;

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
	}

	protected static class SumSquare extends Function {
		private Number sum;

		@Override
		protected void put(Object obj) {
			sum = NumberUtil.add(sum, NumberUtil.mul(obj, obj));
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
	}

	protected static class Values extends DistinctCount {
		@Override
		public Object getResult() {
			return objs;
		}
	}
}
