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
package org.krakenapps.event.api.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.event.api.EventProvider;
import org.krakenapps.event.api.EventProviderRegistry;
import org.krakenapps.event.api.EventProviderRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "event-provider-registry")
@Provides
public class EventProviderRegistryImpl implements EventProviderRegistry {
	private final Logger logger = LoggerFactory.getLogger(EventProviderRegistryImpl.class.getName());

	private EventProviderTracker tracker;
	private ConcurrentMap<String, EventProvider> providers;
	private CopyOnWriteArraySet<EventProviderRegistryEventListener> callbacks;

	public EventProviderRegistryImpl(BundleContext bc) {
		tracker = new EventProviderTracker(bc, this);
		providers = new ConcurrentHashMap<String, EventProvider>();
		callbacks = new CopyOnWriteArraySet<EventProviderRegistryEventListener>();
	}

	@Validate
	public void start() {
		tracker.open();
	}

	@Invalidate
	public void stop() {
		tracker.close();
	}

	@Override
	public Collection<String> getNames() {
		return providers.keySet();
	}

	@Override
	public EventProvider get(String name) {
		return providers.get(name);
	}

	@Override
	public void register(EventProvider provider) {
		EventProvider old = providers.putIfAbsent(provider.getName(), provider);
		if (old != null)
			throw new IllegalStateException("duplicated event provider already exists: " + provider.getName());

		// invoke callbacks
		for (EventProviderRegistryEventListener callback : callbacks) {
			try {
				callback.providerAdded(provider);
			} catch (Exception e) {
				logger.error("kraken event api: event callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void unregister(EventProvider provider) {
		providers.remove(provider.getName());

		// invoke callbacks
		for (EventProviderRegistryEventListener callback : callbacks) {
			try {
				callback.providerRemoved(provider);
			} catch (Exception e) {
				logger.error("kraken event api: event callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void addEventListener(EventProviderRegistryEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(EventProviderRegistryEventListener callback) {
		callbacks.remove(callback);
	}
}
