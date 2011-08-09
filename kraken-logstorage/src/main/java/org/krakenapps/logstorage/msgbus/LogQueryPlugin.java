package org.krakenapps.logstorage.msgbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogQueryCallback;
import org.krakenapps.logstorage.LogQueryService;
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

		LogQuery query = service.getQuery(id);
		LogQueryCallback callback = new LogQueryCallbackImpl(req.getOrgId(), query, offset, limit);
		query.registerCallback(callback);
		new Thread(query, "Log Query " + id).start();
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
		public void callback(FileBufferList<Map<String, Object>> result) {
			pushApi.push(orgId, "logstorage-query", getResultData(query.getId(), offset, limit));
			query.unregisterCallback(this);
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
		m.put("total_counts", query.getResult().size());

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
