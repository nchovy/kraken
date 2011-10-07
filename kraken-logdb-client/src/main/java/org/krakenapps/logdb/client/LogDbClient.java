package org.krakenapps.logdb.client;

import java.net.InetSocketAddress;

import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;

public class LogDbClient {
	private String guid;
	private InetSocketAddress remote;
	private RpcClient client;
	private RpcSession session;

	public static void main(String[] args) throws RpcException, InterruptedException {
		LogDbClient client = new LogDbClient("test-guid", new InetSocketAddress(7139));
		try {
			client.connect();
			client.dropTable("qoo");
		} finally {
			client.close();
		}
	}

	public LogDbClient(String guid, InetSocketAddress remote) {
		this.guid = guid;
		this.remote = remote;
	}

	public void connect() throws RpcException, InterruptedException {
		RpcConnectionProperties props = new RpcConnectionProperties(remote);
		props.setPassword("1234");
		
		client = new RpcClient(guid);
		RpcConnection conn = client.connect(props);
		session = conn.createSession("logdb");
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
		client.close();
	}
}
