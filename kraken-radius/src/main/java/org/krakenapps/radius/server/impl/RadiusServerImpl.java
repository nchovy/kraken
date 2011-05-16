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

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.radius.server.RadiusAuthenticator;
import org.krakenapps.radius.server.RadiusAuthenticatorFactory;
import org.krakenapps.radius.server.RadiusConfigurator;
import org.krakenapps.radius.server.RadiusPortType;
import org.krakenapps.radius.server.RadiusProfile;
import org.krakenapps.radius.server.RadiusServer;
import org.krakenapps.radius.server.RadiusServerEventListener;
import org.krakenapps.radius.server.RadiusUserDatabase;
import org.krakenapps.radius.server.RadiusUserDatabaseFactory;
import org.krakenapps.radius.server.RadiusVirtualServer;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "radius-server")
@Provides
public class RadiusServerImpl implements RadiusServer {
	private static final String VIRTUAL_SERVER_ROOT_KEY = "virtual_servers";
	private static final String AUTHENTICATOR_ROOT_KEY = "authenticators";
	private static final String USERDB_ROOT_KEY = "user_databases";

	private final Logger logger = LoggerFactory.getLogger(RadiusServerImpl.class.getName());

	@Requires
	private PreferencesService prefsvc;

	private BundleContext bc;

	private Map<String, RadiusVirtualServer> virtualServers;
	private Map<String, RadiusProfile> profiles;
	private Map<String, RadiusAuthenticatorFactory> authenticatorFactories;
	private Map<String, RadiusUserDatabaseFactory> userDatabaseFactories;
	private Map<String, RadiusAuthenticator> authenticators;
	private Map<String, RadiusUserDatabase> userDatabases;

	private CopyOnWriteArraySet<RadiusServerEventListener> callbacks;

	private RadiusServiceTracker authTracker;
	private RadiusServiceTracker userdbTracker;

	public RadiusServerImpl(BundleContext bc) {
		this.bc = bc;
	}

	@Validate
	public void start() {
		callbacks = new CopyOnWriteArraySet<RadiusServerEventListener>();

		virtualServers = new ConcurrentHashMap<String, RadiusVirtualServer>();
		profiles = new ConcurrentHashMap<String, RadiusProfile>();
		authTracker = new RadiusServiceTracker(this, bc, RadiusAuthenticatorFactory.class.getName());
		userdbTracker = new RadiusServiceTracker(this, bc, RadiusUserDatabaseFactory.class.getName());
		authenticators = new ConcurrentHashMap<String, RadiusAuthenticator>();
		userDatabases = new ConcurrentHashMap<String, RadiusUserDatabase>();

		try {
			loadVirtualServers();

			authTracker.open();
			userdbTracker.open();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadVirtualServers() throws BackingStoreException {
		Preferences root = prefsvc.getSystemPreferences().node(VIRTUAL_SERVER_ROOT_KEY);
		for (String name : root.childrenNames()) {
			try {
				RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, VIRTUAL_SERVER_ROOT_KEY, name);
				String hostname = conf.getString(RadiusVirtualServerImpl.HOSTNAME_KEY);
				RadiusPortType portType = RadiusPortType.parse(conf.getString(RadiusVirtualServerImpl.PORT_TYPE_KEY));
				Integer port = determinePort(conf, portType);

				InetSocketAddress bindAddress = null;
				if (hostname != null)
					bindAddress = new InetSocketAddress(hostname, port);
				else
					bindAddress = new InetSocketAddress(port);

				RadiusVirtualServerImpl vs = new RadiusVirtualServerImpl(this, name, portType, conf, bindAddress);
				vs.open();

				virtualServers.put(name, vs);
			} catch (IOException e) {
				logger.error("kraken radius: cannot load virtual server - " + name, e);
			}
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
	public void stop() {
		authTracker.close();
		userdbTracker.close();

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
	public RadiusVirtualServer createVirtualServer(String name, InetSocketAddress bindAddress, RadiusPortType portType,
			String profileName) {
		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, VIRTUAL_SERVER_ROOT_KEY, name);
		return new RadiusVirtualServerImpl(this, profileName, portType, conf, bindAddress);
	}

	@Override
	public void removeVirtualServer(String name) {
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
		return profiles.get(name);
	}

	@Override
	public void createProfile(RadiusProfile profile) {
		ProfileConfigHelper.createProfile(prefsvc, profile);
	}

	@Override
	public void updateProfile(RadiusProfile profile) {
		ProfileConfigHelper.updateProfile(prefsvc, profile);
	}

	@Override
	public void removeProfile(String name) {
		ProfileConfigHelper.removeProfile(prefsvc, name);
	}

	@Override
	public List<RadiusAuthenticatorFactory> getAuthenticatorFactories() {
		return new ArrayList<RadiusAuthenticatorFactory>(authenticatorFactories.values());
	}

	@Override
	public List<RadiusAuthenticator> getAuthenticators() {
		return new ArrayList<RadiusAuthenticator>(authenticators.values());
	}

	@Override
	public RadiusAuthenticator createAuthenticator(String instanceName, String factoryName, Map<String, Object> configs) {
		RadiusAuthenticatorFactory factory = authenticatorFactories.get(factoryName);
		if (factory == null)
			throw new IllegalArgumentException("factory not found: " + factoryName);

		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, AUTHENTICATOR_ROOT_KEY, instanceName,
				factory.getConfigMetadatas());
		RadiusAuthenticator auth = factory.newInstance(instanceName, conf);

		return auth;
	}

	@Override
	public void removeAuthenticator(String instanceName) {
		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, AUTHENTICATOR_ROOT_KEY, instanceName);
		conf.purge();

		RadiusAuthenticator auth = authenticators.get(instanceName);
		if (auth != null)
			auth.stop();
	}

	@Override
	public List<RadiusUserDatabase> getUserDatabases() {
		return new ArrayList<RadiusUserDatabase>(userDatabases.values());
	}

	@Override
	public RadiusUserDatabase getUserDatabase(String name) {
		return userDatabases.get(name);
	}

	@Override
	public RadiusUserDatabase createUserDatabase(String instanceName, String factoryName, Map<String, Object> configs) {
		RadiusUserDatabaseFactory factory = userDatabaseFactories.get(factoryName);
		if (factory == null)
			throw new IllegalArgumentException("user database factory not found: " + factoryName);

		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, USERDB_ROOT_KEY, instanceName);
		RadiusUserDatabase udb = factory.newInstance(instanceName, conf);
		return udb;
	}

	@Override
	public void removeUserDatabase(String instanceName) {
		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, USERDB_ROOT_KEY, instanceName);
		conf.purge();

		RadiusUserDatabase udb = userDatabases.remove(instanceName);
		if (udb != null)
			udb.stop();
	}

	@Override
	public void addEventListener(RadiusServerEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeEventListener(RadiusServerEventListener listener) {
		callbacks.remove(listener);
	}

	public void addingService(Object service) {
		if (service instanceof RadiusAuthenticatorFactory) {
			RadiusAuthenticatorFactory af = (RadiusAuthenticatorFactory) service;
			authenticatorFactories.put(af.getName(), af);
		} else if (service instanceof RadiusUserDatabaseFactory) {
			RadiusUserDatabaseFactory udf = (RadiusUserDatabaseFactory) service;
			userDatabaseFactories.put(udf.getName(), udf);
		}
	}

	public void removedService(Object service) {
		if (service instanceof RadiusAuthenticatorFactory) {
			RadiusAuthenticatorFactory af = (RadiusAuthenticatorFactory) service;
			authenticatorFactories.remove(af.getName());
		} else if (service instanceof RadiusUserDatabaseFactory) {
			RadiusUserDatabaseFactory udf = (RadiusUserDatabaseFactory) service;
			userDatabaseFactories.remove(udf.getName());
		}
	}
}
