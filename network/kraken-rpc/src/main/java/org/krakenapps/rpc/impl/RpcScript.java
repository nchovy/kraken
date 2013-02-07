/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.rpc.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.rpc.RpcBindingProperties;
import org.krakenapps.rpc.RpcBlockingTable;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcPeer;
import org.krakenapps.rpc.RpcPeerRegistry;
import org.krakenapps.rpc.RpcPeeringCallback;
import org.krakenapps.rpc.RpcServiceBinding;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcTrustLevel;
import org.krakenapps.rpc.RpcWaitingCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(RpcScript.class.getName());
	private ScriptContext context;
	private RpcAgent agent;
	private KeyStoreManager keyStoreManager;

	public RpcScript(RpcAgent agent, KeyStoreManager keyStoreManager) {
		this.agent = agent;
		this.keyStoreManager = keyStoreManager;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void bindings(String[] args) {
		context.println("Port Bindings");
		context.println("---------------");
		for (RpcBindingProperties props : agent.getBindings())
			context.println(props);
	}

	@ScriptUsage(description = "open rpc port", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "rpc port"),
			@ScriptArgument(name = "ip", type = "string", description = "bind address. '0.0.0.0' by default", optional = true) })
	public void open(String[] args) {
		RpcBindingProperties props = null;
		try {
			props = inputBindingProps(args);
			agent.open(props);
			context.println("opened rpc port: " + args[0]);
		} catch (NumberFormatException e) {
			context.println("invalid port number format");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken rpc: cannot bind " + props, e);
		}
	}

	@ScriptUsage(description = "open rpc ssl port", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "rpc listening port for ssl"),
			@ScriptArgument(name = "key alias", type = "string", description = "key alias. use 'keystore.list' command to list all registered key aliases. Keystore should contain public and private key pair. e.g. PKCS12 keystore"),
			@ScriptArgument(name = "trust alias", type = "string", description = "trust alias. use 'keystore.list' command to list all registered key aliases. Trusted keystore should contain public key of certificate authority"),
			@ScriptArgument(name = "ip", type = "string", description = "bind address. '0.0.0.0' by default", optional = true) })
	public void openSsl(String[] args) {
		RpcBindingProperties props = null;
		try {
			String keyAlias = args[1];
			String trustAlias = args[2];
			String ip = "0.0.0.0";
			if (args.length > 3)
				ip = args[3];

			args[1] = ip;
			args[2] = keyAlias;
			args[3] = trustAlias;
			props = inputBindingProps(args);

			agent.open(props);
			context.println("opened rpc (ssl) port: " + args[0]);
		} catch (NumberFormatException e) {
			context.println("invalid port number format");
		} catch (Exception e) {
			context.println(e.getMessage());
			logger.error("kraken rpc: cannot bind " + props, e);
		}
	}

	@ScriptUsage(description = "close rpc port or rpc ssl port", arguments = {
			@ScriptArgument(name = "port", type = "int", description = "rpc port or rpc ssl port"),
			@ScriptArgument(name = "ip", type = "string", description = "bind address. '0.0.0.0' by default", optional = true) })
	public void close(String[] args) {
		try {
			RpcBindingProperties props = inputBindingProps(args);
			if (!agent.getBindings().contains(props)) {
				String endpoint = "0.0.0.0";
				if (args.length > 1)
					endpoint = args[1];
				endpoint += ":" + args[0];
				context.println(endpoint + " not opened");
				return;
			}

			agent.close(props);
			context.println("closed rpc port");
		} catch (NumberFormatException e) {
			context.println("invalid port number format");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private RpcBindingProperties inputBindingProps(String[] args) {
		int port = Integer.valueOf(args[0]);
		if (port < 1 || port > 65535)
			throw new IllegalArgumentException("port number should be 1~65535");

		String addr = "0.0.0.0";
		if (args.length > 1)
			addr = args[1];

		String keyAlias = null;
		String trustAlias = null;

		if (args.length >= 4) {
			keyAlias = args[2];
			trustAlias = args[3];
		}

		return new RpcBindingProperties(addr, port, keyAlias, trustAlias);
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

		RpcPeer peer = new RpcPeerConfig(guid, password, trustLevel);
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

		PeeringResultPrinter p = new PeeringResultPrinter();
		try {
			p.lock.lock();
			conn.requestPeering(p, password);
			p.cond.await();
		} catch (InterruptedException e) {
			context.println("interrupted");
		} finally {
			p.lock.unlock();
		}

	}

	private class PeeringResultPrinter implements RpcPeeringCallback {
		private ReentrantLock lock;
		private Condition cond;

		public PeeringResultPrinter() {
			lock = new ReentrantLock();
			cond = lock.newCondition();
		}

		@Override
		public void onCompleted(RpcConnection conn) {
			try {
				lock.lock();

				if (conn.getTrustedLevel() == null || conn.getTrustedLevel() == RpcTrustLevel.Untrusted)
					context.println(conn.getRemoteAddress() + " peering failed");
				else
					context.println(conn.getRemoteAddress() + " peering succeeded");

				cond.signalAll();
			} finally {
				lock.unlock();
			}
		}
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
			if (isSsl) {
				TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory(trustAlias, "SunX509");
				KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory(keyAlias, "SunX509");
				connection = agent.connectSsl(new RpcConnectionProperties(host, port, kmf, tmf));
			} else
				connection = agent.connect(new RpcConnectionProperties(host, port));

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
	public void services(String[] args) {
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

	@ScriptUsage(description = "print all connection properties", arguments = { @ScriptArgument(name = "cid", type = "int", description = "connection id") })
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
