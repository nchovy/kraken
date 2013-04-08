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
package org.krakenapps.logdb.msgbus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogTimelineCallback;
import org.krakenapps.logdb.impl.LogQueryHelper;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogRestoreService;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
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

@Component(name = "logdb-msgbus")
@MsgbusPlugin
public class LogQueryPlugin {
	private final Logger logger = LoggerFactory.getLogger(LogQueryPlugin.class.getName());

	@Requires
	private LogQueryService service;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private LogStorage storage;

	@Requires
	private LogRestoreService logRestore;

	@Requires
	private DataSourceRegistry dataSourceRegistry;

	@Requires
	private PushApi pushApi;

	private ConcurrentMap<Session, List<LogQuery>> queries = new ConcurrentHashMap<Session, List<LogQuery>>();

	@MsgbusMethod
	public void logs(Request req, Response resp) {
		String tableName = req.getString("table");
		int limit = req.getInteger("limit");
		int offset = 0;
		if (req.has("offset"))
			offset = req.getInteger("offset");

		if (!tableRegistry.exists(tableName))
			throw new MsgbusException("logdb", "table-not-exists");

		Collection<Log> logs = storage.getLogs(tableName, null, null, offset, limit);
		List<Object> serialized = new ArrayList<Object>(limit);
		for (Log log : logs)
			serialized.add(serialize(log));

		resp.put("logs", serialized);
	}

	private Map<String, Object> serialize(Log log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("table", log.getTableName());
		m.put("id", log.getId());
		m.put("date", log.getDate());
		m.put("data", log.getData());
		return m;
	}

	@MsgbusMethod
	public void getDataSources(Request req, Response resp) {
		List<Object> result = new ArrayList<Object>();
		for (DataSource dataSource : dataSourceRegistry.getAll()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("name", dataSource.getName());
			m.put("type", dataSource.getType());
			m.put("node_guid", dataSource.getNodeGuid());
			m.put("metadata", dataSource.getMetadata());
			result.add(m);
		}
		resp.put("sources", result);
	}

	@MsgbusMethod
	public void queries(Request req, Response resp) {
		List<Object> result = LogQueryHelper.getQueries(service);
		resp.put("queries", result);
	}

	@MsgbusMethod
	public void createQuery(Request req, Response resp) {
		try {
			LogQuery query = service.createQuery(req.getString("query"));
			resp.put("id", query.getId());

			// for query cancellation at session close
			Session session = req.getSession();
			queries.putIfAbsent(session, new ArrayList<LogQuery>());

			List<LogQuery> l = queries.get(session);
			synchronized (l) {
				l.add(query);
			}
		} catch (Exception e) {
			logger.error("kraken logdb: cannot create query", e);
			logRestore.restoreByDelete();
			throw new MsgbusException("logdb", e.getMessage());
		}
	}

	@MsgbusMethod
	public void removeQuery(Request req, Response resp) {
		int id = req.getInteger("id");

		LogQuery target = null;
		List<LogQuery> l = queries.get(req.getSession());
		if (l == null) {
			logger.debug("kraken logdb: remove target query not found for session [{}]", req.getSession());
			return;
		}

		synchronized (l) {
			for (LogQuery q : l)
				if (q.getId() == id)
					target = q;

			if (target != null) {
				l.remove(target);
				logger.debug("kraken logdb: removing query [{}] from session [{}]", target.getId(), req.getSession());
			}
		}

		service.removeQuery(id);
	}

	@MsgbusMethod
	public void startQuery(Request req, Response resp) {
		String orgDomain = req.getOrgDomain();
		int id = req.getInteger("id");
		int offset = req.getInteger("offset");
		int limit = req.getInteger("limit");
		Integer timelineLimit = req.getInteger("timeline_limit");

		LogQuery query = service.getQuery(id);

		// validation check
		if (query == null)
			throw new MsgbusException("logdb", "query not found");

		if (!query.isEnd())
			throw new MsgbusException("logdb", "already running");

		// set query and timeline callback
		LogQueryCallback qc = new MsgbusLogQueryCallback(orgDomain, query, offset, limit);
		query.registerQueryCallback(qc);

		if (timelineLimit != null) {
			int size = timelineLimit.intValue();
			LogTimelineCallback tc = new MsgbusTimelineCallback(orgDomain, query, size);
			query.registerTimelineCallback(tc);
		}

		// start query
		service.startQuery(query.getId());
	}

	@MsgbusMethod
	public void stopQuery(Request req, Response resp) {
		int id = req.getInteger("id");
		LogQuery query = service.getQuery(id);
		if (query != null)
			query.cancel();
		else
			throw new MsgbusException("logdb", "query-not-found");
	}

	@MsgbusMethod
	public void getResult(Request req, Response resp) throws IOException {
		int id = req.getInteger("id");
		int offset = req.getInteger("offset");
		int limit = req.getInteger("limit");

		Map<String, Object> m = LogQueryHelper.getResultData(service, id, offset, limit);
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

	private class MsgbusLogQueryCallback implements LogQueryCallback {
		private String orgDomain;
		private LogQuery query;
		private int offset;
		private int limit;

		private MsgbusLogQueryCallback(String orgDomain, LogQuery query, int offset, int limit) {
			this.orgDomain = orgDomain;
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
		public void onQueryStatusChange() {
			try {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", query.getId());
				m.put("type", "status_change");
				m.put("count", query.getResultCount());
				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
			} catch (IOException e) {
				logger.error("kraken logdb: msgbus push fail", e);
			}
		}

		@Override
		public void onPageLoaded() {
			try {
				Map<String, Object> m = LogQueryHelper.getResultData(service, query.getId(), offset, limit);
				m.put("id", query.getId());
				m.put("type", "page_loaded");
				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
			} catch (IOException e) {
				logger.error("kraken logdb: msgbus push fail", e);
			}
		}

		@Override
		public void onEof() {
			try {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", query.getId());
				m.put("type", "eof");
				m.put("total_count", query.getResultCount());
				pushApi.push(orgDomain, "logdb-query-" + query.getId(), m);
				pushApi.push(orgDomain, "logstorage-query-" + query.getId(), m); // deprecated
				query.unregisterQueryCallback(this);
			} catch (IOException e) {
				logger.error("kraken logdb: msgbus push fail", e);
			}
		}
	}

	private class MsgbusTimelineCallback extends LogTimelineCallback {
		private Logger logger = LoggerFactory.getLogger(MsgbusTimelineCallback.class);
		private String orgDomain;
		private LogQuery query;
		private int size;

		private MsgbusTimelineCallback(String orgDomain, LogQuery query) {
			this(orgDomain, query, 10);
		}

		private MsgbusTimelineCallback(String orgDomain, LogQuery query, int size) {
			this.orgDomain = orgDomain;
			this.query = query;
			this.size = size;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		protected void callback(Date beginTime, SpanValue spanValue, int[] values, boolean isEnd) {
			try {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", query.getId());
				m.put("type", isEnd ? "eof" : "periodic");
				m.put("span_field", spanValue.getFieldName());
				m.put("span_amount", spanValue.getAmount());
				m.put("begin", beginTime);
				m.put("values", values);
				pushApi.push(orgDomain, "logdb-query-timeline-" + query.getId(), m);

				m.put("count", query.getResultCount());
				pushApi.push(orgDomain, "logstorage-query-timeline-" + query.getId(), m); // deprecated

				Object[] trace = new Object[] { query.getId(), spanValue.getFieldName(), spanValue.getAmount(), beginTime,
						Arrays.toString(values), query.getResultCount() };
				logger.trace("kraken logdb: timeline callback => "
						+ "{id={}, span_field={}, span_amount={}, begin={}, values={}, count={}}", trace);
			} catch (IOException e) {
				logger.error("kraken logdb: msgbus push fail", e);
			}
		}
	}
}
