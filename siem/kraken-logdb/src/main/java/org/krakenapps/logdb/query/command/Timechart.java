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

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.command.Function.Sum;

public class Timechart extends LogQueryCommand {
	public static enum Span {
		Second(1000L), Minute(60000L), Hour(3600000L), Day(86400000L), Week(604800000L), Month(0L), Year(0L);

		private long millis;

		private Span(long millis) {
			this.millis = millis;
		}
	}

	private FileBufferList<Map<String, Object>> data;
	private Span spanField;
	private int spanAmount;
	private Function[] values;
	private String keyField;
	private String dateColumn;
	private Set<String> keyFilter = new HashSet<String>();

	public Timechart(Function[] values, String keyField) {
		this(Span.Day, 1, values, keyField);
	}

	public Timechart(Span spanField, int spanAmount, Function[] values, String keyField) {
		this.spanField = spanField;
		this.spanAmount = spanAmount;
		this.values = values;
		this.keyField = keyField;
		this.dateColumn = headerColumn.get("date");

		for (Function func : values) {
			if (func.getTarget() != null)
				keyFilter.add(func.getTarget());
		}
		if (keyField != null)
			keyFilter.add(keyField);
		keyFilter.add(dateColumn);
	}

	@Override
	protected void initProcess() {
		try {
			this.data = new FileBufferList<Map<String, Object>>(new Comparator<Map<String, Object>>() {
				@Override
				public int compare(Map<String, Object> o1, Map<String, Object> o2) {
					Date d1 = (Date) o1.get(dateColumn);
					Date d2 = (Date) o2.get(dateColumn);
					return d1.compareTo(d2);
				}
			});
		} catch (IOException e) {
		}
	}

	@Override
	public void push(LogMap m) {
		Object time = m.get(dateColumn);
		if (time != null && time.getClass().equals(Date.class)) {
			Map<String, Object> map = new HashMap<String, Object>();
			for (Entry<String, Object> e : m.map().entrySet()) {
				if (keyFilter.contains(e.getKey()))
					map.put(e.getKey(), e.getValue());
			}
			data.add(map);
		}
	}

	@Override
	public boolean isReducer() {
		return true;
	}

	@Override
	protected void eofProcess() {
		Map<Object, Function[]> funcs = new HashMap<Object, Function[]>();
		Date currentDate = null;
		Date nextDate = null;

		for (Map<String, Object> m : data) {
			Date d = (Date) m.get(headerColumn.get("date"));
			if (nextDate == null || !nextDate.after(d)) {
				if (nextDate != null) {
					if (currentDate == null) {
						if (spanField.ordinal() <= Span.Week.ordinal())
							currentDate = new Date(nextDate.getTime() - spanField.millis * spanAmount);
						else if (spanField == Span.Month) {
							Calendar c = Calendar.getInstance();
							c.setTime(nextDate);
							c.add(Calendar.MONTH, -spanAmount);
							currentDate = c.getTime();
						} else if (spanField == Span.Year) {
							Calendar c = Calendar.getInstance();
							c.setTime(nextDate);
							c.add(Calendar.YEAR, -spanAmount);
							currentDate = c.getTime();
						}
					}
					writeData(currentDate, funcs);
				}

				funcs = new HashMap<Object, Function[]>();
				currentDate = nextDate;
				nextDate = getDateLimit(d);
			}

			LogMap logmap = new LogMap(m);
			Object key = m.get(keyField);

			if (!funcs.containsKey(key)) {
				Function[] value = new Function[values.length];
				for (int i = 0; i < values.length; i++)
					value[i] = values[i].clone();
				funcs.put(key, value);
			}

			for (Function func : funcs.get(key))
				func.put(logmap);
		}
		writeData(currentDate, funcs);

		data.close();
		data = null;
	}

	private void writeData(Date date, Map<Object, Function[]> funcs) {
		LogMap result = new LogMap();
		result.put(headerColumn.get("date"), date);
		if (keyField == null) {
			for (Function func : funcs.get(null))
				result.put(func.toString(), func.getResult());
		} else {
			if (values.length == 1) {
				for (Object key : funcs.keySet())
					result.put((key != null) ? key.toString() : "null", funcs.get(key)[0].getResult());
			} else {
				for (Object key : funcs.keySet()) {
					for (Function func : funcs.get(key))
						result.put(func.toString() + ":" + key, func.getResult());
				}
			}
		}
		write(result);
	}

	private Date getDateLimit(Date key) {
		long time = key.getTime();

		if (spanField.ordinal() <= Span.Week.ordinal()) {
			int raw = TimeZone.getDefault().getRawOffset();
			time += 259200000L + raw; // base to Monday, 00:00:00
			time -= time % (spanField.millis * spanAmount);
			time -= 259200000L + raw;
			time += spanField.millis * spanAmount;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time - time % Span.Second.millis);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);

			if (spanField == Span.Month) {
				int monthOffset = c.get(Calendar.YEAR) * 12;
				int month = monthOffset + c.get(Calendar.MONTH);
				month -= month % spanAmount;
				month -= monthOffset;
				month += spanAmount;
				c.add(Calendar.MONTH, month);
				time = c.getTimeInMillis();
			} else if (spanField == Span.Year) {
				int year = c.get(Calendar.YEAR);
				c.set(Calendar.YEAR, year - (year % spanAmount) + spanAmount);
				time = c.getTimeInMillis();
			}
		}

		return new Date(time);
	}

	public static final Map<String, Class<? extends Function>> func;
	static {
		func = new HashMap<String, Class<? extends Function>>();
		func.put("per_second", PerSecond.class);
		func.put("per_minute", PerMinute.class);
		func.put("per_hour", PerHour.class);
		func.put("per_day", PerDay.class);
	}

	public static abstract class PerTime extends Sum {
		private long amount;

		abstract protected long getTimeLength();

		public long getAmount() {
			return amount;
		}

		public void setAmount(long amount) {
			this.amount = amount;
		}

		@Override
		public Object getResult() {
			if (super.getResult() == null)
				return null;
			return NumberUtil.div(super.getResult(), (double) amount / (double) getTimeLength());
		}
	}

	public static class PerSecond extends PerTime {
		@Override
		protected long getTimeLength() {
			return 1000L;
		}
	}

	public static class PerMinute extends PerTime {
		@Override
		protected long getTimeLength() {
			return 60 * 1000L;
		}
	}

	public static class PerHour extends PerTime {
		@Override
		protected long getTimeLength() {
			return 60 * 60 * 1000L;
		}
	}

	public static class PerDay extends PerTime {
		@Override
		protected long getTimeLength() {
			return 24 * 60 * 60 * 1000L;
		}
	}
}
