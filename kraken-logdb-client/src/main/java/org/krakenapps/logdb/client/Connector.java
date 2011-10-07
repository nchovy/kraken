package org.krakenapps.logdb.client;

import java.net.InetSocketAddress;

import org.krakenapps.rpc.RpcClient;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;

public class Connector {
	private InetSocketAddress remote;
	private RpcClient client;

	public Connector(InetSocketAddress remote) {
		this.remote = remote;
	}

	public void connect() throws RpcException, InterruptedException {
		client = new RpcClient("test-guid");
		try {
			RpcConnectionProperties props = new RpcConnectionProperties(remote);
			props.setPassword("1234");
			RpcConnection conn = client.connect(props);
			RpcSession session = conn.createSession("logdb");
			System.out.println("before");
			System.out.println(session.call("createQuery", "table xtm"));
			System.out.println("completed");
		} finally {
			client.close();
		}
	}

	public static void main(String[] args) throws RpcException, InterruptedException {
		new Connector(new InetSocketAddress(7139)).connect();
	}
}
