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
package org.krakenapps.logstorage.msgbus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogQueryCallback;
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LogTimelineCallback;
import org.krakenapps.logstorage.query.FileBufferList;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "log-query-plugin")
@MsgbusPlugin
public class LogQueryPlugin {
	@Requires
	private LogQueryService service;

	@Requires
	private PushApi pushApi;

	private Map<Session, List<LogQuery>> queries = new HashMap<Session, List<LogQuery>>();

	@MsgbusMethod
	public void queries(Request req, Response resp) {
		List<Object> result = new ArrayList<Object>();
		for (LogQuery lq : service.getQueries()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", lq.getId());
			m.put("query_string", lq.getQueryString());
			m.put("is_end", lq.isEnd());
			result.add(m);
		}
		resp.put("queries", result);
	}

	@MsgbusMethod
	public void createQuery(Request req, Response resp) {
		LogQuery query = service.createQuery(req.getString("query"));
		resp.put("id", query.getId());

		Session session = req.getSession();
		if (!queries.containsKey(session))
			queries.put(session, new ArrayList<LogQuery>());
		queries.get(session).add(query);
	}

	@MsgbusMethod
	public void removeQuery(Request req, Response resp) {
		int id = req.getInteger("id");
		service.removeQuery(id);
	}

	@MsgbusMethod
	public void startQuery(Request req, Response resp) {
		int id = req.getInteger("id");
		int offset = req.getInteger("offset");
		int limit = req.getInteger("limit");
		Integer timelineLimit = req.getInteger("timeline_limit");

		LogQuery query = service.getQuery(id);
		if (query != null) {
			if (!query.isEnd())
				throw new MsgbusException("0", "already running");

			LogQueryCallback queryCallback = new LogQueryCallbackImpl(req.getOrgId(), query, offset, limit);
			query.registerQueryCallback(queryCallback);
			if (timelineLimit != null) {
				LogTimelineCallback timelineCallback = new LogTimelineCallbackImpl(req.getOrgId(), query,
						timelineLimit.intValue());
				query.registerTimelineCallback(timelineCallback);
			}

			new Thread(query, "Log Query " + id).start();
		}
	}

	@MsgbusMethod
	public void getResult(Request req, Response resp) {
		int id = req.getInteger("id");
		int offset = req.getInteger("offset");
		int limit = req.getInteger("limit");

		Map<String, Object> m = getResultData(id, offset, limit);
		if (m != null)
			resp.putAll(m);
	}

	@MsgbusMethod(type = CallbackType.SessionClosed)
	public void sessionClosed(Session session) {
		List<LogQuery> q = queries.get(session);
		if (q != null) {
			for (LogQuery lq : q) {
				lq.cancel();
				service.removeQuery(lq.getId());
			}
		}
		queries.remove(session);
	}

	private class LogQueryCallbackImpl implements LogQueryCallback {
		private int orgId;
		private LogQuery query;
		private int offset;
		private int limit;

		private LogQueryCallbackImpl(int orgId, LogQuery query, int offset, int limit) {
			this.orgId = orgId;
			this.query = query;
			this.offset = offset;
			this.limit = limit;
		}

		@Override
		public int offset() {
			return offset;
		}

		@Override
		public int limit() {
			return limit;
		}

		@Override
		public void pageLoadedCallback(FileBufferList<Map<String, Object>> result) {
			Map<String, Object> m = getResultData(query.getId(), offset, limit);
			m.put("id", query.getId());
			m.put("type", "page_loaded");
			pushApi.push(orgId, "logstorage-query-" + query.getId(), m);
		}

		@Override
		public void eofCallback() {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", query.getId());
			m.put("type", "eof");
			m.put("total_count", query.getResult().size());
			pushApi.push(orgId, "logstorage-query-" + query.getId(), m);
			query.unregisterQueryCallback(this);
		}
	}

	private Map<String, Object> getResultData(int id, int offset, int limit) {
		LogQuery query = service.getQuery(id);
		if (query != null) {
			Map<String, Object> m = new HashMap<String, Object>();

			m.put("result", query.getResult(offset, limit));
			m.put("count", query.getResult().size());

			return m;
		}
		return null;
	}

	private class LogTimelineCallbackImpl implements LogTimelineCallback {
		private Logger logger = LoggerFactory.getLogger(LogTimelineCallbackImpl.class);
		private final long CALLBACK_INTERVAL = 2000;
		private int orgId;
		private LogQuery query;
		private int size;
		private Map<Long, Integer> timeline = new HashMap<Long, Integer>();
		private SpanValue[] spans = new SpanValue[] { new SpanValue(Calendar.MINUTE, 1),
				new SpanValue(Calendar.MINUTE, 10), new SpanValue(Calendar.HOUR_OF_DAY, 1),
				new SpanValue(Calendar.DAY_OF_YEAR, 1), new SpanValue(Calendar.WEEK_OF_YEAR, 1),
				new SpanValue(Calendar.MONTH, 1) };
		private int spansIndex = 0;
		private long lastCallbackTime;

		private LogTimelineCallbackImpl(int orgId, LogQuery query) {
			this(orgId, query, 10);
		}

		private LogTimelineCallbackImpl(int orgId, LogQuery query, int size) {
			this.orgId = orgId;
			this.query = query;
			this.size = size;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public void setSize(int size) {
			this.size = size;
		}

		@Override
		public void put(Date date) {
			long time = date.getTime();
			time = time - time % 86400;
			if (timeline.containsKey(time))
				timeline.put(time, timeline.get(time) + 1);
			else
				timeline.put(time, 1);

			if (System.currentTimeMillis() > lastCallbackTime + CALLBACK_INTERVAL) {
				callback();
				lastCallbackTime = System.currentTimeMillis();
			}
		}

		@Override
		public void callback() {
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

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", query.getId());
			m.put("span_field", spans[spansIndex].getFieldName());
			m.put("span_amount", spans[spansIndex].amount);
			m.put("begin", new Date(beginTime));
			m.put("values", values);
			m.put("count", query.getResult().size());
			pushApi.push(orgId, "logstorage-query-timeline-" + query.getId(), m);

			logger.trace("kraken logstorage: timeline callback => "
					+ "{id={}, span_field={}, span_amount={}, begin={}, values={}, count={}}",
					new Object[] { query.getId(), spans[spansIndex].getFieldName(), spans[spansIndex].amount,
							new Date(beginTime), Arrays.toString(values), query.getResult().size() });
		}

		private class SpanValue {
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
}
