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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.krakenapps.base.RemoteLogger;
import org.krakenapps.base.RemoteLoggerFactoryInfo;
import org.krakenapps.base.SentryProxy;
import org.krakenapps.base.SentryProxyRegistry;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcContext;
import org.krakenapps.rpc.RpcException;
import org.krakenapps.rpc.RpcMethod;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.rpc.RpcSessionEventCallback;
import org.krakenapps.rpc.SimpleRpcService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "base-rpc-service")
@Provides
public class BaseRpcService extends SimpleRpcService {
	private Logger slogger = LoggerFactory.getLogger(BaseRpcService.class.getName());

	private BundleContext bc;

	@SuppressWarnings("unused")
	@ServiceProperty(name = "rpc.name", value = "kraken-base")
	private String rpcName;

	@Requires
	private SentryProxyRegistry sentryRegistry;

	@Requires
	private LoggerRegistry loggerRegistry;

	private ConcurrentMap<RpcSession, SentryProxy> commandSessions = new ConcurrentHashMap<RpcSession, SentryProxy>();

	public BaseRpcService(BundleContext bc) {
		this.bc = bc;
	}

	@RpcMethod(name = "hello")
	public void hello(String guid) {
		RpcConnection connection = RpcContext.getSession().getConnection();
		// validateGuid(guid, connection);

		// create reverse connection
		RpcSession sentryCommandSession;
		try {
			sentryCommandSession = connection.createSession("kraken-sentry");
		} catch (Exception e) {
			slogger.info("kraken-base: hello failed", e);
			throw new RpcException("kraken-base: cannot open [" + guid + "] sentry rpc service");
		}

		// register sentry proxy
		SentryProxy sentry = new SentryProxyImpl(bc, guid, sentryCommandSession, loggerRegistry, sentryRegistry);
		sentryRegistry.register(sentry);
		commandSessions.putIfAbsent(sentryCommandSession, sentry);

		sentryCommandSession.addListener(new RpcSessionEventCallback() {
			@Override
			public void sessionClosed(RpcSession session) {
				SentryProxy sentry = commandSessions.remove(session);
				if (sentry == null)
					return;

				// unregister sentry
				slogger.info("kraken-sentry: command session [{}] closed", sentry.getGuid());
				sentry.close();
			}
		});
		slogger.info("kraken-base: sentry {} connected, session {}", guid, sentryCommandSession);

		// grab all logger factories
		try {
			sentry.syncLoggerFactories();
			sentry.syncLoggers();
		} catch (InterruptedException e1) {
			throw new RpcException("logger factory sync interrupted");
		}

		// make log channel
		sentry.requestLogChannel();
	}

	private void validateGuid(String guid, RpcConnection connection) {
		String cn = null;
		for (String token : connection.getPeerCertificate().getSubjectDN().getName().split(", ")) {
			if (token.indexOf("=") == -1)
				continue;
			if (token.split("=")[0].equals("CN"))
				cn = token.split("=")[1];
		}

		if (!guid.equals(cn)) {
			slogger.error("kraken base: connection [{}] certificate mismatch, CN [{}], GUID [{}]", new Object[] {
					connection.getRemoteAddress(), cn, guid });

			throw new IllegalStateException("subject does not match");
		}
	}

	@RpcMethod(name = "setLogChannel")
	public void setLogChannel(String guid, String nonce) {
		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null)
			throw new RpcException("sentry not found");

		RpcConnection logChannel = RpcContext.getConnection();
		RpcSession logSession = RpcContext.getSession();
		sentry.setLogSession(nonce, logSession);
		slogger.info("kraken-base: log channel [guid={}, remote={}] opened", guid, logChannel.getRemoteAddress());
	}

	@RpcMethod(name = "onLog")
	public void onLog(String guid, String loggerName, Map<String, Object> logData) {
		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null)
			throw new RpcException("sentry not found");

		try {
			RemoteLogger logger = (RemoteLogger) sentry.getConnectedLogger(loggerName);
			if (logger == null) {
				slogger.warn("kraken-base: connected logger [{}] not found, maybe previous disconnect was incomplete",
						loggerName);
				return;
			}

			Log log = parse(logger, logData);
			logger.onLog(log);

			if (slogger.isTraceEnabled())
				slogger.trace("kraken-base: log received, sentry [{}], logger [{}], date [{}], msg [{}]", new Object[] {
						guid, loggerName, log.getDate(), log.getMessage() });
		} catch (Exception e) {
			slogger.warn("kraken-base: onLog callback failed", e);
		}
	}

	@SuppressWarnings("unchecked")
	private Log parse(RemoteLogger logger, Map<String, Object> logData) {
		Date date = (Date) logData.get("date");
		String message = (String) logData.get("msg");
		Map<String, Object> params = (Map<String, Object>) logData.get("params");
		return new SimpleLog(date, logger.getFullName(), (String) logData.get("type"), message, params);
	}

	@Invalidate
	public void stop() {
		slogger.info("kraken-base: unregistering all remote logger and factories");
		if (sentryRegistry != null) {
			for (String guid : new ArrayList<String>(sentryRegistry.getSentryGuids())) {
				SentryProxy proxy = sentryRegistry.getSentry(guid);
				proxy.close();
			}
		}

		slogger.info("kraken-base: disconnecting all sentry command sessions");
		for (RpcSession session : commandSessions.keySet()) {
			session.getConnection().close();
		}

		commandSessions.clear();
	}

	@RpcMethod(name = "factoryAdded")
	public void factoryAdded(String guid, Map<String, Object> factory) {
		SentryProxy proxy = sentryRegistry.getSentry(guid);
		RemoteLoggerFactoryInfo info = LoggerFactoryResponseParser.parseFactory(factory);
		proxy.registerLoggerFactory(info);
	}

	@RpcMethod(name = "factoryRemoved")
	public void factoryRemoved(String guid, Map<String, Object> factory) {
		RemoteLoggerFactoryInfo info = LoggerFactoryResponseParser.parseFactory(factory);
		SentryProxy proxy = sentryRegistry.getSentry(guid);
		String factoryFullName = guid + "\\" + info.getName();
		proxy.unregisterLoggerFactory(factoryFullName);
	}

	@RpcMethod(name = "loggerAdded")
	public void loggerAdded(String guid, Map<String, Object> logger) {
		try {
			SentryProxy sentry = sentryRegistry.getSentry(guid);
			if (sentry == null)
				throw new RpcException("sentry not found");

			sentry.registerLogger(LoggerResponseParser.parse(sentry, logger));
		} catch (Exception e) {
			slogger.error("kraken base: cannot add logger", e);
		}
	}

	@RpcMethod(name = "loggerRemoved")
	public void loggerRemoved(String guid, String loggerName) {
		try {
			SentryProxy sentry = sentryRegistry.getSentry(guid);
			if (sentry == null)
				throw new RpcException("sentry not found");

			sentry.unregisterLogger(loggerName);
		} catch (Exception e) {
			slogger.error("kraken base: cannot remove logger", e);
		}
	}

	@RpcMethod(name = "loggerStarted")
	public void loggerStarted(String guid, String loggerName, Integer interval) {
		slogger.info("kraken base: logger [{}] started at sentry [{}], interval [{}]", new Object[] { loggerName, guid,
				interval });

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null)
			throw new RpcException("sentry not found");

		String loggerFullName = guid + "\\" + loggerName;
		sentry.loggerStarted(loggerFullName, interval);
	}

	@RpcMethod(name = "loggerStopped")
	public void loggerStopped(String guid, String loggerName) {
		slogger.info("kraken base: logger [{}] stopped at sentry [{}]", loggerName, guid);

		SentryProxy sentry = sentryRegistry.getSentry(guid);
		if (sentry == null)
			throw new RpcException("sentry not found");

		String loggerFullName = guid + "\\" + loggerName;
		sentry.loggerStopped(loggerFullName);
	}
}
