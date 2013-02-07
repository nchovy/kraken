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
package org.krakenapps.logdb.client;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDbClient {
	private final Logger logger = LoggerFactory.getLogger(LogDbClient.class.getName());

	private String guid;
	private RpcClient client;
	private RpcSession session;
	private int bufferSize;
	private LogDbClientRpcService service;
	private Map<Integer, LogQueryStatus> queries;

	private List<Object> logs = new LinkedList<Object>();

	public static void main(String[] args) throws RpcException, InterruptedException {
		final LogDbClient client = new LogDbClient("test-guid");

		try {
			client.connect(new InetSocketAddress(7139), "1234");
			LogQueryStatus q = client.createQuery("table test");
			q.addCallback(new LogQueryCallback() {

				@Override
				public void onPageLoaded(int queryId) {
				}

				@Override
				public void onEof(int queryId) {
					try {
						LogQueryResult r = client.getResult(queryId, 0, 10);
						System.out.println("total " + r.getTotalCount());
						Iterator<Object> it = r.getResult();

						while (it.hasNext()) {
							@SuppressWarnings("unchecked")
							Map<String, Object> m = (Map<String, Object>) it.next();
							System.out.println(m.get("_id") + ", " + m.get("line"));
						}

						client.removeQuery(queryId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			System.out.println(q);

			client.startQuery(q.getId(), 0, 10, 5);
			client.waitFor(q.getId(), 5000);
		} finally {
			client.close();
		}
	}

	public LogDbClient(String guid) {
		this.guid = guid;
		this.service = new LogDbClientRpcService(new RpcEventHandler());
		this.queries = new ConcurrentHashMap<Integer, LogQueryStatus>();
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void connect(InetSocketAddress remote, String password) throws RpcException, InterruptedException {
		RpcConnectionProperties props = new RpcConnectionProperties(remote);
		props.setPassword(password);

		client = new RpcClient(guid);
		RpcConnection conn = client.connect(props);
		conn.bind("logdb-client", service);
		session = conn.createSession("logdb");
	}

	public void connectSsl(InetSocketAddress remote, KeyManagerFactory kmf, TrustManagerFactory tmf)
			throws RpcException, InterruptedException {
		RpcConnectionProperties props = new RpcConnectionProperties(remote, kmf, tmf);

		client = new RpcClient(guid);
		RpcConnection conn = client.connect(props);
		conn.bind("logdb-client", service);
		session = conn.createSession("logdb");
	}

	public void write(Log log) throws RpcException, InterruptedException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("table", log.getTableName());
		m.put("date", log.getDate());
		m.put("data", log.getData());
		logs.add(m);

		if (logs.size() > bufferSize)
			flush();
	}

	public LogQueryStatus createQuery(String query) throws RpcException, InterruptedException {
		int id = (Integer) session.call("createQuery", query);
		LogQueryStatus q = new LogQueryStatus(id, query, false);
		queries.put(id, q);
		return q;
	}

	public void startQuery(int queryId, int offset, int limit, int timelineSize) throws RpcException,
			InterruptedException {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("id", queryId);
		options.put("offset", offset);
		options.put("limit", limit);
		options.put("timeline_size", timelineSize);

		session.call("startQuery", options);
	}

	public void removeQuery(int queryId) throws RpcException, InterruptedException {
		session.call("removeQuery", queryId);
		queries.remove(queryId);
	}

	public void waitFor(int queryId, int timeout) throws InterruptedException {
		Date begin = new Date();

		while (true) {
			LogQueryStatus status = queries.get(queryId);
			if (status == null || status.isEnded())
				break;

			Date now = new Date();
			if (now.getTime() - begin.getTime() > timeout)
				throw new InterruptedException("timed out");

			Thread.sleep(100);
		}
	}

	@SuppressWarnings("unchecked")
	public LogQueryResult getResult(int queryId, int offset, int limit) throws RpcException, InterruptedException {

		Map<String, Object> m = (Map<String, Object>) session.call("getResult", queryId, offset, limit);
		int totalCount = (Integer) m.get("count");
		Object[] result = (Object[]) m.get("result");

		return new LogQueryResult(offset, limit, totalCount, Arrays.asList(result));
	}

	/**
	 * flush logs
	 * 
	 * @throws InterruptedException
	 * @throws RpcException
	 */
	public void flush() throws RpcException, InterruptedException {
		if (logs.isEmpty())
			return;

		if (logger.isTraceEnabled())
			logger.trace("kraken logdb client: flushing {} logs", logs.size());

		session.call("writeLogs", logs);
		logs.clear();
	}

	public void createTable(String tableName) throws RpcException, InterruptedException {
		session.call("createTable", tableName, null);
	}

	public void dropTable(String tableName) throws RpcException, InterruptedException {
		session.call("dropTable", tableName);
	}

	public void close() throws RpcException, InterruptedException {
		try {
			flush();
		} finally {
			client.close();
		}
	}

	private class RpcEventHandler implements LogDbClientRpcCallback {

		@Override
		public void onPageLoaded(int queryId, int offset, int limit) {

			logger.info("kraken logdb client: on page loaded, id: {}, offset: {}, limit: {}", new Object[] { queryId,
					offset, limit });

			LogQueryStatus status = queries.get(queryId);

			if (status == null) {
				logger.warn("kraken logdb client: query [{}] not found", queryId);
				return;
			}

			for (LogQueryCallback callback : status.getCallbacks())
				callback.onPageLoaded(queryId);
		}

		@Override
		public void onEof(int queryId, int offset, int limit) {

			logger.info("kraken logdb client: on eof id: {}, offset: {}, limit: {}", new Object[] { queryId, offset,
					limit });

			LogQueryStatus status = queries.get(queryId);

			if (status == null) {
				logger.warn("kraken logdb client: query [{}] not found", queryId);
				return;
			}

			status.setRunning(false);
			status.setEnded(true);

			for (LogQueryCallback callback : status.getCallbacks())
				callback.onEof(queryId);
		}
	}
}
