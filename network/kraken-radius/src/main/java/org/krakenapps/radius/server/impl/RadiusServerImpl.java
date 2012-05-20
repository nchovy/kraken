/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.radius.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.radius.server.RadiusFactory;
import org.krakenapps.radius.server.RadiusFactoryEventListener;
import org.krakenapps.radius.server.RadiusInstance;
import org.krakenapps.radius.server.RadiusModule;
import org.krakenapps.radius.server.RadiusModuleType;
import org.krakenapps.radius.server.RadiusPortType;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusServer;
import org.krakenapps.radius.server.RadiusServerEventListener;
import org.krakenapps.radius.server.RadiusVirtualServer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "radius-server")
@Provides(specifications = { RadiusServer.class })
public class RadiusServerImpl implements RadiusServer, RadiusFactoryEventListener {
	public static final int DEFAULT_AUTH_PORT = 1812;
	private final Logger logger = LoggerFactory.getLogger(RadiusServerImpl.class.getName());

	@Requires
	private ConfigService cfg;

	private BundleContext bc;

	private Map<String, RadiusVirtualServer> virtualServers;
	private Map<String, RadiusProfile> profiles;
	private Map<RadiusModuleType, RadiusModule> modules;

	private CopyOnWriteArraySet<RadiusServerEventListener> callbacks;
	private ExecutorService executor;

	public RadiusServerImpl(BundleContext bc) {
		this.bc = bc;
	}

	@Validate
	public void start() {
		this.callbacks = new CopyOnWriteArraySet<RadiusServerEventListener>();
		this.virtualServers = new ConcurrentHashMap<String, RadiusVirtualServer>();
		this.profiles = new ConcurrentHashMap<String, RadiusProfile>();
		this.modules = new ConcurrentHashMap<RadiusModuleType, RadiusModule>();
		this.executor = Executors.newCachedThreadPool();

		loadProfiles();
		loadModules();
		loadVirtualServers();
	}

	private void loadVirtualServers() {
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		ConfigIterator it = db.findAll(RadiusVirtualServerConfig.class);
		for (RadiusVirtualServerConfig config : it.getDocuments(RadiusVirtualServerConfig.class)) {
			try {
				if (config.getPortType() == null) {
					logger.warn("kraken radius: virtual server [{}]'s port type is null", config.getName());
					continue;
				}

				RadiusPortType portType = RadiusPortType.parse(config.getPortType());
				Integer port = determinePort(config.getPort(), portType);

				InetSocketAddress bindAddress = null;
				if (config.getHostName() != null)
					bindAddress = new InetSocketAddress(config.getHostName(), port);
				else
					bindAddress = new InetSocketAddress(port);

				RadiusVirtualServerImpl vs = new RadiusVirtualServerImpl(this, config.getName(), config.getProfile(),
						portType, executor, bindAddress);
				vs.open();

				virtualServers.put(config.getName(), vs);
			} catch (IOException e) {
				logger.error("kraken radius: cannot load virtual server - " + config.getName(), e);
			}
		}
	}

	private void loadModules() {
		for (RadiusModuleType t : RadiusModuleType.values()) {
			RadiusModule module = new RadiusModule(bc, this, t, cfg);
			modules.put(t, module);
			loadModuleInstances(t);
			module.start();
		}
	}

	private void loadModuleInstances(RadiusModuleType t) {
		RadiusModule module = modules.get(t);

		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		ConfigCollection col = db.ensureCollection("instances");

		for (Object o : col.findAll().getDocuments()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> m = (Map<String, Object>) o;
			String name = (String) m.get("name");
			module.loadInstance(name);
		}
	}

	private void unloadModules() {
		for (RadiusModuleType t : RadiusModuleType.values()) {
			RadiusModule module = modules.get(t);
			if (module == null)
				continue;

			try {
				module.stop();
			} catch (Exception e) {
				logger.warn("kraken radius: module callback should not throw any exception", e);
			}
		}

		modules.clear();
	}

	private void loadProfiles() {
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		for (RadiusProfile profile : db.findAll(RadiusProfile.class).getDocuments(RadiusProfile.class)) {
			profiles.put(profile.getName(), profile);
		}
	}

	private Integer determinePort(Integer port, RadiusPortType portType) {
		if (port == null) {
			if (portType == RadiusPortType.Authentication)
				port = RadiusVirtualServerImpl.DEFAULT_AUTH_PORT;
			else if (portType == RadiusPortType.Accounting)
				port = RadiusVirtualServerImpl.DEFAULT_ACCT_PORT;
		}
		return port;
	}

	@Invalidate
	public void stop() {
		stopVirtualServers();
		unloadModules();
	}

	private void stopVirtualServers() {
		for (RadiusVirtualServer vs : virtualServers.values()) {
			try {
				logger.info("kraken radius: stopping virtual server [{}] - {}", vs.getName(), vs.getBindAddress());
				vs.close();
			} catch (IOException e) {
				logger.error("kraken radius: cannot stop virtual server " + vs.getName(), e);
			}
		}

		virtualServers.clear();
	}

	@Override
	public List<RadiusVirtualServer> getVirtualServers() {
		return new ArrayList<RadiusVirtualServer>(virtualServers.values());
	}

	@Override
	public RadiusVirtualServer getVirtualServer(String name) {
		return virtualServers.get(name);
	}

	@Override
	public RadiusVirtualServer createVirtualServer(String name, RadiusPortType portType, String profileName) {
		return createVirtualServer(name, portType, profileName, null);
	}

	@Override
	public RadiusVirtualServer createVirtualServer(String name, RadiusPortType portType, String profileName,
			InetSocketAddress bindAddress) {
		verifyNotNull("name", name);
		verifyNotNull("port type", portType);
		verifyNotNull("profile name", profileName);

		if (!profiles.containsKey(profileName))
			throw new IllegalArgumentException("profile not found: " + profileName);

		if (virtualServers.containsKey(name))
			throw new IllegalStateException("duplicated virtual server name: " + name);

		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		RadiusVirtualServerConfig config = new RadiusVirtualServerConfig();
		config.setName(name);
		config.setProfile(profileName);
		config.setPortType(portType.getAlias());

		if (bindAddress != null) {
			config.setPort(bindAddress.getPort());
			config.setHostName(bindAddress.getAddress().getHostAddress());
		} else {
			config.setPort(DEFAULT_AUTH_PORT);
		}

		db.add(config);

		RadiusVirtualServerImpl vs = new RadiusVirtualServerImpl(this, name, profileName, portType, executor,
				bindAddress);

		virtualServers.put(vs.getName(), vs);
		return vs;
	}

	@Override
	public void removeVirtualServer(String name) {
		verifyNotNull("name", name);

		RadiusVirtualServer vs = virtualServers.remove(name);
		if (vs == null)
			return;

		try {
			vs.close();
		} catch (IOException e) {
			logger.error("kraken radius: cannot close virtual server " + name, e);
		}
	}

	@Override
	public List<RadiusProfile> getProfiles() {
		return new ArrayList<RadiusProfile>(profiles.values());
	}

	@Override
	public RadiusProfile getProfile(String name) {
		verifyNotNull("name", name);
		return profiles.get(name);
	}

	@Override
	public void createProfile(RadiusProfile profile) {
		verifyNotNull("profile", profile);
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		Config c = db.findOne(RadiusProfile.class, Predicates.field("name", profile.getName()));
		if (c != null)
			throw new IllegalStateException("duplicated profile name: " + profile.getName());

		db.add(profile);
		profiles.put(profile.getName(), profile);
	}

	@Override
	public void updateProfile(RadiusProfile profile) {
		verifyNotNull("profile", profile);
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		Config c = db.findOne(RadiusProfile.class, Predicates.field("name", profile.getName()));
		if (c == null)
			throw new IllegalStateException("profile not found: " + profile.getName());

		db.update(c, profile);
		profiles.put(profile.getName(), profile);
	}

	@Override
	public void removeProfile(String name) {
		verifyNotNull("name", name);
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		Config c = db.findOne(RadiusProfile.class, Predicates.field("name", name));
		if (c == null)
			throw new IllegalStateException("profile not found: " + name);

		db.remove(c);
		profiles.remove(name);
	}

	@Override
	public List<RadiusModule> getModules() {
		return new ArrayList<RadiusModule>(modules.values());
	}

	@Override
	public RadiusModule getModule(RadiusModuleType type) {
		return modules.get(type);
	}

	@Override
	public RadiusInstance getModuleInstance(RadiusModuleType type, String instanceName) {
		RadiusModule module = getModule(type);
		if (module == null)
			return null;

		return module.getInstance(instanceName);
	}

	@Override
	public RadiusInstance createModuleInstance(RadiusModuleType type, String instanceName, String factoryName,
			Map<String, Object> configs) {
		RadiusModule module = getModule(type);
		if (module == null)
			throw new IllegalStateException("module not found: " + type);

		RadiusFactory<?> factory = module.getFactory(factoryName);
		if (factory == null)
			throw new IllegalStateException("factory not found: " + factoryName);

		return module.createInstance(instanceName, factoryName, configs);
	}

	@Override
	public void removeModuleInstance(RadiusModuleType type, String instanceName) {
		RadiusModule module = getModule(type);
		if (module == null)
			throw new IllegalStateException("module not found: " + type);

		module.removeInstance(instanceName);
	}

	@Override
	public void addEventListener(RadiusServerEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeEventListener(RadiusServerEventListener listener) {
		callbacks.remove(listener);
	}

	@Override
	public void addingService(Object service) {
		for (RadiusModuleType t : RadiusModuleType.values()) {
			if (t.getFactoryClass().isAssignableFrom(service.getClass())) {
				RadiusModule module = getModule(t);
				RadiusFactory<?> factory = (RadiusFactory<?>) service;
				module.addFactory(factory);

				loadModuleInstances(t);

				return;
			}
		}
	}

	@Override
	public void removedService(Object service) {
		for (RadiusModuleType t : RadiusModuleType.values()) {
			if (t.getFactoryClass().isAssignableFrom(service.getClass())) {
				RadiusFactory<?> factory = (RadiusFactory<?>) service;
				RadiusModule module = getModule(t);
				module.removeFactory(factory.getName());
				return;
			}
		}
	}

	private void verifyNotNull(String name, Object value) {
		if (value == null)
			throw new IllegalArgumentException(name + " should be not null");
	}
}
