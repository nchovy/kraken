package org.krakenapps.log.api.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.log.api.LoggerEventListener;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryEventListener;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.log.api.LoggerFactoryRegistryEventListener;
import org.krakenapps.log.api.LoggerRegistry;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.osgi.service.prefs.PreferencesService;

@Component(name = "logger-loader")
public class LoggerLoader implements LoggerFactoryRegistryEventListener, LoggerFactoryEventListener,
		LoggerEventListener {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LoggerLoader.class.getName());

	@Requires
	private PreferencesService prefsvc;

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
		Preferences root = getRootPreferences();
		try {
			String loggerFullname = logger.getFullName();
			if (!root.nodeExists(loggerFullname)) {
				slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
				return;
			}
			Preferences node = root.node(loggerFullname);
			node.putBoolean("logger_running", true);
			node.putInt("logger_interval", logger.getInterval());
			root.flush();
			root.sync();

			slog.trace("kraken log api: running status saved: {}", loggerFullname);
		} catch (BackingStoreException e) {
			slog.warn("kraken log api: running status save failed", e);
		}
	}

	@Override
	public void onStop(Logger logger) {
		Preferences root = getRootPreferences();
		if (root == null)
			return;

		try {
			String loggerFullname = logger.getFullName();
			if (!root.nodeExists(loggerFullname)) {
				slog.warn("kraken log api: config not exists for logger {}", logger.getFullName());
				return;
			}
			Preferences node = root.node(loggerFullname);

			// do not save status caused by bundle stopping
			if (loggerRegistry != null && loggerRegistry.isOpen()) {
				slog.trace("kraken log api: [{}] stopped state saved", loggerFullname);
				node.put("logger_running", Boolean.toString(false));
			}

			node.putLong("log_count", logger.getLogCount());
			if (logger.getLastLogDate() != null)
				node.putLong("last_log_date", logger.getLastLogDate().getTime());

			root.flush();
			root.sync();
		} catch (BackingStoreException e) {
			slog.warn("kraken log api: running status save failed", e);
		}
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
		try {
			Preferences root = getRootPreferences();
			for (String loggerFullname : root.childrenNames()) {
				Preferences loggerNode = root.node(loggerFullname);
				String factoryNamespace = loggerNode.get("factory_namespace", null);
				String factoryName = loggerNode.get("factory_name", null);

				if (loggerFactory.getName().equals(factoryName)
						&& loggerFactory.getNamespace().equals(factoryNamespace))
					tryLoad(loggerFullname);
			}
		} catch (BackingStoreException e) {
			slog.error("kraken log api: cannot read logger config", e);
		}
	}

	@Override
	public void factoryRemoved(LoggerFactory loggerFactory) {
		if (slog.isTraceEnabled())
			slog.trace("kraken log api: removing listener to logger factory [{}]", loggerFactory.getFullName());

		loggerFactory.removeListener(this);
	}

	private void loadLoggers() {
		try {
			Preferences root = getRootPreferences();
			for (String loggerFullname : root.childrenNames()) {
				tryLoad(loggerFullname);
			}
		} catch (BackingStoreException e) {
			slog.error("kraken log api: cannot read logger config", e);
		}
	}

	private void tryLoad(String loggerFullname) {
		Preferences root = getRootPreferences();
		Preferences loggerNode = root.node(loggerFullname);
		String loggerNamespace = loggerNode.get("logger_namespace", null);
		String loggerName = loggerNode.get("logger_name", null);
		String description = loggerNode.get("description", null);
		String factoryNamespace = loggerNode.get("factory_namespace", null);
		String factoryName = loggerNode.get("factory_name", null);
		boolean isRunning = loggerNode.getBoolean("logger_running", false);
		int interval = loggerNode.getInt("logger_interval", -1);
		long logCount = loggerNode.getLong("log_count", 0);
		long lastTime = loggerNode.getLong("last_log_date", 0);
		Date lastLogDate = lastTime != 0 ? new Date(lastTime) : null;

		// check logger factory
		LoggerFactory factory = factoryRegistry.getLoggerFactory(factoryNamespace, factoryName);
		if (factory == null)
			return;

		// if logger already exists
		if (loggerRegistry.getLogger(loggerFullname) != null)
			return;

		Properties config = new Properties();
		loadConfigs(loggerNode, factory, config);

		try {
			Logger newLogger = factory.newLogger(loggerNamespace, loggerName, description, logCount, lastLogDate,
					config);
			slog.info("kraken log api: logger [{}] restored", loggerFullname);
			if (newLogger != null && isRunning && interval != -1) {
				newLogger.start(interval);
				slog.info("kraken log api: logger [{}] started with interval {}ms", loggerFullname, interval);
			}
		} catch (Exception e) {
			try {
				loggerNode.removeNode();
				root.flush();
				root.sync();
			} catch (BackingStoreException e1) {
				slog.warn("kraken log api: logger config deletion failed.", e1);
			}
			slog.error(String.format("kraken log api: cannot load logger %s, saved config deleted.", loggerFullname), e);
		}
	}

	private void loadConfigs(Preferences loggerNode, LoggerFactory factory, Properties configs) {
		try {
			Preferences configPreferences = loggerNode.node("configs");
			Map<String, LoggerConfigOption> optionMap = toMap(factory.getConfigOptions());

			for (String name : configPreferences.keys()) {
				LoggerConfigOption option = optionMap.get(name);
				if (option != null) {
					String type = option.getType();
					if (type.equals("string")) {
						configs.put(name, configPreferences.get(name, null));
					} else if (type.equals("integer")) {
						configs.put(name, Integer.parseInt(configPreferences.get(name, "-1")));
					}
				} else {
					configs.put(name, configPreferences.get(name, null));
				}
			}
		} catch (Exception e) {
			slog.error("kraken log api: logger config loader failed", e);
		}
	}

	private Map<String, LoggerConfigOption> toMap(Collection<LoggerConfigOption> options) {
		Map<String, LoggerConfigOption> optionMap = new HashMap<String, LoggerConfigOption>();

		for (LoggerConfigOption option : options) {
			optionMap.put(option.getName(), option);
		}

		return optionMap;
	}

	private void deleteLoggerConfig(Logger logger) {
		try {
			Preferences root = getRootPreferences();
			String loggerFullname = logger.getFullName();
			if (root.nodeExists(loggerFullname))
				root.node(loggerFullname).removeNode();

			root.flush();
			root.sync();

			slog.info("kraken log api: deleted logger config for [{}]", loggerFullname);
		} catch (BackingStoreException e) {
			slog.error("kraken log api: cannot delete logger config", e);
		}
	}

	private void saveLoggerConfig(Logger logger, Properties config) {
		try {
			Preferences root = getRootPreferences();
			String loggerFullname = logger.getFullName();
			if (root.nodeExists(loggerFullname))
				root.node(loggerFullname).removeNode();

			Preferences node = root.node(loggerFullname);
			node.put("factory_namespace", logger.getFactoryNamespace());
			node.put("factory_name", logger.getFactoryName());
			node.put("logger_namespace", logger.getNamespace());
			node.put("logger_name", logger.getName());
			node.put("description", logger.getDescription());
			node.putBoolean("logger_running", logger.isRunning());
			node.putInt("logger_interval", logger.getInterval());
			node.putLong("log_count", logger.getLogCount());
			if (logger.getLastLogDate() != null)
				node.putLong("last_log_date", logger.getLastLogDate().getTime());

			Preferences configNode = node.node("configs");

			for (Object key : config.keySet()) {
				String name = key.toString();

				if (slog.isDebugEnabled())
					slog.debug("kraken log api: logger name [{}], key [{}], value [{}]", new Object[] {
							logger.getFullName(), name, config.get(name) });

				// in case of optional configurations
				if (config.get(name) == null)
					continue;

				configNode.put(name, config.get(name).toString());
			}

			root.flush();
			root.sync();

			slog.trace("kraken log api: saved logger [{}] config, {}", loggerFullname, logger.isRunning());
		} catch (BackingStoreException e) {
			slog.error("kraken log api: cannot save logger config", e);
		}
	}

	private Preferences getRootPreferences() {
		return prefsvc.getSystemPreferences().node("/loggers");
	}
}
