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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.KeyStoreManager;
import org.krakenapps.rpc.RpcAgent;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;
import org.krakenapps.rpc.RpcSession;
import org.krakenapps.sentry.Base;
import org.krakenapps.sentry.ConnectionWatchdog;
import org.krakenapps.sentry.Sentry;
import org.krakenapps.sentry.SentryRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "sentry-connection-watchdog")
@Provides
public class ConnectionWatchdogImpl implements ConnectionWatchdog, Runnable {
	private final Logger logger = LoggerFactory.getLogger(ConnectionWatchdogImpl.class.getName());
	private volatile boolean stop = false;
	private volatile boolean stopped = false;
	private Thread t;

	@Requires
	private SentryRpcService sentryRpcService;

	@Requires
	private RpcAgent rpc;

	@Requires
	private Sentry sentry;

	@Requires
	private KeyStoreManager keyStoreManager;

	@Validate
	@Override
	public void start() {
		t = new Thread(this, "Sentry Connection Watchdog");
		t.start();
	}

	@Invalidate
	@Override
	public boolean stop() {
		stop = true;
		t.interrupt();

		// wait for thread stop
		for (int i = 0; i < 10; i++) {
			try {
				if (stopped)
					break;

				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}

		if (!stopped)
			logger.error("kraken-sentry: watchdog thread didn't stop in 5seconds, give up.");

		return stopped;
	}

	@Override
	public void run() {
		int i = 0;
		try {
			while (!stop) {
				// check every 10secs.
				if ((i % 20) == 0)
					checkNow();

				Thread.sleep(500);
				i++;
			}
		} catch (InterruptedException e) {
			logger.info("kraken-sentry: watchdog stopped");
		}

		// clear flags
		stop = false;
		stopped = true;
	}

	@Override
	public void checkNow() {
		try {
			logger.debug("kraken-sentry: checking sentry connections");
			if (sentry.getGuid() == null) {
				logger.trace("kraken-sentry: set guid first");
				return;
			}

			checkConnections();
		} catch (Exception e) {
			logger.error("kraken-sentry: watchdog check failed", e);
		}
	}

	private void checkConnections() {
		Collection<String> names = sentry.getCommandSessionNames();

		// live connection check
		for (Base base : sentry.getBases()) {
			String name = base.getName();
			if (names.contains(name)) {
				RpcSession commandSession = sentry.getCommandSession(name);
				RpcConnection conn = commandSession.getConnection();
				// if connection is closed, remove it and reconnect.
				if (!conn.isOpen()) {
					sentry.removeCommandSession(name);
					connect(sentry, base);
				}
			} else {
				try {
					connect(sentry, base);
				} catch (Exception e) {
					logger.warn("kraken-sentry: cannot connect to base [{}]", base.getName());
				}
			}
		}

		// dead connection check (just removed by admin)
		List<String> removeTargets = new ArrayList<String>();
		for (String name : names) {
			Base found = null;
			for (Base base : sentry.getBases()) {
				if (base.getName().equals(name)) {
					found = base;
					break;
				}
			}

			// it should be removed
			if (found == null) {
				removeTargets.add(name);
			}
		}

		for (String target : removeTargets) {
			RpcSession commandSession = sentry.removeCommandSession(target);
			if (commandSession != null) {
				commandSession.getConnection().close();
			}
		}
	}

	private void connect(Sentry sentry, Base base) {
		RpcConnection connection = null;
		try {
			InetSocketAddress endpoint = base.getAddress();
			KeyManagerFactory kmf = keyStoreManager.getKeyManagerFactory("rpc-agent", "SunX509");
			TrustManagerFactory tmf = keyStoreManager.getTrustManagerFactory("rpc-ca", "SunX509");

			RpcConnectionProperties props = new RpcConnectionProperties(endpoint, kmf, tmf);

			connection = rpc.connectSsl(props);
			if (connection == null) {
				logger.warn("kraken-sentry: connect failed to [{}]", base.getAddress());
				return;
			}

			// bind sentry service, and connect to base service.
			connection.bind("kraken-sentry", sentryRpcService);
			RpcSession commandSession = connection.createSession("kraken-base");
			sentry.addCommandSession(base.getName(), commandSession);

			commandSession.call("hello", new Object[] { sentry.getGuid() }, 5000);
			logger.info("kraken-sentry: new sentry connection [{}] to base [{}]", connection, base.getName());
		} catch (Exception e) {
			logger.info("kraken-sentry: failed to connect, closing connection", e);
			if (connection != null)
				connection.close();
		}
	}
}
