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
import org.krakenapps.radius.server.RadiusConfigMetadata;
import org.krakenapps.radius.server.RadiusConfigurator;
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
import org.krakenapps.radius.server.RadiusConfigMetadata.Type;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "radius-server")
@Provides(specifications = { RadiusServer.class })
public class RadiusServerImpl implements RadiusServer, RadiusFactoryEventListener {
	private static final String VIRTUAL_SERVER_ROOT_KEY = "virtual_servers";

	private final Logger logger = LoggerFactory.getLogger(RadiusServerImpl.class.getName());

	@Requires
	private PreferencesService prefsvc;

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

		try {
			loadProfiles();
			loadModules();
			loadVirtualServers();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadVirtualServers() throws BackingStoreException {
		Preferences root = prefsvc.getSystemPreferences().node(VIRTUAL_SERVER_ROOT_KEY);
		for (String name : root.childrenNames()) {
			try {
				RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, VIRTUAL_SERVER_ROOT_KEY, name,
						getVirtualServerConfigList());

				String hostname = conf.getString(RadiusVirtualServerImpl.HOSTNAME_KEY);
				RadiusPortType portType = RadiusPortType.parse(conf.getString(RadiusVirtualServerImpl.PORT_TYPE_KEY));
				if (portType == null) {
					System.out.println("### warn port type null");
					continue;
				}

				Integer port = determinePort(conf, portType);

				InetSocketAddress bindAddress = null;
				if (hostname != null)
					bindAddress = new InetSocketAddress(hostname, port);
				else
					bindAddress = new InetSocketAddress(port);

				RadiusVirtualServerImpl vs = new RadiusVirtualServerImpl(this, name, portType, conf, executor,
						bindAddress);
				vs.open();

				virtualServers.put(name, vs);
			} catch (IOException e) {
				logger.error("kraken radius: cannot load virtual server - " + name, e);
			}
		}
	}

	private List<RadiusConfigMetadata> getVirtualServerConfigList() {
		List<RadiusConfigMetadata> l = new ArrayList<RadiusConfigMetadata>();
		l.add(new RadiusConfigMetadata(Type.String, RadiusVirtualServerImpl.PORT_TYPE_KEY, true));
		l.add(new RadiusConfigMetadata(Type.String, RadiusVirtualServerImpl.HOSTNAME_KEY, false));
		l.add(new RadiusConfigMetadata(Type.String, RadiusVirtualServerImpl.PROFILE_KEY, true));
		l.add(new RadiusConfigMetadata(Type.Integer, RadiusVirtualServerImpl.PORT_KEY, true));
		return l;
	}

	private void loadModules() throws BackingStoreException {
		for (RadiusModuleType t : RadiusModuleType.values()) {
			RadiusModule module = new RadiusModule(bc, this, t, prefsvc);
			modules.put(t, module);
			loadModuleInstances(t);
			module.start();
		}
	}

	private void loadModuleInstances(RadiusModuleType t) throws BackingStoreException {
		RadiusModule module = modules.get(t);

		Preferences root = prefsvc.getSystemPreferences().node(t.getConfigNamespace());
		for (String instanceName : root.childrenNames())
			module.loadInstance(instanceName);
	}

	private void unloadModules() throws BackingStoreException {
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
		for (RadiusProfile profile : ProfileConfigHelper.loadProfiles(prefsvc)) {
			profiles.put(profile.getName(), profile);
		}
	}

	private Integer determinePort(RadiusConfigurator conf, RadiusPortType portType) {
		Integer port = conf.getInteger(RadiusVirtualServerImpl.PORT_KEY);
		if (port == null) {
			if (portType == RadiusPortType.Authentication)
				port = RadiusVirtualServerImpl.DEFAULT_AUTH_PORT;
			else if (portType == RadiusPortType.Accounting)
				port = RadiusVirtualServerImpl.DEFAULT_ACCT_PORT;
		}
		return port;
	}

	@Invalidate
	public void stop() throws BackingStoreException {
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

		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, VIRTUAL_SERVER_ROOT_KEY, name);
		conf.put("profile", profileName);
		RadiusVirtualServerImpl vs = new RadiusVirtualServerImpl(this, profileName, portType, conf, executor,
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
		ProfileConfigHelper.createProfile(prefsvc, profile);
		profiles.put(profile.getName(), profile);
	}

	@Override
	public void updateProfile(RadiusProfile profile) {
		verifyNotNull("profile", profile);
		ProfileConfigHelper.updateProfile(prefsvc, profile);
		profiles.put(profile.getName(), profile);
	}

	@Override
	public void removeProfile(String name) {
		verifyNotNull("name", name);
		ProfileConfigHelper.removeProfile(prefsvc, name);
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

				try {
					loadModuleInstances(t);
				} catch (BackingStoreException e) {
					logger.error("kraken radius: cannot load module instances for " + t);
				}

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
