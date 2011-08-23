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

import java.nio.BufferOverflowException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTimelineCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table extends LogQueryCommand {
	public static enum Span {
		Second(Calendar.SECOND, 1000L), Minute(Calendar.MINUTE, 60 * 1000L), Hour(Calendar.HOUR_OF_DAY, 60 * 60 * 1000L), Day(
				Calendar.DAY_OF_MONTH, 24 * 60 * 60 * 1000L), Week(Calendar.WEEK_OF_YEAR, 7 * 24 * 60 * 60 * 1000L), Month(
				Calendar.MONTH, 0L), Year(Calendar.YEAR, 0L);

		private int calendarField;
		private long millis;

		private Span(int calendarField, long millis) {
			this.calendarField = calendarField;
			this.millis = millis;
		}
	}

	private Logger logger = LoggerFactory.getLogger(Table.class);
	private LogStorage storage;
	private String tableName;
	private int offset;
	private int limit;
	private Date from;
	private Date to;
	private Map<Date, Integer> timeline = new HashMap<Date, Integer>();
	private TimelineSpanValue[] timelineSpanValues;
	private int timelineSpanValueIndex = 0;

	public Table(String tableName) {
		this(tableName, 0);
	}

	public Table(String tableName, int limit) {
		this(tableName, limit, null, null);
	}

	public Table(String tableName, Date from, Date to) {
		this(tableName, 0, from, to);
	}

	public Table(String tableName, int limit, Date from, Date to) {
		this(tableName, 0, 0, from, to);
	}

	public Table(String tableName, int offset, int limit, Date from, Date to) {
		this.tableName = tableName;
		this.offset = offset;
		this.limit = limit;
		this.from = from;
		this.to = to;
		this.timelineSpanValues = new TimelineSpanValue[] { new TimelineSpanValue(Span.Minute, 1),
				new TimelineSpanValue(Span.Hour, 1), new TimelineSpanValue(Span.Day, 1),
				new TimelineSpanValue(Span.Month, 1) };
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public LogStorage getStorage() {
		return storage;
	}

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public void start() {
		try {
			if (from == null)
				from = new Date(0);
			if (to == null)
				to = new Date();

			status = Status.Running;
			storage.search(tableName, from, to, offset, limit, new LogSearchCallbackImpl());
		} catch (InterruptedException e) {
			logger.trace("kraken logstorage: query interrupted");
		} catch (Exception e) {
			logger.error("kraken logstorage: table exception", e);
		} catch (Error e) {
			logger.error("kraken logstorage: table error", e);
		}
		eof();

		TimelineSpanValue timelineSpan = timelineSpanValues[timelineSpanValueIndex];
		for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
			callbackTimeline(callback, timelineSpan, true);
	}

	@Override
	public void push(Map<String, Object> m) {
		throw new UnsupportedOperationException();
	}

	private class LogSearchCallbackImpl implements LogSearchCallback {
		private long DEFAULT_TIMELINE_REFRESH_INTERVAL = 2000;
		private long latestCallback = 0;

		@Override
		public void onLog(Log log) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("_table", log.getTableName());
			m.put("_id", log.getId());
			m.put("_time", log.getDate());
			m.put("_data", log.getData().get("log"));
			log.getData().remove("log");
			m.putAll(log.getData());
			write(m);

			if (!logQuery.getTimelineCallbacks().isEmpty()) {
				TimelineSpanValue timelineSpan = timelineSpanValues[timelineSpanValueIndex];
				Date key = getTimelineKey(timelineSpan, log.getDate());
				if (timeline.containsKey(key))
					timeline.put(key, timeline.get(key) + 1);
				else
					timeline.put(key, 1);

				if (System.currentTimeMillis() > latestCallback + DEFAULT_TIMELINE_REFRESH_INTERVAL) {
					for (LogTimelineCallback callback : logQuery.getTimelineCallbacks())
						callbackTimeline(callback, timelineSpan, false);
					latestCallback = System.currentTimeMillis();
				}
			}
		}

		@Override
		public void interrupt() {
			eof();
		}

		@Override
		public boolean isInterrupted() {
			return status.equals(Status.End);
		}
	}

	private void callbackTimeline(LogTimelineCallback callback, TimelineSpanValue timelineSpan, boolean isFinal) {
		while (true) {
			try {
				callback.callback(timelineSpan.field.calendarField, timelineSpan.amount, timeline, isFinal);
				break;
			} catch (BufferOverflowException e) {
				if (timelineSpanValueIndex == timelineSpanValues.length - 1)
					break;

				timelineSpan = timelineSpanValues[++timelineSpanValueIndex];
				Map<Date, Integer> newTimeline = new HashMap<Date, Integer>();
				for (Date k : timeline.keySet()) {
					Date newKey = getTimelineKey(timelineSpan, k);
					if (newTimeline.containsKey(newKey))
						newTimeline.put(newKey, newTimeline.get(newKey) + timeline.get(k));
					else
						newTimeline.put(newKey, timeline.get(k));
				}
				timeline = newTimeline;
			}
		}
	}

	private Date getTimelineKey(TimelineSpanValue span, Date date) {
		long time = date.getTime();

		if (span.field == Span.Second || span.field == Span.Minute || span.field == Span.Hour || span.field == Span.Day
				|| span.field == Span.Week) {
			time += 291600000L; // base to Monday, 00:00:00
			time -= time % (span.field.millis * span.amount);
			time -= 291600000L;
		} else {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time - time % Span.Second.millis);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);

			if (span.field == Span.Month) {
				int monthOffset = c.get(Calendar.YEAR) * 12;
				int month = monthOffset + c.get(Calendar.MONTH);
				month -= month % span.amount;
				month -= monthOffset;
				c.add(Calendar.MONTH, month);
				time = c.getTimeInMillis();
			} else if (span.field == Span.Year) {
				int year = c.get(Calendar.YEAR);
				c.set(Calendar.YEAR, year - (year % span.amount));
				time = c.getTimeInMillis();
			}
		}

		return new Date(time);
	}

	private class TimelineSpanValue {
		private Span field;
		private int amount;

		private TimelineSpanValue(Span field, int amount) {
			this.field = field;
			this.amount = amount;
		}
	}
}
