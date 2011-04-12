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
package org.krakenapps.log.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLoggerFactory implements LoggerFactory {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(AbstractLoggerFactory.class.getName());

	private String namespace;
	private String fullName;
	private Map<String, Logger> loggers;
	private Set<LoggerFactoryEventListener> callbacks;

	public AbstractLoggerFactory() {
		this("local");
	}

	public AbstractLoggerFactory(String namespace) {
		this.namespace = namespace;
		loggers = new ConcurrentHashMap<String, Logger>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoggerFactoryEventListener, Boolean>());
	}

	@Override
	public String getFullName() {
		if (fullName == null)
			fullName = namespace + "\\" + getName();

		return fullName;
	}

	@Override
	public final String getNamespace() {
		return namespace;
	}

	@Override
	public final void addListener(LoggerFactoryEventListener callback) {
		callbacks.add(callback);
	}

	@Override
	public final void removeListener(LoggerFactoryEventListener callback) {
		callbacks.remove(callback);
	}

	@Override
	public final Logger newLogger(String name, String description, Properties config) {
		return newLogger("local", name, description, config);
	}

	@Override
	public final Logger newLogger(String namespace, String name, String description, Properties config) {
		return newLogger(namespace, name, description, 0, null, config);
	}

	@Override
	public Logger newLogger(String namespace, String name, String description, long logCount, Date lastLogDate,
			Properties config) {
		Logger logger = createLogger(new LoggerSpecification(namespace, name, description, logCount, lastLogDate,
				config));
		loggers.put(logger.getFullName(), logger);

		for (LoggerFactoryEventListener callback : callbacks) {
			callback.loggerCreated(this, logger, config);
		}

		return logger;
	}

	@Override
	public void deleteLogger(String name) {
		deleteLogger("local", name);
	}

	@Override
	public void deleteLogger(String namespace, String name) {
		String fullName = namespace + "\\" + name;
		Logger logger = loggers.get(fullName);
		if (logger == null)
			throw new IllegalStateException("logger not found: " + fullName);

		for (LoggerFactoryEventListener callback : callbacks) {
			try {
				callback.loggerDeleted(this, logger);
			} catch (Exception e) {
				slog.error("kraken log api: logger factory event listener should not throw any exception", e);
			}
		}
	}

	//
	// default empty implementation
	//

	@Override
	public Collection<LoggerConfigOption> getConfigOptions() {
		return new ArrayList<LoggerConfigOption>();
	}

	@Override
	public Collection<Locale> getDisplayNameLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	@Override
	public Collection<Locale> getDescriptionLocales() {
		return Arrays.asList(Locale.ENGLISH);
	}

	protected abstract Logger createLogger(LoggerSpecification spec);

	@Override
	public String toString() {
		return String.format("fullname=%s, type=%s, description=%s", getFullName(), getDisplayName(Locale.ENGLISH),
				getDescription(Locale.ENGLISH));
	}

}
