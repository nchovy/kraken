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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.log.api.impl.LoggerConfig;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public abstract class AbstractLoggerFactory implements LoggerFactory {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(AbstractLoggerFactory.class.getName());

	private String namespace;
	private String fullName;
	private Map<String, Logger> loggers;
	private Set<LoggerFactoryEventListener> callbacks;

	private BundleContext bc;
	private boolean started;
	private DbSync sync = new DbSync();;

	public AbstractLoggerFactory() {
		this("local");
	}

	public AbstractLoggerFactory(String namespace) {
		this.namespace = namespace;
		loggers = new ConcurrentHashMap<String, Logger>();
		callbacks = Collections.newSetFromMap(new ConcurrentHashMap<LoggerFactoryEventListener, Boolean>());
	}

	@Override
	public void onStart(BundleContext bc) {
		if (started)
			throw new IllegalStateException("logger factory [" + fullName + "] is already started");

		this.bc = bc;
		loadLoggers();
		this.started = true;
	}

	@Override
	public void onStop() {
		if (!started)
			throw new IllegalStateException("logger factory [" + fullName + "] is not started");

		unloadLoggers();
		started = false;
	}

	private void loadLoggers() {
		Collection<LoggerConfig> configs = getLoggerConfigs();
		for (LoggerConfig config : configs) {
			slog.info("kraken log api: trying to load logger [{}]", config);
			tryLoad(config);
		}
	}

	private void unloadLoggers() {
		// unload from registry
		LoggerRegistry loggerRegistry = getLoggerRegistry();
		for (Logger logger : loggers.values()) {
			// prevent stop state saving
			logger.removeEventListener(sync);

			// stop and unregister
			logger.stop(5000);
			if (loggerRegistry != null)
				loggerRegistry.removeLogger(logger);
		}

		loggers.clear();
	}

	private void tryLoad(LoggerConfig config) {
		// if logger already exists
		LoggerRegistry loggerRegistry = getLoggerRegistry();
		if (loggerRegistry.getLogger(config.getFullname()) != null) {
			slog.warn("kraken log api: logger [{}] already registered, skip auto-load", config.getFullname());
			return;
		}

		try {
			Logger newLogger = handleNewLogger(config.getNamespace(), config.getName(), config.getDescription(),
					config.getCount(), config.getLastLogDate(), config.getConfigs(), true);
			newLogger.setPassive(config.isPassive());
			slog.info("kraken log api: logger [{}] is loaded", config.getFullname());
			if (config.isRunning() && config.getInterval() != -1) {
				newLogger.start(config.getInterval());
				slog.info("kraken log api: logger [{}] started with interval {}ms", config.getFullname(), config.getInterval());
			}
		} catch (Exception e) {
			slog.error(String.format("kraken log api: cannot load logger %s, saved config deleted.", config.getFullname()), e);
		}
	}

	private LoggerRegistry getLoggerRegistry() {
		ServiceReference ref = bc.getServiceReference(LoggerRegistry.class.getName());
		if (ref == null)
			return null;
		return (LoggerRegistry) bc.getService(ref);
	}

	private Collection<LoggerConfig> getLoggerConfigs() {
		ConfigDatabase db = getConfigDatabase();
		Map<String, Object> pred = new HashMap<String, Object>();
		pred.put("factory_namespace", getNamespace());
		pred.put("factory_name", getName());

		return db.find(LoggerConfig.class, Predicates.field(pred)).getDocuments(LoggerConfig.class);
	}

	private ConfigDatabase getConfigDatabase() {
		ServiceReference ref = bc.getServiceReference(ConfigService.class.getName());
		ConfigService conf = (ConfigService) bc.getService(ref);

		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		return db;
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
	public Logger newLogger(String namespace, String name, String description, long logCount, Date lastLogDate, Properties config) {
		return handleNewLogger(namespace, name, description, logCount, lastLogDate, config, false);
	}

	private Logger handleNewLogger(String namespace, String name, String description, long logCount, Date lastLogDate,
			Properties config, boolean booting) {
		Logger logger = createLogger(new LoggerSpecification(namespace, name, description, logCount, lastLogDate, config));
		loggers.put(logger.getFullName(), logger);

		// add listener, save config, and register logger
		logger.addEventListener(sync);
		if (!booting)
			saveLoggerConfig(logger, config);

		LoggerRegistry loggerRegistry = getLoggerRegistry();
		loggerRegistry.addLogger(logger);

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

		// remove listener, remove from logger registry, and delete config
		logger.removeEventListener(sync);
		LoggerRegistry loggerRegistry = getLoggerRegistry();
		loggerRegistry.removeLogger(logger);
		deleteLoggerConfig(logger);

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

	private void saveLoggerConfig(Logger logger, Properties config) {
		LoggerConfig model = new LoggerConfig(logger);
		model.setConfigs(config);

		ConfigDatabase db = getConfigDatabase();
		Config c = db.findOne(LoggerConfig.class, Predicates.field("fullname", logger.getFullName()));
		if (c == null) {
			db.add(model);
			slog.trace("kraken log api: created logger [{}] config, {}", logger.getFullName(), logger.isRunning());
		} else {
			if (!PrimitiveConverter.serialize(model).equals(c.getDocument())) {
				db.update(c, model);
				slog.trace("kraken log api: updated logger [{}] config, {}", logger.getFullName(), logger.isRunning());
			}
		}
	}

	private void deleteLoggerConfig(Logger logger) {
		ConfigDatabase db = getConfigDatabase();
		Config c = db.findOne(LoggerConfig.class, Predicates.field("fullname", logger.getFullName()));
		if (c != null)
			db.remove(c);
		slog.info("kraken log api: deleted logger config for [{}]", logger.getFullName());
	}

	private class DbSync implements LoggerEventListener {
		@Override
		public void onStart(Logger logger) {
			ConfigDatabase db = getConfigDatabase();
			Config c = db.findOne(LoggerConfig.class, Predicates.field("fullname", logger.getFullName()));
			if (c == null) {
				slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
				return;
			}

			LoggerConfig model = c.getDocument(LoggerConfig.class);
			model.setRunning(true);
			model.setInterval(logger.getInterval());
			db.update(c, model);

			slog.trace("kraken log api: running status saved: {}", logger.getFullName());
		}

		@Override
		public void onStop(Logger logger) {
			ConfigDatabase db = getConfigDatabase();
			Config c = db.findOne(LoggerConfig.class, Predicates.field("fullname", logger.getFullName()));
			if (c == null) {
				slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
				return;
			}

			LoggerConfig model = c.getDocument(LoggerConfig.class);
			// do not save status caused by bundle stopping
			LoggerRegistry loggerRegistry = getLoggerRegistry();
			if (loggerRegistry != null && loggerRegistry.isOpen()) {
				slog.trace("kraken log api: [{}] stopped state saved", logger.getFullName());
				model.setRunning(false);
			}
			model.setCount(logger.getLogCount());
			model.setLastLogDate(logger.getLastLogDate());
			db.update(c, model);
		}

		@Override
		public void onUpdated(Logger logger, Properties config) {
			saveLoggerConfig(logger, config);
		}
	}

}
