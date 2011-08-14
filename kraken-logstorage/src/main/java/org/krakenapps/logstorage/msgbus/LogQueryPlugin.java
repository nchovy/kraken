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

import java.nio.BufferOverflowException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.krakenapps.msgbus.PushApi;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.Session;
import org.krakenapps.msgbus.handler.CallbackType;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;

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
			LogQueryCallback queryCallback = new LogQueryCallbackImpl(req.getOrgId(), query, offset, limit);
			query.registerQueryCallback(queryCallback);
			if (timelineLimit != null) {
				LogTimelineCallback timelineCallback = new LogTimelineCallbackImpl(req.getOrgId(),
						timelineLimit.intValue());
				query.registerTimelineCallback(timelineCallback);
			}

			new Thread(query, "Log Query " + id).start();
		}
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
			pushApi.push(orgId, "logstorage-query", m);
		}

		@Override
		public void eofCallback() {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", query.getId());
			m.put("type", "eof");
			m.put("total_count", query.getResult().size());
			pushApi.push(orgId, "logstorage-query", m);
			query.unregisterQueryCallback(this);
		}
	}

	private class LogTimelineCallbackImpl implements LogTimelineCallback {
		private int orgId;
		private int limit;

		public LogTimelineCallbackImpl(int orgId, int limit) {
			this.orgId = orgId;
			this.limit = limit;
		}

		@Override
		public int limit() {
			return limit;
		}

		@Override
		public void callback(int spanField, int spanAmount, Map<Date, Integer> timeline) {
			if (timeline.size() > limit)
				throw new BufferOverflowException();

			String fieldName = "";
			if (spanField == Calendar.MINUTE)
				fieldName = "Minute";
			else if (spanField == Calendar.HOUR_OF_DAY)
				fieldName = "Hour";
			else if (spanField == Calendar.DAY_OF_MONTH)
				fieldName = "Day";
			else if (spanField == Calendar.MONTH)
				fieldName = "Month";

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("span_field", fieldName);
			m.put("span_amount", spanAmount);
			Map<String, Object> t = new HashMap<String, Object>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (Date key : timeline.keySet())
				t.put(sdf.format(key), timeline.get(key));
			m.put("timeline", t);
			pushApi.push(orgId, "logstorage-query-timeline", m);
		}
	}

	@MsgbusMethod
	public void getResult(Request req, Response resp) {
		int id = req.getInteger("id");
		int offset = req.getInteger("offset");
		int limit = req.getInteger("limit");

		resp.putAll(getResultData(id, offset, limit));
	}

	private Map<String, Object> getResultData(int id, int offset, int limit) {
		LogQuery query = service.getQuery(id);
		Map<String, Object> m = new HashMap<String, Object>();

		m.put("result", query.getResult(offset, limit));
		m.put("count", query.getResult().size());

		return m;
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
}
