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
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserRegistry;
import org.krakenapps.log.api.LogParserRegistryEventListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "log-parser-registry")
@Provides(specifications = { LogParserRegistry.class })
public class LogParserRegistryImpl extends ServiceTracker implements LogParserRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogParserRegistryImpl.class.getName());
	private ConcurrentMap<String, LogParser> parserMap;
	private Set<LogParserRegistryEventListener> callbacks;

	public LogParserRegistryImpl(BundleContext bc) {
		super(bc, LogParser.class.getName(), null);
		parserMap = new ConcurrentHashMap<String, LogParser>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LogParserRegistryEventListener, Boolean>());
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
		parserMap.clear();
	}

	@Override
	public Object addingService(ServiceReference reference) {
		LogParser parser = (LogParser) super.addingService(reference);
		register(parser);
		return parser;
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		LogParser parser = (LogParser) service;
		unregister(parser);
		super.removedService(reference, service);
	}

	@Override
	public Collection<String> getNames() {
		return new ArrayList<String>(parserMap.keySet());
	}

	@Override
	public LogParser get(String loggerFactoryName) {
		if (loggerFactoryName == null)
			return null;
		
		return parserMap.get(loggerFactoryName);
	}

	@Override
	public void register(LogParser parser) {
		verifyArgs(parser);

		LogParser old = parserMap.putIfAbsent(parser.getName(), parser);
		if (old != null)
			throw new IllegalStateException("duplicated log parser mapping: " + parser.getName());

		for (LogParserRegistryEventListener callback : callbacks) {
			try {
				callback.parserAdded(parser);
			} catch (Exception e) {
				logger.warn("parser event callback should not throw any exception", e);
			}
		}

		logger.info("log parser registry: new parser added for [{}]", parser.getName());
	}

	@Override
	public void unregister(LogParser parser) {
		verifyArgs(parser);

		LogParser old = parserMap.get(parser.getName());
		if (old != parser) {
			logger.warn("log parser registry: cannot unregister log parser {}", parser.getName());
			return;
		}

		parserMap.remove(parser.getName());

		for (LogParserRegistryEventListener callback : callbacks) {
			try {
				callback.parserRemoved(parser);
			} catch (Exception e) {
				logger.warn("parser event callback should not throw any exception", e);
			}
		}

		logger.info("log parser registry: parser removed for [{}]", parser.getName());
	}

	private void verifyArgs(LogParser parser) {
		if (parser == null)
			throw new IllegalArgumentException("parser must be not null");
	}

	@Override
	public void addEventListener(LogParserRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("parser event listener must be not null");

		callbacks.add(callback);
	}

	@Override
	public void removeEventListener(LogParserRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("parser event listener must be not null");

		callbacks.remove(callback);
	}
}
