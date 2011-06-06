package org.krakenapps.grid.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;

public class GridClient {
	private static final int RPC_PORT = 7139;
	private RpcAgent rpcAgent;
	private InetAddress locatorAddr;
	private int timeout = 10000;

	/**
	 * rpc method name to grid nodes;
	 */
	private Map<String, List<InetAddress>> rpcMethodMap;

	private Map<InetAddress, RpcConnection> connections;

	public GridClient(RpcAgent rpcAgent, InetAddress locatorAddr) {
		this.rpcAgent = rpcAgent;
		this.locatorAddr = locatorAddr;
		this.connections = new HashMap<InetAddress, RpcConnection>();
	}

	public Object call(String method, Object[] params) throws RpcException, InterruptedException {
		return call(method, params, timeout);
	}

	public Object call(String method, Object[] params, int timeout) throws RpcException, InterruptedException {
		RpcSession session = null;
		try {
			session = getSession(method);
			Object value = session.call(method, params, timeout);
			return value;
		} finally {
			if (session != null)
				session.close();
		}
	}

	private RpcSession getSession(String method) throws RpcException, InterruptedException {
		RpcConnection conn = getConnection(method);
		int p = method.indexOf('.');
		String serviceName = method.substring(0, p);
		return conn.createSession(serviceName);
	}

	private RpcConnection getConnection(String method) throws RpcException, InterruptedException {
		if (this.rpcMethodMap == null)
			refreshNodes();

		InetAddress serverAddr = selectServer(method);

		// find existing connection
		RpcConnection conn = null;

		for (RpcConnection c : rpcAgent.getConnections()) {
			if (c.getRemoteAddress().getAddress().equals(serverAddr)) {
				conn = c;
				break;
			}
		}

		// if not, try to connect server
		if (conn == null) {
			conn = rpcAgent.connect(new InetSocketAddress(serverAddr, RPC_PORT));
			connections.put(serverAddr, conn);
		}

		return conn;
	}

	private InetAddress selectServer(String methodName) {
		List<InetAddress> serverAddrs = rpcMethodMap.get(methodName);
		int selected = new Random().nextInt(serverAddrs.size());
		return serverAddrs.get(selected);
	}

	private void refreshNodes() throws RpcException, InterruptedException {
		RpcSession session = null;
		try {
			RpcConnection locator = findLocator();
			session = locator.createSession("kraken-grid-locator");

			Map<String, List<InetAddress>> methodMap = new HashMap<String, List<InetAddress>>();
			Map<InetAddress, Object> m = (Map<InetAddress, Object>) session.call("grid-locator.getGridServices", null);

			for (InetAddress ip : m.keySet()) {
				List<String> methodNames = (List<String>) m.get(ip);
				for (String methodName : methodNames) {
					List<InetAddress> ipSet = methodMap.get(methodName);
					if (ipSet == null) {
						ipSet = new ArrayList<InetAddress>();
						methodMap.put(methodName, ipSet);
					}

					if (!ipSet.contains(ip))
						ipSet.add(ip);
				}
			}

			this.rpcMethodMap = methodMap;
		} finally {
			if (session != null)
				session.close();
		}
	}

	private RpcConnection findLocator() {
		for (RpcConnection conn : rpcAgent.getConnections())
			if (conn.getPropertyKeys().contains("kraken-grid-locator"))
				return conn;
		return null;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int milliseconds) {
		this.timeout = milliseconds;
	}

	public void close() throws IOException {
	}
}
