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
import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.log.api.LogNormalizerRegistry;
import org.krakenapps.log.api.LogNormalizerRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "log-normalizer-registry")
@Provides(specifications = { LogNormalizerRegistry.class })
public class LogNormalizerRegistryImpl extends ServiceTracker implements LogNormalizerRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogNormalizerRegistryImpl.class.getName());

	private ConcurrentMap<String, LogNormalizer> normalizerMap;
	private Set<LogNormalizerRegistryEventListener> callbacks;

	public LogNormalizerRegistryImpl(BundleContext bc) {
		super(bc, LogNormalizer.class.getName(), null);
		normalizerMap = new ConcurrentHashMap<String, LogNormalizer>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LogNormalizerRegistryEventListener, Boolean>());
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
		normalizerMap.clear();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		LogNormalizer normalizer = (LogNormalizer) super.addingService(reference);
		register(normalizer);
		return normalizer;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LogNormalizer normalizer = (LogNormalizer) service;
		unregister(normalizer);
		super.removedService(reference, service);
	}

	@Override
	public Collection<String> getNames() {
		return new ArrayList<String>(normalizerMap.keySet());
	}

	@Override
	public LogNormalizer get(String loggerFactoryName) {
		if (loggerFactoryName == null)
			return null;
		
		return normalizerMap.get(loggerFactoryName);
	}

	@Override
	public void register(LogNormalizer normalizer) {
		verifyArgs(normalizer);

		LogNormalizer old = normalizerMap.putIfAbsent(normalizer.getName(), normalizer);
		if (old != null)
			throw new IllegalStateException("duplicated log normalizer mapping: " + normalizer.getName());

		// invoke callbacks
		for (LogNormalizerRegistryEventListener callback : callbacks) {
			try {
				callback.normalizerAdded(normalizer);
			} catch (Exception e) {
				logger.warn("log normalizer callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void unregister(LogNormalizer normalizer) {
		verifyArgs(normalizer);

		LogNormalizer old = normalizerMap.get(normalizer.getName());
		if (old != normalizer)
			return;

		normalizerMap.remove(normalizer.getName());

		// invoke callbacks
		for (LogNormalizerRegistryEventListener callback : callbacks) {
			try {
				callback.normalizerRemoved(normalizer);
			} catch (Exception e) {
				logger.warn("log normalizer callback should not throw any exception", e);
			}
		}
	}

	private void verifyArgs(LogNormalizer normalizer) {
		if (normalizer == null)
			throw new IllegalArgumentException("log normalizer must be not null");
	}

	@Override
	public void addEventListener(LogNormalizerRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("normalizer callback must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(LogNormalizerRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("normalizer callback must be not null");

		callbacks.remove(callback);
	}

}
