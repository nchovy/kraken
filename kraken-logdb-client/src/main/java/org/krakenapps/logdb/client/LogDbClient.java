package org.krakenapps.logdb.client;

import java.net.InetSocketAddress;
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
			client.write(log);
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

	public void write(Log log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("table", log.getTableName());
		m.put("date", log.getDate());
		m.put("data", log.getData());
		logs.add(m);

		if (logs.size() > bufferSize)
			flush();
	}

	/**
	 * flush logs
	 */
	public void flush() {
		try {
			if (logs.isEmpty())
				return;

			if (logger.isTraceEnabled())
				logger.trace("kraken logdb client: flushing {} logs", logs.size());

			session.call("writeLogs", logs);
			logs.clear();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void createTable(String tableName) {
		try {
			session.call("createTable", tableName, null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void dropTable(String tableName) {
		try {
			session.call("dropTable", tableName);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public void close() {
		try {
			flush();
		} finally {
			client.close();
		}
	}
}
