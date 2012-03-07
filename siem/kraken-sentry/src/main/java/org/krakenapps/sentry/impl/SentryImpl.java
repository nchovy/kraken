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
package org.krakenapps.sentry.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerFactoryRegistryEventListener;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.LoggerRegistryEventListener;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionEventListener;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.sentry.Base;
import org.krakenapps.sentry.CommandHandler;
import org.krakenapps.sentry.Sentry;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

@Component(name = "sentry")
@Provides
public class SentryImpl implements Sentry, LoggerRegistryEventListener, LoggerFactoryRegistryEventListener {
	private static final String BASE_PATH = "bases";
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(SentryImpl.class.getName());

	@Requires
	private RpcAgent agent;

	@Requires
	private PreferencesService prefsvc;

	@Requires
	private LoggerFactoryRegistry loggerFactoryRegistry;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private KeyStoreManager keyStoreManager;

	/**
	 * base name to log session mappings
	 */
	private ConcurrentMap<String, RpcSession> logSessions;

	/**
	 * base name to command session mappings
	 */
	private ConcurrentMap<String, RpcSession> commandSessions;

	/**
	 * method name to command handler mappings
	 */
	private ConcurrentMap<String, CommandHandler> commandHandlers;

	public SentryImpl() {
		logSessions = new ConcurrentHashMap<String, RpcSession>();
		commandSessions = new ConcurrentHashMap<String, RpcSession>();

		commandHandlers = new ConcurrentHashMap<String, CommandHandler>();
	}

	@Validate
	public void start() {
		loggerFactoryRegistry.addListener(this);
		loggerRegistry.addListener(this);
	}

	@Invalidate
	public void stop() {
		if (loggerRegistry != null)
			loggerRegistry.removeListener(this);

		if (loggerFactoryRegistry != null)
			loggerFactoryRegistry.removeListener(this);

		for (RpcSession session : commandSessions.values()) {
			session.getConnection().close();
		}

		for (RpcSession session : logSessions.values()) {
			session.getConnection().close();
		}

		commandSessions.clear();
		logSessions.clear();
	}

	@Override
	public String getGuid() {
		Preferences prefs = getPreferences();
		return prefs.get("guid", null);
	}

	@Override
	public void setGuid(String guid) {
		try {
			Preferences prefs = getPreferences();
			prefs.put("guid", guid);
			prefs.flush();
			prefs.sync();
		} catch (BackingStoreException e) {
			slog.error("kraken-sentry: failed to save guid");
		}
	}

	@Override
	public String getBaseName(RpcConnection connection) {
		for (Map.Entry<String, RpcSession> pair : commandSessions.entrySet())
			if (pair.getValue().getConnection() == connection)
				return pair.getKey();

		return null;
	}

	@Override
	public RpcSession getCommandSession(String baseName) {
		return commandSessions.get(baseName);
	}

	@Override
	public Collection<String> getCommandSessionNames() {
		return Collections.unmodifiableCollection(commandSessions.keySet());
	}

	@Override
	public void addCommandSession(final String name, RpcSession commandSession) {
		if (name == null)
			throw new IllegalArgumentException("name must be not null");

		if (commandSession == null)
			throw new IllegalArgumentException("connection must be not null");

		RpcSession old = commandSessions.putIfAbsent(name, commandSession);
		if (old != null)
			throw new IllegalStateException("duplicated base connection name: " + name);

		commandSession.getConnection().addListener(new RpcConnectionEventListener() {
			@Override
			public void connectionOpened(RpcConnection connection) {
			}

			@Override
			public void connectionClosed(RpcConnection connection) {
				commandSessions.remove(name);
			}
		});
	}

	@Override
	public RpcSession removeCommandSession(String name) {
		return commandSessions.remove(name);
	}

	@Override
	public RpcSession getLogSession(String baseName) {
		return logSessions.get(baseName);
	}

	@Override
	public RpcSession connectLogChannel(final String baseName, String nonce) throws IOException {
		Base base = getBase(baseName);
		if (base == null)
			throw new IllegalStateException("base config not found: " + baseName);

		if (logSessions.containsKey(baseName))
			return logSessions.get(baseName);

		RpcConnection dataConnection = null;
		try {
			InetSocketAddress address = base.getAddress();
			KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory("rpc-agent", "SunX509");
			TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory("rpc-ca", "SunX509");

			RpcConnectionProperties props = new RpcConnectionProperties(address, kmf, tmf);

			dataConnection = agent.connectSsl(props);
			dataConnection.addListener(new RpcConnectionEventListener() {
				@Override
				public void connectionClosed(RpcConnection connection) {
					RpcSession session = commandSessions.get(baseName);
					if (session != null && session.getConnection().isOpen())
						session.getConnection().close();
					logSessions.remove(baseName);
				}

				@Override
				public void connectionOpened(RpcConnection connection) {
				}
			});

			RpcSession logSession = dataConnection.createSession("kraken-base");
			logSession.call("setLogChannel", new Object[] { getGuid(), nonce });

			RpcSession old = logSessions.putIfAbsent(baseName, logSession);
			if (old != null) {
				if (dataConnection != null)
					dataConnection.close();
				return null;
			}
			return logSession;
		} catch (Exception e) {
			getCommandSession(baseName).getConnection().close();
			if (dataConnection != null)
				dataConnection.close();
			slog.error("kraken-sentry: failed to open log channel", e);
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void disconnectLogChannel(String baseName) {
		RpcSession logSession = logSessions.remove(baseName);
		RpcConnection conn = logSession.getConnection();
		if (conn != null)
			conn.close();
	}

	@Override
	public Collection<Base> getBases() {
		List<Base> l = new ArrayList<Base>();

		try {
			Preferences root = getPreferences().node(BASE_PATH);
			for (String name : root.childrenNames()) {
				Preferences node = root.node(name);

				InetAddress ip = InetAddress.getByName(node.get("ip", null));
				int port = node.getInt("port", 0);
				String keyAlias = node.get("key", null);
				String trustAlias = node.get("ca", null);

				InetSocketAddress address = new InetSocketAddress(ip, port);
				Base base = new BaseImpl(name, address, keyAlias, trustAlias);

				l.add(base);
			}
		} catch (BackingStoreException e) {
			slog.warn("kraken-sentry: get base list failed", e);
		} catch (UnknownHostException e) {

		}
		return l;
	}

	@Override
	public Base getBase(String baseName) {
		try {
			Preferences root = getPreferences().node(BASE_PATH);
			if (!root.nodeExists(baseName))
				return null;

			Preferences p = root.node(baseName);
			InetAddress ip = InetAddress.getByName(p.get("ip", null));
			int port = p.getInt("port", 7140);
			String keyAlias = p.get("key", null);
			String trustAlias = p.get("ca", null);

			return new BaseImpl(baseName, new InetSocketAddress(ip, port), keyAlias, trustAlias);
		} catch (BackingStoreException e) {
			slog.warn("kraken-sentry: remove base failed", e);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void addBase(Base base) {
		try {
			Preferences root = getPreferences().node(BASE_PATH);
			if (root.nodeExists(base.getName()))
				throw new IllegalStateException("duplicated base name: " + base.getName());

			Preferences node = root.node(base.getName());
			InetSocketAddress address = base.getAddress();

			node.put("ip", address.getAddress().getHostAddress());
			node.putInt("port", address.getPort());
			node.put("key", base.getKeyAlias());
			node.put("ca", base.getTrustAlias());

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			slog.warn("kraken-sentry: add base failed", e);
		}
	}

	@Override
	public void removeBase(String name) {
		try {
			Preferences root = getPreferences().node(BASE_PATH);
			if (!root.nodeExists(name))
				throw new IllegalStateException("base not found: " + name);

			root.node(name).removeNode();
			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			slog.warn("kraken-sentry: remove base failed", e);
		}
	}

	@Override
	public void addCommandHandler(String name, CommandHandler handler) {
		if (name == null)
			throw new IllegalArgumentException("name must be not null");

		if (handler == null)
			throw new IllegalArgumentException("handler must be not null");

		CommandHandler old = commandHandlers.putIfAbsent(name, handler);
		if (old != null)
			throw new IllegalStateException("kraken-sentry: duplicated command handler name => " + name);
	}

	@Override
	public void removeCommandHandler(String name) {
		if (name == null)
			throw new IllegalArgumentException("name must be not null");

		commandHandlers.remove(name);
	}

	private Preferences getPreferences() {
		return prefsvc.getSystemPreferences();
	}

	//
	// LoggerFactoryRegistryEventListener callbacks
	//

	@Override
	public void factoryAdded(LoggerFactory factory) {
		for (String baseName : commandSessions.keySet()) {
			try {
				RpcSession session = commandSessions.get(baseName);
				session.post("factoryAdded", new Object[] { getGuid(), LoggerFactorySerializer.toMap(factory) });
			} catch (Exception e) {
				slog.error("kraken sentry: cannot post factory [{}] added event", factory.getFullName());
			}
		}
	}

	@Override
	public void factoryRemoved(LoggerFactory factory) {
		for (String baseName : commandSessions.keySet()) {
			try {
				RpcSession session = commandSessions.get(baseName);
				session.post("factoryRemoved", new Object[] { getGuid(), LoggerFactorySerializer.toMap(factory) });
			} catch (Exception e) {
				slog.error("kraken sentry: cannot post factory [{}] removed event", factory.getFullName());
			}
		}
	}

	//
	// LoggerRegistryEventListener callbacks
	//

	@Override
	public void loggerAdded(Logger logger) {
		for (String baseName : commandSessions.keySet()) {
			try {
				RpcSession session = commandSessions.get(baseName);
				session.post("loggerAdded", new Object[] { getGuid(), LoggerFactorySerializer.toMap(logger) });
				slog.info("kraken sentry: notify logger {}, {}", baseName, logger.getFullName());
			} catch (Exception e) {
				slog.error("kraken sentry: cannot post logger [{}] added event", logger.getFullName());
			}
		}
	}

	@Override
	public void loggerRemoved(Logger logger) {
		for (String baseName : commandSessions.keySet()) {
			try {
				RpcSession session = commandSessions.get(baseName);
				session.post("loggerRemoved", new Object[] { getGuid(), logger.getName() });
			} catch (Exception e) {
				slog.error("kraken sentry: cannot post logger [{}] removed event", logger.getFullName());
			}
		}
	}

}
