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
package org.krakenapps.logdb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LogTimelineCallback {
	private long callbackInterval = 2000;
	private Map<Long, Integer> timeline = new HashMap<Long, Integer>();
	private long lastCallbackTime;
	private SpanValue[] spans = new SpanValue[] { new SpanValue(Calendar.MINUTE, 1), //
			new SpanValue(Calendar.MINUTE, 10), //
			new SpanValue(Calendar.HOUR_OF_DAY, 1), //
			new SpanValue(Calendar.DAY_OF_YEAR, 1), //
			new SpanValue(Calendar.WEEK_OF_YEAR, 1), //
			new SpanValue(Calendar.MONTH, 1) };
	private int spansIndex = 0;

	public long getCallbackInterval() {
		return callbackInterval;
	}

	public void setCallbackInterval(long callbackInterval) {
		this.callbackInterval = callbackInterval;
	}

	public abstract int getSize();

	public void put(Date date) {
		if (date == null)
			return;

		long time = date.getTime();
		time = time - time % 86400;
		if (timeline.containsKey(time))
			timeline.put(time, timeline.get(time) + 1);
		else
			timeline.put(time, 1);

		if (System.currentTimeMillis() > lastCallbackTime + callbackInterval) {
			callback();
			lastCallbackTime = System.currentTimeMillis();
		}
	}

	public void callback() {
		buildCallbackData(false);
	}

	public void eof() {
		buildCallbackData(true);
	}

	private void buildCallbackData(boolean isEnd) {
		int size = getSize();
		int[] values = new int[size];
		Long beginTime = null;

		if (timeline.isEmpty())
			return;

		if (spansIndex >= spans.length)
			return;

		long[] index = new long[size];
		while (true) {
			List<Long> keys = new ArrayList<Long>(timeline.keySet());
			Collections.sort(keys, Collections.reverseOrder());
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(spans[spansIndex].getBaseTime(keys.get(0)));
			for (int i = size - 1; i >= 0; i--) {
				index[i] = c.getTimeInMillis();
				c.add(spans[spansIndex].field, -spans[spansIndex].amount);
			}
			beginTime = index[0];
			if (keys.get(keys.size() - 1) < beginTime) {
				if (++spansIndex >= spans.length)
					return;
				continue;
			}

			int indexPos = size - 1;
			for (Long key : keys) {
				while (key < index[indexPos])
					indexPos--;
				values[indexPos] += timeline.get(key);
			}

			Map<Long, Integer> newTimeline = new HashMap<Long, Integer>();
			for (int i = 0; i < size; i++)
				newTimeline.put(index[i], values[i]);
			timeline = newTimeline;

			break;
		}

		callback(new Date(beginTime), spans[spansIndex], values, isEnd);
	}

	protected abstract void callback(Date beginTime, SpanValue spanValue, int[] values, boolean isEnd);

	public class SpanValue {
		private int field;
		private int amount;

		private SpanValue(int field, int amount) {
			this.field = field;
			this.amount = amount;
		}

		public String getFieldName() {
			switch (field) {
			case Calendar.MINUTE:
				return "Minute";
			case Calendar.HOUR_OF_DAY:
				return "Hour";
			case Calendar.DAY_OF_YEAR:
				return "Day";
			case Calendar.WEEK_OF_YEAR:
				return "Week";
			case Calendar.MONTH:
				return "Month";
			}
			return Integer.toString(field);
		}

		public int getAmount() {
			return amount;
		}

		public Date getBaseTime(Date time) {
			return new Date(getBaseTime(time.getTime()));
		}

		public long getBaseTime(long time) {
			switch (field) {
			case Calendar.MINUTE:
			case Calendar.HOUR_OF_DAY:
			case Calendar.DAY_OF_YEAR:
			case Calendar.WEEK_OF_YEAR:
				time += 291600000L; // base to Monday, 00:00:00
				time -= time % (getMillis() * amount);
				time -= 291600000L;
				return time;

			case Calendar.MONTH:
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(time);
				c.set(Calendar.MILLISECOND, 0);
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MINUTE, 0);
				c.set(Calendar.HOUR_OF_DAY, 0);
				c.set(Calendar.DAY_OF_MONTH, 1);
				int monthOffset = c.get(Calendar.YEAR) * 12;
				int month = monthOffset + c.get(Calendar.MONTH);
				month -= month % amount;
				month -= monthOffset;
				if (month >= 0)
					c.set(Calendar.MONTH, month);
				else {
					c.set(Calendar.YEAR, c.get(Calendar.YEAR) - 1);
					c.set(Calendar.MONTH, month + 12);
				}
				return c.getTimeInMillis();
			}
			return time;
		}

		private long getMillis() {
			switch (field) {
			case Calendar.MINUTE:
				return 60 * 1000L;
			case Calendar.HOUR_OF_DAY:
				return 60 * 60 * 1000L;
			case Calendar.DAY_OF_YEAR:
				return 24 * 60 * 60 * 1000L;
			case Calendar.WEEK_OF_YEAR:
				return 7 * 24 * 60 * 60 * 1000L;
			}
			return -1;
		}
	}
}