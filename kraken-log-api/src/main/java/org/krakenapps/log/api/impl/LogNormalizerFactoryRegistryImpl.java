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
package org.krakenapps.log.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.LogNormalizerFactory;
import org.krakenapps.log.api.LogNormalizerFactoryRegistry;
import org.krakenapps.log.api.LogNormalizerFactoryRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "log-normalizer-registry")
@Provides(specifications = { LogNormalizerFactoryRegistry.class })
public class LogNormalizerFactoryRegistryImpl extends ServiceTracker implements LogNormalizerFactoryRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogNormalizerFactoryRegistryImpl.class.getName());

	private ConcurrentMap<String, LogNormalizerFactory> factoryMap;
	private Set<LogNormalizerFactoryRegistryEventListener> callbacks;

	public LogNormalizerFactoryRegistryImpl(BundleContext bc) {
		super(bc, LogNormalizerFactory.class.getName(), null);
		factoryMap = new ConcurrentHashMap<String, LogNormalizerFactory>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LogNormalizerFactoryRegistryEventListener, Boolean>());
	}

	@Validate
	public void start() {
		reset();
		super.open();
	}

	@Invalidate
	public void stop() {
		super.close();
		reset();
	}

	private void reset() {
		callbacks.clear();
		factoryMap.clear();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		LogNormalizerFactory normalizer = (LogNormalizerFactory) super.addingService(reference);
		register(normalizer);
		return normalizer;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LogNormalizerFactory normalizer = (LogNormalizerFactory) service;
		unregister(normalizer);
		super.removedService(reference, service);
	}

	@Override
	public Collection<String> getNames() {
		return new ArrayList<String>(factoryMap.keySet());
	}

	@Override
	public LogNormalizerFactory get(String loggerFactoryName) {
		if (loggerFactoryName == null)
			return null;

		return factoryMap.get(loggerFactoryName);
	}

	@Override
	public void register(LogNormalizerFactory normalizer) {
		verifyArgs(normalizer);

		LogNormalizerFactory old = factoryMap.putIfAbsent(normalizer.getName(), normalizer);
		if (old != null)
			throw new IllegalStateException("duplicated log normalizer mapping: " + normalizer.getName());

		// invoke callbacks
		for (LogNormalizerFactoryRegistryEventListener callback : callbacks) {
			try {
				callback.normalizerAdded(normalizer);
			} catch (Exception e) {
				logger.warn("log normalizer callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void unregister(LogNormalizerFactory normalizer) {
		verifyArgs(normalizer);

		LogNormalizerFactory old = factoryMap.get(normalizer.getName());
		if (old != normalizer)
			return;

		factoryMap.remove(normalizer.getName());

		// invoke callbacks
		for (LogNormalizerFactoryRegistryEventListener callback : callbacks) {
			try {
				callback.normalizerRemoved(normalizer);
			} catch (Exception e) {
				logger.warn("log normalizer callback should not throw any exception", e);
			}
		}
	}

	private void verifyArgs(LogNormalizerFactory normalizer) {
		if (normalizer == null)
			throw new IllegalArgumentException("log normalizer must be not null");
	}

	@Override
	public void addEventListener(LogNormalizerFactoryRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("normalizer callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(LogNormalizerFactoryRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("normalizer callback must be not null");

		callbacks.remove(callback);
	}
}
