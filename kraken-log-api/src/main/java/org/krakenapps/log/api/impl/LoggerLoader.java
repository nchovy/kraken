package org.krakenapps.log.api.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.FieldOption;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.CollectionName;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerEventListener;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryEventListener;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerFactoryRegistryEventListener;
import org.krakenapps.log.api.LoggerRegistry;

@Component(name = "logger-loader")
public class LoggerLoader implements LoggerFactoryRegistryEventListener, LoggerFactoryEventListener, LoggerEventListener {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LoggerLoader.class.getName());

	@Requires
	private ConfigService conf;

	@Requires
	private LoggerFactoryRegistry factoryRegistry;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Validate
	public void start() {
		factoryRegistry.addListener(this);

		for (LoggerFactory factory : factoryRegistry.getLoggerFactories()) {
			factoryAdded(factory);
		}

		loadLoggers();
		slog.trace("kraken log api: logger loading completed");
	}

	@Invalidate
	public void stop() {
		if (factoryRegistry != null)
			factoryRegistry.removeListener(this);
	}

	@Override
	public void loggerCreated(LoggerFactory factory, Logger logger, Properties config) {
		logger.addEventListener(this);
		saveLoggerConfig(logger, config);
	}

	@Override
	public void loggerDeleted(LoggerFactory factory, Logger logger) {
		slog.trace("kraken log api: deleting logger [{}] factory [{}]", logger.getFullName(), factory.getFullName());
		if (logger != null) {
			logger.removeEventListener(this);
			loggerRegistry.removeLogger(logger);
			deleteLoggerConfig(logger);
		}
	}

	@Override
	public void onStart(Logger logger) {
		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		Config c = db.findOne(LoggerModel.class, Predicates.field("fullname", logger.getFullName()));
		if (c == null) {
			slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
			return;
		}

		LoggerModel model = c.getDocument(LoggerModel.class);
		model.isRunning = true;
		model.interval = logger.getInterval();
		db.update(c, model);

		slog.trace("kraken log api: running status saved: {}", logger.getFullName());
	}

	@Override
	public void onStop(Logger logger) {
		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		Config c = db.findOne(LoggerModel.class, Predicates.field("fullname", logger.getFullName()));
		if (c == null) {
			slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
			return;
		}

		LoggerModel model = c.getDocument(LoggerModel.class);
		// do not save status caused by bundle stopping
		if (loggerRegistry != null && loggerRegistry.isOpen()) {
			slog.trace("kraken log api: [{}] stopped state saved", logger.getFullName());
			model.isRunning = false;
		}
		model.count = logger.getLogCount();
		model.lastLogDate = logger.getLastLogDate();
		db.update(c, model);
	}

	@Override
	public void onUpdated(Logger logger, Properties config) {
		saveLoggerConfig(logger, config);
	}

	@Override
	public void factoryAdded(LoggerFactory loggerFactory) {
		if (slog.isTraceEnabled())
			slog.trace("kraken log api: adding listener to logger factory [{}]", loggerFactory.getFullName());

		loggerFactory.addListener(this);

		// find related logger configs and load all loggers
		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		for (LoggerModel model : db.findAll(LoggerModel.class).getDocuments(LoggerModel.class)) {
			slog.info("kraken log api: factory added > model [{}]", model);
			String factoryName = model.factoryName;
			String factoryNamespace = model.factoryNamespace;
			if (loggerFactory.getName().equals(factoryName) && loggerFactory.getNamespace().equals(factoryNamespace))
				tryLoad(model);
		}
	}

	@Override
	public void factoryRemoved(LoggerFactory loggerFactory) {
		if (slog.isTraceEnabled())
			slog.trace("kraken log api: removing listener to logger factory [{}]", loggerFactory.getFullName());

		loggerFactory.removeListener(this);
	}

	private void loadLoggers() {
		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		for (LoggerModel model : db.findAll(LoggerModel.class).getDocuments(LoggerModel.class)) {
			tryLoad(model);
		}
	}

	private void tryLoad(LoggerModel model) {
		// check logger factory
		LoggerFactory factory = factoryRegistry.getLoggerFactory(model.factoryNamespace, model.factoryName);
		if (factory == null)
			return;

		// if logger already exists
		if (loggerRegistry.getLogger(model.fullname) != null)
			return;

		try {
			Logger newLogger = factory.newLogger(model.namespace, model.name, model.description, model.count, model.lastLogDate,
					model.getConfigs());
			slog.info("kraken log api: logger [{}] restored", model.fullname);
			if (newLogger != null && model.isRunning && model.interval != -1) {
				newLogger.start(model.interval);
				slog.info("kraken log api: logger [{}] started with interval {}ms", model.fullname, model.interval);
			}
		} catch (Exception e) {
			slog.error(String.format("kraken log api: cannot load logger %s, saved config deleted.", model.fullname), e);
		}
	}

	private void deleteLoggerConfig(Logger logger) {
		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		Config c = db.findOne(LoggerModel.class, Predicates.field("fullname", logger.getFullName()));
		if (c != null)
			db.remove(c);
		slog.info("kraken log api: deleted logger config for [{}]", logger.getFullName());
	}

	private void saveLoggerConfig(Logger logger, Properties config) {
		LoggerModel model = new LoggerModel(logger);
		model.setConfigs(config);

		ConfigDatabase db = conf.ensureDatabase("kraken-log-api");
		Config c = db.findOne(LoggerModel.class, Predicates.field("fullname", logger.getFullName()));
		if (c == null)
			db.add(model);
		else {
			if (!PrimitiveConverter.serialize(model).equals(c.getDocument()))
				db.update(c, model);
		}

		slog.trace("kraken log api: saved logger [{}] config, {}", logger.getFullName(), logger.isRunning());
	}

	@CollectionName("logger")
	private static class LoggerModel {
		private String factoryNamespace;
		private String factoryName;
		private String namespace;
		private String fullname;
		private String name;
		private String description;
		private boolean isRunning;
		private int interval;

		@FieldOption(skip = true)
		private long count;

		@FieldOption(skip = true)
		private Date lastLogDate;

		private Map<String, Object> configs = new HashMap<String, Object>();

		@SuppressWarnings("unused")
		public LoggerModel() {
		}

		public LoggerModel(Logger logger) {
			this.factoryNamespace = logger.getFactoryNamespace();
			this.factoryName = logger.getFactoryName();
			this.namespace = logger.getNamespace();
			this.fullname = logger.getFullName();
			this.name = logger.getName();
			this.description = logger.getDescription();
			this.isRunning = logger.isRunning();
			this.interval = logger.getInterval();
			this.count = logger.getLogCount();
			this.lastLogDate = logger.getLastLogDate();
		}

		public void setConfigs(Properties configs) {
			for (Object key : configs.keySet())
				this.configs.put(key.toString(), configs.getProperty((String) key));
		}

		public Properties getConfigs() {
			Properties configs = new Properties();
			for (String key : this.configs.keySet())
				configs.put(key, this.configs.get(key));
			return configs;
		}
	}
}
