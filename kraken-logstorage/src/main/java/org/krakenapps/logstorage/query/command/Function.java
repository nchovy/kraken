package org.krakenapps.logstorage.query.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logstorage.query.ObjectComparator;
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
	}

	private String name;
	private String keyName;
	private String target;

	public static Function getFunction(String name, String target) {
		return getFunction(name, null, target);
	}

	public static Function getFunction(String name, String keyName, String target) {
		if (name == null)
			return null;

		Class<? extends Function> cls = mapping.get(name.toLowerCase());
		if (cls == null)
			return null;

		try {
			Function f = cls.newInstance();
			f.name = name;
			f.keyName = keyName;
			f.target = target;
			return f;
		} catch (Exception e) {
			logger.error("kraken logstorage: failed create function.", e);
		}

		return null;
	}

	public String getName() {
		return name;
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
		return getFunction(name, keyName, target);
	}

	public void put(Map<String, Object> row) {
		if (row.containsKey(target))
			put(row.get(target));
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

	protected static class Average extends Function {
		private Double d;
		private int count;

		@Override
		protected void put(Object obj) {
			try {
				double t = Double.parseDouble((String) obj);
				if (d == null)
					d = 0.;
				d += t;
			} catch (NumberFormatException e) {
			}
			count++;
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
		private int result = 0;

		@Override
		protected void put(Object obj) {
			result++;
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
		private java.util.List<Object> objs = new ArrayList<Object>();

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
}
