/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.jpa.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaConfig {
	final Logger logger = LoggerFactory.getLogger(JpaConfig.class.getName());
	private Preferences prefs;

	public JpaConfig(Preferences prefs) {
		this.prefs = prefs.node("/kraken-jpa"); // from root system preference
	}

	public List<EntityManagerFactoryInstance> getEntityManagerFactoryInstances() {
		List<EntityManagerFactoryInstance> instances = new ArrayList<EntityManagerFactoryInstance>();
		try {
			Preferences emfPrefs = prefs.node("emf");
			for (String name : emfPrefs.childrenNames()) {
				Preferences node = emfPrefs.node(name);

				int bundleId = node.getInt("bundle_id", -1);
				String factoryName = node.get("factory_name", null);
				Properties props = new Properties();

				Preferences propsPrefs = node.node("properties");
				for (String key : propsPrefs.keys()) {
					props.put(key, propsPrefs.get(key, null));
				}

				EntityManagerFactoryInstance instance = new EntityManagerFactoryInstance(bundleId, factoryName, props);
				instances.add(instance);
			}
		} catch (BackingStoreException e) {
			logger.warn("kraken jpa: get emf failed", e);
		}

		return instances;
	}

	public boolean addEntityManagerFactoryInstance(long bundleId, String factoryName, Properties properties) {
		try {
			Preferences emfPrefs = prefs.node("emf");
			logger.trace("kraken jpa: adding entity manager factory => {}", factoryName);

			Preferences newEmf = emfPrefs.node(factoryName);
			newEmf.putLong("bundle_id", bundleId);
			newEmf.put("factory_name", factoryName);

			Preferences props = newEmf.node("properties");
			for (Object key : properties.keySet()) {
				props.put(key.toString(), properties.get(key).toString());
			}

			newEmf.flush();
			newEmf.sync();
			
			logger.trace("kraken jpa: emf [{}] config created", factoryName);

			return true;
		} catch (BackingStoreException e) {
			logger.warn("kraken jpa: add emf instance failed", e);
		}

		return false;
	}

	public void removeEntityManagerFactoryInstance(String factoryName) {
		try {
			Preferences emfPrefs = prefs.node("emf");
			if (!emfPrefs.nodeExists(factoryName))
				return;

			emfPrefs.node(factoryName).removeNode();
			emfPrefs.flush();
			emfPrefs.sync();
			
			logger.trace("kraken jpa: emf [{}] config removed", factoryName);
		} catch (BackingStoreException e) {
			logger.warn("kraken jpa: remove emf instance failed");
		}
	}
}
