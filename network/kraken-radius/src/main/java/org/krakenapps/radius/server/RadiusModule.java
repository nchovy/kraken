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
package org.krakenapps.radius.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.radius.server.impl.RadiusFactoryServiceTracker;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusModule {
	private final Logger logger = LoggerFactory.getLogger(RadiusModule.class.getName());

	private ConfigService cfg;
	private Map<String, RadiusFactory<?>> factories;
	private Map<String, RadiusInstance> instances;
	private RadiusFactoryServiceTracker tracker;

	public RadiusModule(BundleContext bc, RadiusFactoryEventListener listener, RadiusModuleType type, ConfigService cfg) {
		this.cfg = cfg;
		this.factories = new ConcurrentHashMap<String, RadiusFactory<?>>();
		this.instances = new ConcurrentHashMap<String, RadiusInstance>();
		this.tracker = new RadiusFactoryServiceTracker(listener, bc, type.getFactoryClass().getName());
	}

	public void start() {
		tracker.open();
	}

	public void stop() {
		tracker.close();
	}

	public List<RadiusFactory<?>> getFactories() {
		return new ArrayList<RadiusFactory<?>>(factories.values());
	}

	public RadiusFactory<?> getFactory(String name) {
		return factories.get(name);
	}

	public void addFactory(RadiusFactory<?> factory) {
		factories.put(factory.getName(), factory);
	}

	public void removeFactory(String name) {
		RadiusFactory<?> factory = factories.remove(name);
		if (factory == null)
			return;

		// remove all related instances
		for (RadiusInstance instance : instances.values()) {
			if (factory == instance.getFactory())
				instances.remove(instance.getName());
		}
	}

	public List<RadiusInstance> getInstances() {
		return new ArrayList<RadiusInstance>(instances.values());
	}

	public RadiusInstance getInstance(String name) {
		return instances.get(name);
	}

	public RadiusInstance createInstance(String instanceName, String factoryName, Map<String, Object> configs) {
		RadiusFactory<?> factory = factories.get(factoryName);
		if (factory == null)
			throw new IllegalArgumentException("factory not found: " + factoryName);

		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		RadiusInstanceConfig config = new RadiusInstanceConfig();
		config.setName(instanceName);
		config.setFactoryName(factoryName);
		config.setConfigs(configs);
		db.add(config);

		return loadInstance(instanceName);
	}

	public RadiusInstance loadInstance(String instanceName) {
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		Config c = db.findOne(RadiusInstanceConfig.class, Predicates.field("name", instanceName));
		if (c == null)
			return null;

		RadiusInstanceConfig config = c.getDocument(RadiusInstanceConfig.class);
		String factoryName = config.getFactoryName();

		// return already loaded instance
		if (instances.containsKey(instanceName))
			return instances.get(instanceName);

		// factory not loaded yet, try later
		RadiusFactory<?> factory = factories.get(factoryName);
		if (factory == null)
			return null;

		RadiusInstance instance = factory.newInstance(config);
		instances.put(instanceName, instance);
		logger.info("kraken radius: loaded radius instance [{}]", instanceName);

		return instance;
	}

	public void removeInstance(String instanceName) {
		ConfigDatabase db = cfg.ensureDatabase("kraken-radius");
		Config c = db.findOne(RadiusInstanceConfig.class, Predicates.field("name", instanceName));
		if (c != null) {
			db.remove(c);
		}

		RadiusInstance instance = instances.remove(instanceName);
		if (instance == null)
			return;

		instance.stop();
		logger.trace("kraken radius: unloaded radius instance [{}]", instanceName);
	}
}
