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
package org.krakenapps.syslog.impl;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.syslog.Syslog;
import org.krakenapps.syslog.SyslogListener;
import org.krakenapps.syslog.SyslogProfile;
import org.krakenapps.syslog.SyslogServer;
import org.krakenapps.syslog.SyslogServerRegistry;
import org.krakenapps.syslog.SyslogServerRegistryEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "syslog-server-registry")
@Provides(specifications = { SyslogServerRegistry.class })
public class SyslogServerRegistryImpl implements SyslogServerRegistry, SyslogListener {
	private final Logger logger = LoggerFactory.getLogger(SyslogServerRegistryImpl.class.getName());
	private ConcurrentMap<String, SyslogServer> serverMap;
	private Set<SyslogListener> syslogCallbacks;
	private Set<SyslogServerRegistryEventListener> eventCallbacks;

	@Requires
	private ConfigService conf;

	public SyslogServerRegistryImpl() {
		serverMap = new ConcurrentHashMap<String, SyslogServer>();
		syslogCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<SyslogListener, Boolean>());
		eventCallbacks = Collections.newSetFromMap(new ConcurrentHashMap<SyslogServerRegistryEventListener, Boolean>());
	}

	/**
	 * load all persistent syslog servers
	 */
	@Validate
	public void start() {
		for (SyslogProfile p : getSyslogProfiles()) {
			try {
				SyslogReceiver server = new SyslogReceiver(p);
				server.open();
				server.addListener(this);
				serverMap.put(p.getName(), server);
			} catch (Throwable t) {
				logger.error("kraken syslog: cannot open syslog server", t);
			}
		}
	}

	/**
	 * close all syslog servers
	 */
	@Invalidate
	public void stop() {
		for (SyslogServer server : serverMap.values()) {
			try {
				server.removeListener(this);
				server.close();
			} catch (Throwable t) {
				logger.error("kraken syslog: cannot close - " + server, t);
			}
		}
	}

	@Override
	public boolean contains(String name) {
		return serverMap.containsKey(name);
	}

	@Override
	public Collection<String> getNames() {
		return new ArrayList<String>(serverMap.keySet());
	}

	@Override
	public SyslogServer getServer(String name) {
		return serverMap.get(name);
	}

	@Override
	public SyslogServer findServer(InetSocketAddress local) {
		if (local == null)
			return null;

		for (String name : serverMap.keySet()) {
			SyslogServer server = serverMap.get(name);
			if (server.getListenAddress().equals(local))
				return server;
		}

		return null;
	}

	@Override
	public void register(String name, SyslogServer server) {
		SyslogServer old = serverMap.putIfAbsent(name, server);
		if (old != null) {
			logger.warn("kraken syslog: duplicated server name [{}]", name);
			return;
		}

		logger.info("kraken syslog: [{}, addr={}] syslog server registered", name, server.getListenAddress());

		// add callback
		server.addListener(this);

		for (SyslogServerRegistryEventListener callback : eventCallbacks) {
			try {
				callback.syslogServerAdded(name, server);
			} catch (Exception e) {
				logger.warn("kraken syslog: registry event callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void unregister(String name) {
		SyslogServer server = serverMap.remove(name);

		logger.info("kraken syslog: [{}, addr={}] syslog server unregistered", name, server.getListenAddress());

		// remove callback
		server.removeListener(this);

		for (SyslogServerRegistryEventListener callback : eventCallbacks) {
			try {
				callback.syslogServerRemoved(name, server);
			} catch (Exception e) {
				logger.warn("kraken syslog: registry event callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addSyslogListener(SyslogListener callback) {
		if (callback == null) {
			return;
		}

		syslogCallbacks.add(callback);
	}

	@Override
	public void removeSyslogListener(SyslogListener callback) {
		if (callback == null) {
			return;
		}

		syslogCallbacks.remove(callback);
	}

	@Override
	public void addEventListener(SyslogServerRegistryEventListener callback) {
		if (callback == null)
			return;

		eventCallbacks.add(callback);
	}

	@Override
	public void removeEventListener(SyslogServerRegistryEventListener callback) {
		if (callback == null)
			return;

		eventCallbacks.remove(callback);
	}

	@Override
	public void onReceive(Syslog syslog) {
		for (SyslogListener callback : syslogCallbacks) {
			try {
				callback.onReceive(syslog);
			} catch (Exception e) {
				logger.warn("kraken syslog: syslog callback should not throw any exception", e);
			}
		}
	}

	@Override
	public Collection<SyslogProfile> getSyslogProfiles() {
		ConfigDatabase db = conf.ensureDatabase("kraken-syslog");
		return db.findAll(SyslogProfile.class).getDocuments(SyslogProfile.class);
	}

	@Override
	public void open(SyslogProfile profile) throws SocketException {
		for (SyslogProfile p : getSyslogProfiles()) {
			if (p.getName().equals(profile.getName()))
				throw new IllegalStateException("duplicated syslog server name");

			if (p.getListenAddress().equals(profile.getListenAddress()))
				throw new IllegalStateException("listen address conflict");
		}

		SyslogReceiver server = new SyslogReceiver(profile);
		server.open();

		// add callback
		server.addListener(this);

		serverMap.put(profile.getName(), server);

		ConfigDatabase db = conf.ensureDatabase("kraken-syslog");
		db.add(profile);
	}

	@Override
	public void close(String name) {
		SyslogServer server = serverMap.remove(name);
		if (server != null) {
			// remove callback
			server.removeListener(this);
			server.close();
		}

		ConfigDatabase db = conf.ensureDatabase("kraken-syslog");
		Config c = db.findOne(SyslogProfile.class, Predicates.field("name", name));
		if (c != null)
			db.remove(c);
	}
}
