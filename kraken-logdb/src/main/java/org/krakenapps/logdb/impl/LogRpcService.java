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
package org.krakenapps.logdb.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCallback;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogTimelineCallback;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.SimpleRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logdb-rpc")
@Provides
public class LogRpcService extends SimpleRpcService {
	private final Logger logger = LoggerFactory.getLogger(LogRpcService.class.getName());

	@SuppressWarnings("unused")
	@ServiceProperty(name = "rpc.name", value = "logdb")
	private String name;

	@Requires
	private LogQueryService qs;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private LogStorage logStorage;

	@RpcMethod(name = "createTable")
	public void createTable(String tableName, Map<String, Object> options) {
		tableRegistry.createTable(tableName, null);
		logger.info("kraken logdb: created table [{}] from [{}]", tableName, RpcContext.getConnection());
	}

	@RpcMethod(name = "dropTable")
	public void dropTable(String tableName) {
		tableRegistry.dropTable(tableName);
		logger.info("kraken logdb: dropped table [{}] from [{}]", tableName, RpcContext.getConnection());
	}

	@SuppressWarnings("unchecked")
	@RpcMethod(name = "writeLogs")
	public void writeLogs(Object[] logs) {
		for (Object o : logs) {
			Map<String, Object> m = (Map<String, Object>) o;
			String tableName = (String) m.get("table");
			Date date = (Date) m.get("date");
			Map<String, Object> data = (Map<String, Object>) m.get("data");
			Log log = new Log(tableName, date, data);

			logStorage.write(log);
		}
	}

	@RpcMethod(name = "getQueries")
	public List<Object> getQueries() {
		List<Object> result = new ArrayList<Object>();
		for (LogQuery lq : qs.getQueries()) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("id", lq.getId());
			m.put("query_string", lq.getQueryString());
			m.put("is_end", lq.isEnd());
			result.add(m);
		}

		return result;
	}

	@RpcMethod(name = "createQuery")
	public int createQuery(String query) {
		LogQuery q = qs.createQuery(query);
		return q.getId();
	}

	@RpcMethod(name = "removeQuery")
	public void removeQuery(int id) {
		qs.removeQuery(id);
	}

	@RpcMethod(name = "startQuery")
	public void startQuery(Map<String, Object> options) {
		RpcSession session = RpcContext.getSession();

		int id = (Integer) options.get("id");
		int offset = (Integer) options.get("offset");
		int limit = (Integer) options.get("limit");
		Integer timelineSize = (Integer) options.get("timeline_size");

		// TODO: general rpc callback infra

		LogQuery query = qs.getQuery(id);
		if (query != null) {
			if (!query.isEnd())
				throw new RpcException("already running");

			LogQueryCallback qc = new RpcQueryCallback(session, query, offset, limit);
			query.registerQueryCallback(qc);

			if (timelineSize != null) {
				int size = timelineSize.intValue();
				LogTimelineCallback tc = new RpcTimelineCallback(session, query, size);
				query.registerTimelineCallback(tc);
			}

			new Thread(query, "Log Query " + id).start();
		}
	}

	@RpcMethod(name = "getResult")
	public Map<String, Object> getResult(int id, int offset, int limit) {
		return LogQueryHelper.getResultData(qs, id, offset, limit);
	}

	private class RpcQueryCallback implements LogQueryCallback {
		private RpcSession session;
		private LogQuery query;
		private int offset;
		private int limit;

		public RpcQueryCallback(RpcSession session, LogQuery query, int offset, int limit) {
			this.session = session;
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
		public void onPageLoaded(FileBufferList<Map<String, Object>> result) {
			logger.info("kraken logdb: page loaded for query [{}], offset [{}], limit [{}]", new Object[] { query,
					offset, limit });
		}

		@Override
		public void onEof() {
			logger.info("kraken logdb: eof for query [{}], offset [{}], limit [{}]", new Object[] { query, offset,
					limit });
		}
	}

	private class RpcTimelineCallback implements LogTimelineCallback {
		private RpcSession session;
		private LogQuery query;
		private int size;

		public RpcTimelineCallback(RpcSession session, LogQuery query, int size) {
			this.session = session;
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
		}

		@Override
		public void callback() {
		}
	}
}
