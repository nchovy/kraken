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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryEventListener;
import org.krakenapps.log.api.LoggerFactoryRegistryEventListener;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.LoggerRegistryEventListener;

@Component(name = "logger-registry")
@Provides(specifications = { LoggerRegistry.class })
public class LoggerRegistryImpl implements LoggerRegistry, LoggerFactoryRegistryEventListener, LoggerFactoryEventListener,
		LogPipe {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggerRegistryImpl.class.getName());
	private ConcurrentMap<String, Logger> loggers;
	private Set<LoggerRegistryEventListener> callbacks;
	private ConcurrentMap<String, Set<LogPipe>> pipeMap;
	private boolean isOpen = false;

	public LoggerRegistryImpl() {
		loggers = new ConcurrentHashMap<String, Logger>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoggerRegistryEventListener, Boolean>());
		pipeMap = new ConcurrentHashMap<String, Set<LogPipe>>();
	}

	@Override
	public boolean isOpen() {
		return isOpen;
	}

	@Validate
	public void start() {
		callbacks.clear();
		isOpen = true;
	}

	@Invalidate
	public void stop() {
		isOpen = false;
		callbacks.clear();
	}

	@Override
	public void factoryAdded(LoggerFactory loggerFactory) {
		loggerFactory.addListener(this);
	}

	@Override
	public void factoryRemoved(LoggerFactory loggerFactory) {
		loggerFactory.removeListener(this);

		// remove all related loggers
		List<Logger> removeList = new ArrayList<Logger>();
		for (Logger logger : loggers.values())
			if (logger.getFactoryFullName().equals(loggerFactory.getFullName()))
				removeList.add(logger);

		for (Logger logger : removeList) {
			try {
				// logger stop event caused by factory removal will not be sent.
				logger.clearEventListeners();
				logger.stop();
				removeLogger(logger);
			} catch (Exception e) {
				log.warn("kraken-log-api: logger remove error", e);
			}
		}
	}

	@Override
	public Collection<Logger> getLoggers() {
		return new ArrayList<Logger>(loggers.values());
	}

	@Override
	public Logger getLogger(String fullName) {
		return loggers.get(fullName);
	}

	@Override
	public Logger getLogger(String namespace, String name) {
		return loggers.get(namespace + "\\" + name);
	}

	@Override
	public void addLogger(Logger logger) {
		log.debug("kraken log api: adding logger [{}]", logger.getFullName());
		Logger old = loggers.putIfAbsent(logger.getFullName(), logger);
		if (old != null)
			throw new IllegalStateException("logger already exists: " + logger.getFullName());

		// connect pipe
		logger.addLogPipe(this);

		log.debug("kraken log api: logger [{}] added", logger.getFullName());

		// invoke logger event callbacks
		for (LoggerRegistryEventListener callback : callbacks) {
			try {
				callback.loggerAdded(logger);
			} catch (Exception e) {
				log.warn("kraken-log-api: logger registry callback should not throw exception", e);
			}
		}
	}

	@Override
	public void removeLogger(Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException("logger must not be null");

		log.debug("kraken log api: removing logger [{}]", logger.getFullName());

		if (logger.isRunning())
			throw new IllegalStateException("logger is still running");

		loggers.remove(logger.getFullName());

		// disconnect pipe
		logger.removeLogPipe(this);

		log.debug("kraken log api: logger [{}] removed", logger.getFullName());

		// invoke logger event callbacks
		for (LoggerRegistryEventListener callback : callbacks) {
			try {
				callback.loggerRemoved(logger);
			} catch (Exception e) {
				log.warn("kraken-log-api: logger registry callback should not throw exception", e);
			}
		}
	}

	@Override
	public void loggerCreated(LoggerFactory factory, Logger logger, Properties config) {
		addLogger(logger);
	}

	@Override
	public void loggerDeleted(LoggerFactory factory, Logger logger) {
		if (logger != null)
			removeLogger(logger);
	}

	@Override
	public void addLogPipe(String loggerFactoryName, LogPipe pipe) {
		Set<LogPipe> pipes = Collections.newSetFromMap(new ConcurrentHashMap<LogPipe, Boolean>());
		Set<LogPipe> oldPipes = pipeMap.putIfAbsent(loggerFactoryName, pipes);
		if (oldPipes != null)
			pipes = oldPipes;

		pipes.add(pipe);
	}

	@Override
	public void removeLogPipe(String loggerFactoryName, LogPipe pipe) {
		Set<LogPipe> pipes = pipeMap.get(loggerFactoryName);
		if (pipes == null)
			return;

		pipes.remove(pipe);
	}

	@Override
	public void addListener(LoggerRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must not be null");

		callbacks.add(callback);
	}

	@Override
	public void removeListener(LoggerRegistryEventListener callback) {
		if (callback == null)
			throw new IllegalArgumentException("callback must not be null");

		callbacks.remove(callback);
	}

	@Override
	public void onLog(Logger logger, Log log) {
		Set<LogPipe> pipes = pipeMap.get(logger.getFactoryName());
		if (pipes == null)
			return;

		for (LogPipe pipe : pipes) {
			pipe.onLog(logger, log);
		}
	}
}
