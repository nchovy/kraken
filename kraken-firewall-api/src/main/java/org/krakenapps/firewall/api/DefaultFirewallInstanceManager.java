/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.firewall.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultFirewallInstanceManager implements FirewallInstanceManager {
	private final Logger logger = LoggerFactory.getLogger(DefaultFirewallInstanceManager.class.getName());

	protected ConcurrentMap<String, FirewallInstance> instances;
	protected Set<FirewallInstanceManagerListener> callbacks;

	public DefaultFirewallInstanceManager() {
		this.instances = new ConcurrentHashMap<String, FirewallInstance>();
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<FirewallInstanceManagerListener, Boolean>());
	}

	@Override
	public Collection<FirewallInstance> getInstances() {
		return new ArrayList<FirewallInstance>(instances.values());
	}

	@Override
	public FirewallInstance getInstance(String name) {
		return instances.get(name);
	}

	@Override
	public FirewallInstance createInstance(String instanceName, Properties config) {
		if (instanceName == null)
			throw new IllegalArgumentException("instance name should not be null");

		if (getFirewallController().getInstance(instanceName) != null)
			throw new IllegalStateException("duplicated firewall instance name: " + instanceName);

		FirewallInstance instance = onCreate(instanceName, config);
		assert (instance != null);

		try {
			fireCreateEvent(instance);
			load(instance);
		} catch (IllegalStateException e) {
			unloadAndRemove(instanceName, instance);
			throw e;
		}

		return instance;
	}

	@Override
	public void removeInstance(String instanceName) {
		if (instanceName == null)
			throw new IllegalArgumentException("instance name should not be null");

		FirewallInstance instance = instances.get(instanceName);
		if (instance == null)
			throw new IllegalStateException("instance not found: " + instanceName);

		unloadAndRemove(instanceName, instance);
	}

	/**
	 * event callback should be called first, otherwise event callback will
	 * raise instance not found exception
	 */
	private void unloadAndRemove(String instanceName, FirewallInstance instance) {
		fireUnloadEvent(instance);
		fireRemoveEvent(instance);
		onRemove(instanceName);
		instances.remove(instance.getName());
		getFirewallController().unregister(instance);
	}

	@Override
	public void addEventListener(FirewallInstanceManagerListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback should not be null");

		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(FirewallInstanceManagerListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback should not be null");

		callbacks.remove(callback);
	}

	protected void load(FirewallInstance instance) {
		getFirewallController().register(instance);
		instances.put(instance.getName(), instance);
		fireLoadEvent(instance);
	}

	protected void unload(FirewallInstance instance) {
		fireUnloadEvent(instance);
		instances.remove(instance.getName());
		getFirewallController().unregister(instance);
	}

	protected abstract FirewallInstance onCreate(String instanceName, Properties config);

	protected abstract void onRemove(String instanceName);

	private void fireCreateEvent(FirewallInstance instance) {
		for (FirewallInstanceManagerListener callback : callbacks) {
			try {
				callback.onCreate(this, instance);
			} catch (Exception e) {
				logger.error("kraken firewall api: event callback should not throw any exception", e);
			}
		}
	}

	private void fireRemoveEvent(FirewallInstance instance) {
		for (FirewallInstanceManagerListener callback : callbacks) {
			try {
				callback.onRemove(this, instance);
			} catch (Exception e) {
				logger.error("kraken firewall api: event callback should not throw any exception", e);
			}
		}
	}

	private void fireLoadEvent(FirewallInstance instance) {
		for (FirewallInstanceManagerListener callback : callbacks) {
			try {
				callback.onLoad(this, instance);
			} catch (Exception e) {
				logger.error("kraken firewall api: event callback should not throw any exception", e);
			}
		}
	}

	private void fireUnloadEvent(FirewallInstance instance) {
		for (FirewallInstanceManagerListener callback : callbacks) {
			try {
				callback.onUnload(this, instance);
			} catch (Exception e) {
				logger.error("kraken firewall api: event callback should not throw any exception", e);
			}
		}
	}

	protected abstract FirewallController getFirewallController();
}
