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
package org.krakenapps.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.krakenapps.base.RemoteLogger;
import org.krakenapps.base.RemoteLoggerFactory;
import org.krakenapps.base.RemoteLoggerFactoryInfo;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyRegistry;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.rpc.RpcAsyncCallback;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class SentryProxyImpl implements SentryProxy {
	private final org.slf4j.Logger slogger = org.slf4j.LoggerFactory.getLogger(SentryProxyImpl.class.getName());

	private boolean isClosed = false;
	private BundleContext bc;
	private String guid;
	private RpcSession commandSession;
	private String nonce;
	private RpcSession logSession;

	private ConcurrentMap<String, Logger> connectedLoggers;
	private LoggerRegistry loggerRegistry;
	private SentryProxyRegistry sentryProxyRegistry;

	/**
	 * remote factory full name to factory registration mappings
	 */
	private ConcurrentMap<String, ServiceRegistration> factoryMap;

	private ConcurrentMap<String, Logger> loggerMap;

	public SentryProxyImpl(BundleContext bc, String guid, RpcSession commandSession, LoggerRegistry loggerRegistry,
			SentryProxyRegistry sentryProxyRegistry) {
		this.bc = bc;
		this.guid = guid;
		this.commandSession = commandSession;
		this.connectedLoggers = new ConcurrentHashMap<String, Logger>();
		this.loggerRegistry = loggerRegistry;
		this.sentryProxyRegistry = sentryProxyRegistry;

		this.factoryMap = new ConcurrentHashMap<String, ServiceRegistration>();
		this.loggerMap = new ConcurrentHashMap<String, Logger>();
	}

	@Override
	public boolean isOpen() {
		return !isClosed;
	}

	@Override
	public String getGuid() {
		return guid;
	}

	@Override
	public Object call(String method, Object[] params) throws RpcException, InterruptedException {
		verify();
		return commandSession.call("run", new Object[] { method, params });
	}

	@Override
	public Object call(String method, Object[] params, long timeout) throws RpcException, InterruptedException {
		verify();
		return commandSession.call("run", new Object[] { method, params }, timeout);
	}

	@Override
	public void call(String method, Object[] params, RpcAsyncCallback callback) {
		verify();
		commandSession.call("run", new Object[] { method, params }, callback);
	}

	@Override
	public void post(String method, Object[] params) {
		verify();
		commandSession.post("run", new Object[] { method, params });
	}

	@Override
	public void requestLogChannel() {
		verify();

		if (nonce != null)
			throw new IllegalStateException("already requested log channel");

		if (logSession != null)
			throw new IllegalStateException("log session already exists");

		nonce = UUID.randomUUID().toString();
		post("connectLogChannel", new Object[] { nonce });
	}

	@Override
	public RpcSession getLogSession() {
		verify();
		return logSession;
	}

	@Override
	public void setLogSession(String nonce, RpcSession logSession) {
		verify();
		if (!this.nonce.equals(nonce))
			throw new IllegalArgumentException("nonce does not match");

		this.logSession = logSession;
	}

	@Override
	public Map<String, RemoteLoggerFactoryInfo> getRemoteLoggerFactories() throws RpcException, InterruptedException {
		Object[] factories = (Object[]) call("getLoggerFactories", null);
		return LoggerFactoryResponseParser.parseFactories(factories);
	}

	@SuppressWarnings("unchecked")
	@Override
	public RemoteLoggerFactoryInfo getRemoteLoggerFactory(String name) throws RpcException, InterruptedException {
		Object factory = call("getLoggerFactory", new Object[] { name });
		return LoggerFactoryResponseParser.parseFactory((Map<String, Object>) factory);
	}

	@Override
	public Map<String, Logger> getRemoteLoggers() throws RpcException, InterruptedException {
		Object[] loggers = (Object[]) call("getLoggers", null);
		return LoggerResponseParser.parse(this, loggers);
	}

	@Override
	public Logger createRemoteLogger(String factoryName, String name, String description, Properties props)
			throws RpcException, InterruptedException {
		Map<String, Object> m = new HashMap<String, Object>();
		for (Object key : props.keySet()) {
			m.put(key.toString(), props.get(key));
		}

		call("createLogger", new Object[] { factoryName, name, description, m });
		return new RemoteLogger(this, name, factoryName, description, new Properties());
	}

	@Override
	public void removeRemoteLogger(String name) throws RpcException, InterruptedException {
		call("removeLogger", new Object[] { name });
	}

	@Override
	public void startRemoteLogger(String name, int interval) throws RpcException, InterruptedException {
		call("startLogger", new Object[] { name, interval });
	}

	@Override
	public void stopRemoteLogger(String name, int timeout) throws RpcException, InterruptedException {
		call("stopLogger", new Object[] { name, timeout });
	}

	@Override
	public void connectRemoteLogger(String loggerName) throws RpcException, InterruptedException {
		if (loggerName == null)
			throw new IllegalArgumentException("logger name must not be null");

		if (connectedLoggers.containsKey(loggerName))
			throw new IllegalStateException("already connected logger");

		Logger remoteLogger = loggerRegistry.getLogger(guid + "\\" + loggerName);
		Logger old = connectedLoggers.putIfAbsent(loggerName, remoteLogger);
		if (old != null)
			throw new IllegalStateException("duplicated logger connection: " + loggerName);

		call("connectLogger", new Object[] { loggerName });
	}

	@Override
	public void disconnectRemoteLogger(String loggerName) throws RpcException, InterruptedException {
		if (loggerName == null)
			throw new IllegalArgumentException("logger name must not be null");

		Logger logger = loggerRegistry.getLogger(guid + "\\" + loggerName);
		if (logger == null)
			throw new IllegalStateException("logger not found: " + loggerName);

		call("disconnectLogger", new Object[] { loggerName });
	}

	@Override
	public Logger getConnectedLogger(String name) {
		if (name == null)
			throw new IllegalArgumentException("connected logger name must not be null");

		return connectedLoggers.get(name);
	}

	@Override
	public Collection<Logger> getConnectedLoggers() {
		return new ArrayList<Logger>(connectedLoggers.values());
	}

	@Override
	public void syncLoggerFactories() throws RpcException, InterruptedException {
		Map<String, RemoteLoggerFactoryInfo> map = getRemoteLoggerFactories();

		unregisterAllRemoteFactories();

		for (String name : map.keySet()) {
			RemoteLoggerFactoryInfo info = map.get(name);
			registerLoggerFactory(info);
		}
	}

	@Override
	public void syncLoggers() throws RpcException, InterruptedException {
		Map<String, Logger> loggers = getRemoteLoggers();

		unregisterAllRemoteLoggers();

		for (String name : loggers.keySet()) {
			Logger logger = loggers.get(name);
			loggerRegistry.addLogger(logger);
		}
	}

	private void unregisterAllRemoteLoggers() {
		for (String loggerName : new ArrayList<String>(this.loggerMap.keySet())) {
			Logger logger = loggerMap.get(loggerName);
			try {
				unregisterLogger(logger.getName());
			} catch (Exception e) {
				// all remote logger should be removed
			}
		}
	}

	private void unregisterAllRemoteFactories() {
		for (String factoryName : new ArrayList<String>(this.factoryMap.keySet())) {
			try {
				ServiceRegistration registration = this.factoryMap.get(factoryName);
				RemoteLoggerFactory factory = (RemoteLoggerFactory) bc.getService(registration.getReference());
				unregisterLoggerFactory(factory.getFullName());
			} catch (Exception e) {
				// all remote logger factories should be removed
			}
		}
	}

	@Override
	public void registerLoggerFactory(RemoteLoggerFactoryInfo factory) {
		RemoteLoggerFactory remoteFactory = new RemoteLoggerFactory(this, factory);
		String factoryFullName = guid + "\\" + factory.getName();

		if (factoryMap.containsKey(factoryFullName)) {
			slogger.error("kraken base: factory [{}] already added", factoryFullName);
			return;
		}

		try {
			ServiceRegistration registration = bc.registerService(LoggerFactory.class.getName(), remoteFactory,
					new Hashtable<String, String>());
			ServiceRegistration old = factoryMap.putIfAbsent(factoryFullName, registration);
			if (old != null)
				registration.unregister();

			slogger.info("kraken base: factory [{}] added", factoryFullName);
		} catch (Exception e) {
			slogger.error("kraken base: bundle context is not valid", e);
		}
	}

	@Override
	public void unregisterLoggerFactory(String factoryFullName) {
		ServiceRegistration old = factoryMap.remove(factoryFullName);
		if (old == null) {
			slogger.error("kraken base: factory [{}] not found", factoryFullName);
			return;
		}

		old.unregister();

		slogger.info("kraken base: factory [{}] removed", factoryFullName);

	}

	@Override
	public void registerLogger(Logger logger) {
		loggerRegistry.addLogger(logger);
		loggerMap.put(logger.getName(), logger);
	}

	@Override
	public void unregisterLogger(String loggerName) {
		if (loggerName == null)
			throw new IllegalArgumentException("logger must not be null");

		connectedLoggers.remove(loggerName);

		Logger logger = loggerRegistry.getLogger(guid + "\\" + loggerName);
		try {
			loggerRegistry.removeLogger(logger);
		} catch (Exception e) {
			// if connection is reset, removeLogger can throw 'logger is still
			// running' exception. However it should be ignored.
		}
		loggerMap.remove(loggerName);
	}

	@Override
	public void loggerStarted(String loggerFullName, int interval) {
		RemoteLogger logger = (RemoteLogger) loggerRegistry.getLogger(loggerFullName);
		if (logger == null)
			return;

		logger.setRunning(true);
		logger.setInterval(interval);
	}

	@Override
	public void loggerStopped(String loggerFullName) {
		RemoteLogger logger = (RemoteLogger) loggerRegistry.getLogger(loggerFullName);
		if (logger == null)
			return;

		logger.setRunning(false);
	}

	@Override
	public void close() {
		if (isClosed)
			return;

		isClosed = true;

		if (sentryProxyRegistry != null)
			sentryProxyRegistry.unregister(this);

		// unregister all remote logger and factories
		unregisterAllRemoteLoggers();
		unregisterAllRemoteFactories();

		// close all sessions
		if (commandSession != null) {
			commandSession.getConnection().close();
			commandSession = null;
		}

		if (logSession != null) {
			logSession.getConnection().close();
			logSession = null;
		}

		connectedLoggers.clear();
	}

	private void verify() {
		if (isClosed)
			throw new IllegalStateException("sentry [" + guid + "] is closed");
	}

	@Override
	public String toString() {
		RpcConnection connection = commandSession.getConnection();
		return String.format("guid=%s, remote=%s", guid, connection.getRemoteAddress());
	}
}