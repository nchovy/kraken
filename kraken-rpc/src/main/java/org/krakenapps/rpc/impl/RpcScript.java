package org.krakenapps.rpc.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.krakenapps.rpc.RpcServiceBinding;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcWaitingCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RpcScript.class.getName());
	private ScriptContext context;
	private RpcAgent agent;

	public RpcScript(RpcAgent agent) {
		this.agent = agent;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	@ScriptUsage(description = "print guid of local agent")
	public void guid(String[] args) {
		context.println(agent.getGuid());
	}

	@ScriptUsage(description = "list all registered peers")
	public void peers(String[] args) {
		RpcPeerRegistry registry = agent.getPeerRegistry();
		context.println("RPC Peers");
		context.println("--------------");

		for (String guid : registry.getPeerGuids()) {
			context.println(guid);
		}
	}

	@ScriptUsage(description = "register rpc peer", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "the guid of peer"),
			@ScriptArgument(name = "password", type = "string", description = "the password of peer"),
			@ScriptArgument(name = "trust level", type = "int", description = "1(untrust), 2(low), 3(medium), 4(high)") })
	public void registerPeer(String[] args) {
		RpcPeerRegistry registry = agent.getPeerRegistry();
		String guid = args[0];
		String password = args[1];
		int trustLevel = Integer.parseInt(args[2]);

		if (trustLevel < 1 || trustLevel > 4) {
			context.println("trust level should be between 1 and 4");
			return;
		}

		RpcPeer peer = new RpcPeerImpl(guid, password, trustLevel);
		try {
			registry.register(peer);
			context.println(peer.getGuid() + " registered");
		} catch (IllegalStateException e) {
			context.println("register failed: " + e.getMessage());
		}
	}

	@ScriptUsage(description = "unregister rpc peer", arguments = { @ScriptArgument(name = "guid", type = "string", description = "peer's guid") })
	public void unregisterPeer(String[] args) {
		RpcPeerRegistry registry = agent.getPeerRegistry();
		String guid = args[0];
		try {
			registry.unregister(guid);
			context.println(guid + " unregistered");
		} catch (IllegalStateException e) {
			context.println("unregister failed: " + e.getMessage());
		}
	}

	public void connections(String[] args) {
		context.println("RPC Connections");
		context.println("----------------");
		for (RpcConnection connection : agent.getConnections()) {
			context.println(connection.toString());
		}
	}

	@ScriptUsage(description = "send peering request", arguments = {
			@ScriptArgument(name = "cid", type = "int", description = "connection id"),
			@ScriptArgument(name = "password", type = "string", description = "password", optional = true) })
	public void requestPeering(String[] args) {
		int id = Integer.parseInt(args[0]);
		String password = null;
		if (args.length > 1)
			password = args[1];

		RpcConnection conn = agent.findConnection(id);

		if (conn == null) {
			context.println("connection not found");
			return;
		}

		boolean ret = conn.requestPeering(password);
		if (ret)
			context.println(conn.getRemoteAddress() + " peering succeeded");
		else
			context.println(conn.getRemoteAddress() + " peering failed");
	}

	@ScriptUsage(description = "connect to rpc peer", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "host name or ip address"),
			@ScriptArgument(name = "port", type = "int", description = "port number", optional = true),
			@ScriptArgument(name = "properties", description = "key=value pairs", optional = true) })
	public void connect(String[] args) {
		doConnect(false, args);
	}

	@ScriptUsage(description = "ssl connect to rpc peer", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "host name or ip address"),
			@ScriptArgument(name = "port", type = "int", description = "port number"),
			@ScriptArgument(name = "key alias", type = "string", description = "private key (PKCS#12)"),
			@ScriptArgument(name = "trust alias", type = "string", description = "CA key (JKS)") })
	public void connectSsl(String[] args) {
		doConnect(true, args);
	}

	private void doConnect(boolean isSsl, String[] args) {
		try {
			String host = InetAddress.getByName(args[0]).getHostAddress();
			int port = isSsl ? 7140 : 7139;
			String keyAlias = null;
			String trustAlias = null;

			if (args.length > 1)
				port = Integer.parseInt(args[1]);

			if (isSsl) {
				keyAlias = args[2];
				trustAlias = args[3];
			}

			RpcConnection connection = null;
			if (isSsl)
				connection = agent.connectSsl(host, port, keyAlias, trustAlias);
			else
				connection = agent.connect(host, port);

			if (connection != null)
				context.printf("%s connected\n", connection.getRemoteAddress());
			else
				context.println("connect failed");
		} catch (UnknownHostException e) {
			context.println("unknown host: " + args[0]);
			logger.warn("connect failed", e);
		} catch (Exception e) {
			if (e.getMessage() != null)
				context.println(e.getMessage());
			else
				context.println("connect failed");
			logger.warn("connect failed", e);
		}
	}

	@ScriptUsage(description = "terminate connection gracefully", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connection id") })
	public void disconnect(String[] args) {
		int id = Integer.parseInt(args[0]);
		RpcConnection connection = agent.findConnection(id);
		if (connection == null) {
			context.println("connection not found");
			return;
		}

		connection.close();
		context.println("disconnected " + connection.toString());
	}

	@ScriptUsage(description = "terminate all connections gracefully")
	public void disconnectAll(String[] args) {
		// guard for concurrent modification exception
		Collection<RpcConnection> connections = new ArrayList<RpcConnection>(agent.getConnections());
		for (RpcConnection connection : connections) {
			if (connection.isOpen())
				connection.close();
		}
	}

	// session list of the connection
	// at least, there should be one session, "rpc-control"
	@ScriptUsage(description = "list all sessions of the connection", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connection id") })
	public void sessions(String[] args) {
		int id = -1;
		try {
			id = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			context.println("connection id should be number");
			return;
		}

		RpcConnection conn = agent.findConnection(id);
		if (conn == null) {
			context.println("connection not found");
			return;
		}

		context.println("RPC Sessions");
		context.println("---------------");
		for (RpcSession session : conn.getSessions()) {
			context.println(session.toString());
		}
	}

	// listening providers
	@ScriptUsage(description = "list all bound services", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connection id") })
	public void bindings(String[] args) {
		int id = Integer.parseInt(args[0]);
		RpcConnection conn = agent.findConnection(id);
		if (conn == null) {
			context.println("connection not found");
			return;
		}

		context.println("Service Bindings");
		context.println("-------------------");
		for (RpcServiceBinding binding : conn.getServiceBindings()) {
			context.println(binding.toString());
		}
	}

	@ScriptUsage(description = "cancel rpc calls of the connection", arguments = {
			@ScriptArgument(name = "cid", type = "int", description = "connection id"),
			@ScriptArgument(name = "call id", type = "int", description = "call id") })
	public void cancel(String[] args) {
		int connId = Integer.parseInt(args[0]);
		int callId = Integer.parseInt(args[1]);

		RpcConnection conn = agent.findConnection(connId);
		RpcBlockingTable blockingTable = conn.getBlockingTable();

		blockingTable.cancel(callId);
		context.println("call cancelled");
	}

	@ScriptUsage(description = "list all waiting calls of the connection", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connection id") })
	public void waitings(String[] args) {
		int id = Integer.parseInt(args[0]);
		RpcConnection conn = agent.findConnection(id);
		RpcBlockingTable blockingTable = conn.getBlockingTable();

		context.println("RPC Waiting Calls");
		context.println("---------------------");
		for (RpcWaitingCall waiting : blockingTable.getWaitingCalls()) {
			context.println(waiting.toString());
		}
	}

	@ScriptUsage(description = "set connection property", arguments = {
			@ScriptArgument(name = "cid", type = "int", description = "connection id"),
			@ScriptArgument(name = "property key", type = "string", description = "property key name"),
			@ScriptArgument(name = "property value", type = "string", description = "property value") })
	public void setprop(String[] args) {
		try {
			int cid = Integer.valueOf(args[0]);
			RpcConnection conn = agent.findConnection(cid);
			if (conn == null) {
				context.println("connection not found");
				return;
			}

			conn.setProperty(args[1], args[2]);
			context.println("set");
		} catch (NumberFormatException e) {
			context.println("invalid connection id format");
		}
	}

	@ScriptUsage(description = "print all connection properties", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connectino id") })
	public void props(String[] args) {
		try {
			int cid = Integer.valueOf(args[0]);
			RpcConnection conn = agent.findConnection(cid);
			if (conn == null) {
				context.println("connection not found");
				return;
			}
			
			context.println("RPC Connection Properties");
			context.println("---------------------------");
			
			for (String key : conn.getPropertyKeys()) 
				context.println(key + ": " + conn.getProperty(key));
			
		} catch (NumberFormatException e) {
			context.println("invalid connection id format");
		}
	}
}
