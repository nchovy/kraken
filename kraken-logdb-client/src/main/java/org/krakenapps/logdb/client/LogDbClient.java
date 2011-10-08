package org.krakenapps.logdb.client;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	private InetSocketAddress remote;
	private RpcClient client;
	private RpcSession session;
	private int bufferSize;

	private List<Object> logs = new LinkedList<Object>();

	public static void main(String[] args) throws RpcException, InterruptedException {
		LogDbClient client = new LogDbClient("test-guid", new InetSocketAddress(7139));
		try {
			Log log = new Log("test", "hello world");
			client.connect();
			LogQueryStatus q = client.createQuery("table test");
			client.startQuery(q, 0, 10, 5);
			System.out.println(q);
		} finally {
			client.close();
		}
	}

	public LogDbClient(String guid, InetSocketAddress remote) {
		this.guid = guid;
		this.remote = remote;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void connect() throws RpcException, InterruptedException {
		RpcConnectionProperties props = new RpcConnectionProperties(remote);
		props.setPassword("1234");

		client = new RpcClient(guid);
		RpcConnection conn = client.connect(props);
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
		return q;
	}

	public void startQuery(LogQueryStatus query, int offset, int limit, int timelineSize) throws RpcException,
			InterruptedException {
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("id", query.getId());
		options.put("offset", offset);
		options.put("limit", limit);
		options.put("timeline_size", timelineSize);

		session.call("startQuery", options);
	}

	public void removeQuery(LogQueryStatus query) throws RpcException, InterruptedException {
		session.call("removeQuery", query.getId());
	}

	@SuppressWarnings("unchecked")
	public LogQueryResult getResult(LogQueryStatus query, int offset, int limit) throws RpcException,
			InterruptedException {

		Map<String, Object> m = (Map<String, Object>) session.call("getResult", query.getId(), offset, limit);
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
}
