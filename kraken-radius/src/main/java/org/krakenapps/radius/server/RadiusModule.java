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

import org.krakenapps.radius.server.impl.PreferencesConfigurator;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadiusModule<Factory extends RadiusFactory<Instance>, Instance extends RadiusInstance> {
	private final Logger logger = LoggerFactory.getLogger(RadiusModule.class.getName());

	private PreferencesService prefsvc;
	private String configNamespace;
	private Map<String, Factory> factories;
	private Map<String, Instance> instances;

	public RadiusModule(PreferencesService prefsvc, String categoryKey) {
		this.prefsvc = prefsvc;
		this.configNamespace = categoryKey;
		this.factories = new ConcurrentHashMap<String, Factory>();
		this.instances = new ConcurrentHashMap<String, Instance>();
	}

	public List<Factory> getFactories() {
		return new ArrayList<Factory>(factories.values());
	}

	public Factory getFactory(String name) {
		return factories.get(name);
	}

	@SuppressWarnings("unchecked")
	public void addFactory(Object factory) {
		Factory f = (Factory) factory;
		factories.put(f.getName(), f);
	}

	public void removeFactory(String name) {
		factories.remove(name);
	}

	public List<Instance> getInstances() {
		return new ArrayList<Instance>(instances.values());
	}

	public Instance getInstance(String name) {
		return instances.get(name);
	}

	public Instance createInstance(String instanceName, String factoryName, Map<String, Object> configs) {
		Factory factory = factories.get(factoryName);
		if (factory == null)
			throw new IllegalArgumentException("factory not found: " + factoryName);

		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, configNamespace, instanceName);
		conf.put("factory_name", factory.getName());
		return loadInstance(instanceName);
	}

	public Instance loadInstance(String instanceName) {
		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, configNamespace, instanceName);
		String factoryName = conf.getString("factory_name");

		// return already loaded instance
		if (instances.containsKey(instanceName))
			return instances.get(instanceName);

		// factory not loaded yet, try later
		Factory factory = factories.get(factoryName);
		if (factory == null)
			return null;

		Instance instance = factory.newInstance(instanceName, conf);
		instances.put(instanceName, instance);
		logger.trace("kraken radius: loaded radius instance [{}]", instanceName);
		return instance;
	}

	public void removeInstance(String instanceName) {
		RadiusConfigurator conf = new PreferencesConfigurator(prefsvc, configNamespace, instanceName);
		conf.purge();

		Instance instance = instances.remove(instanceName);
		if (instance == null)
			return;

		instance.stop();
		logger.trace("kraken radius: unloaded radius instance [{}]", instanceName);
	}
}
