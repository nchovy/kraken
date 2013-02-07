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
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LogParserFactoryRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "log-parser-factory-registry")
@Provides(specifications = { LogParserFactoryRegistry.class })
public class LogParserFactoryRegistryImpl extends ServiceTracker implements LogParserFactoryRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogParserFactoryRegistryImpl.class);
	private ConcurrentMap<String, LogParserFactory> factoryMap;
	private Set<LogParserFactoryRegistryEventListener> callbacks;

	public LogParserFactoryRegistryImpl(BundleContext bc) {
		super(bc, LogParserFactory.class.getName(), null);
		this.factoryMap = new ConcurrentHashMap<String, LogParserFactory>();
		this.callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LogParserFactoryRegistryEventListener, Boolean>());
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
		LogParserFactory factory = (LogParserFactory) super.addingService(reference);
		register(factory);
		return factory;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LogParserFactory factory = (LogParserFactory) service;
		unregister(factory);
		super.removedService(reference, service);
	}

	@Override
	public Collection<String> getNames() {
		return new ArrayList<String>(factoryMap.keySet());
	}

	@Override
	public LogParserFactory get(String name) {
		if (name == null)
			return null;

		return factoryMap.get(name);
	}

	@Override
	public void register(LogParserFactory factory) {
		verifyArgs(factory);

		LogParserFactory old = factoryMap.putIfAbsent(factory.getName(), factory);
		if (old != null)
			throw new IllegalStateException("duplicated log parser factory mapping: " + factory.getName());

		for (LogParserFactoryRegistryEventListener callback : callbacks) {
			try {
				callback.factoryAdded(factory);
			} catch (Exception e) {
				logger.warn("kraken-log-api: callback should not throw any exception", e);
			}
		}

		logger.info("kraken-log-api: new parser factory added for [{}]", factory.getName());
	}

	@Override
	public void unregister(LogParserFactory factory) {
		verifyArgs(factory);

		LogParserFactory old = factoryMap.get(factory.getName());
		if (old != factory) {
			logger.warn("kraken-log-api: cannot unregister log parser factory {}", factory.getName());
			return;
		}

		factoryMap.remove(factory.getName());

		for (LogParserFactoryRegistryEventListener callback : callbacks) {
			try {
				callback.factoryRemoved(factory);
			} catch (Exception e) {
				logger.warn("kraken-log-api: callback should not throw any exception", e);
			}
		}

		logger.info("kraken-log-api: parser factory removed for [{}]", factory.getName());
	}

	private void verifyArgs(LogParserFactory factory) {
		if (factory == null)
			throw new IllegalArgumentException("parser factory must be not null");
	}

	@Override
	public void addEventListener(LogParserFactoryRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("parser factory event listener must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(LogParserFactoryRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("parser factory event listener must be not null");

		callbacks.remove(callback);
	}
}
