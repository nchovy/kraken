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
package org.krakenapps.siem.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigCollection;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigIterator;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.log.api.LogNormalizerFactory;
import org.krakenapps.log.api.LogNormalizerFactoryRegistry;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.LoggerRegistryEventListener;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.siem.ConfigManager;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.NormalizedLog;
import org.krakenapps.siem.NormalizedLogListener;
import org.krakenapps.siem.model.ManagedLogger;

@Component(name = "siem-log-server")
@Provides
public class LogServerEngine implements LogServer, LogPipe, LoggerRegistryEventListener {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LogServerEngine.class.getName());

	@Requires
	private ConfigManager configManager;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LogParserFactoryRegistry parserFactoryRegistry;

	@Requires
	private LogNormalizerFactoryRegistry normalizerFactoryRegistry;

	@Requires
	private LogTableRegistry logTableRegistry;

	@Requires
	private LogStorage logStorage;

	private ConcurrentMap<String, CopyOnWriteArrayList<NormalizedLogListener>> normalizedLogCallbacks;

	/**
	 * logger fullname to managed logger mappings. you should RELOAD server
	 * after logger metadata update
	 */
	private ConcurrentMap<String, ManagedLogger> managedLoggers;

	/**
	 * logger fullname to log parser mappings
	 */
	private ConcurrentMap<String, LogParser> parsers;

	/**
	 * logger fullname to log normalizer mappings
	 */
	private ConcurrentMap<String, LogNormalizer> normalizers;

	@Validate
	public void start() {
		normalizedLogCallbacks = new ConcurrentHashMap<String, CopyOnWriteArrayList<NormalizedLogListener>>();

		managedLoggers = new ConcurrentHashMap<String, ManagedLogger>();
		parsers = new ConcurrentHashMap<String, LogParser>();
		normalizers = new ConcurrentHashMap<String, LogNormalizer>();

		// add logger registration monitor
		loggerRegistry.addListener(this);

		// get all managed loggers and connect log pipes
		for (ManagedLogger m : getManagedLoggers()) {
			Logger logger = loggerRegistry.getLogger(m.getFullName());
			if (logger == null)
				continue;

			connectManagedLogger(logger);
		}
	}

	@Invalidate
	public void stop() {
		if (loggerRegistry == null)
			return;

		// disconnect all pipes
		for (Logger logger : loggerRegistry.getLoggers())
			disconnectManagedLogger(logger);

		loggerRegistry.removeListener(this);
	}

	@Override
	public void write(org.krakenapps.logstorage.Log log) {
		logStorage.write(log);
	}

	@Override
	public ManagedLogger getManagedLogger(String fullName) {
		ConfigCollection col = getCol();
		Config c = col.findOne(Predicates.field("full_name", fullName));
		if (c == null)
			return null;

		return PrimitiveConverter.parse(ManagedLogger.class, c.getDocument());
	}

	@Override
	public Collection<ManagedLogger> getManagedLoggers() {
		ConfigCollection col = getCol();
		ConfigIterator it = col.findAll();
		try {
			List<ManagedLogger> loggers = new ArrayList<ManagedLogger>();
			while (it.hasNext())
				loggers.add(PrimitiveConverter.parse(ManagedLogger.class, it.next().getDocument()));

			return loggers;
		} finally {
			if (it != null)
				it.close();
		}
	}

	@Override
	public void createManagedLogger(ManagedLogger ml) {
		Logger logger = loggerRegistry.getLogger(ml.getFullName());
		if (logger == null)
			throw new IllegalStateException("logger not found: " + ml.getFullName());

		// check if duplicated managed logger exists
		ConfigCollection col = getCol();
		Config c = col.findOne(Predicates.field("full_name", ml.getFullName()));
		if (c != null)
			throw new IllegalStateException("duplicated managed logger exists: " + ml.getFullName());

		col.add(PrimitiveConverter.serialize(ml));

		// create log table
		logStorage.createTable(ml.getFullName(), ml.getMetadata());

		// connect pipe
		connectManagedLogger(logger);
	}

	@Override
	public void removeManagedLogger(ManagedLogger ml) {
		ConfigCollection col = getCol();
		Config c = col.findOne(Predicates.field("full_name", ml.getFullName()));

		if (c == null)
			return;

		// disconnect pipe
		Logger logger = loggerRegistry.getLogger(ml.getFullName());
		if (logger != null)
			disconnectManagedLogger(logger);

		// remove configuration
		col.remove(c);

		// drop log table
		logStorage.dropTable(ml.getFullName());
	}

	@Override
	public void addNormalizedLogListener(String category, NormalizedLogListener callback) {
		CopyOnWriteArrayList<NormalizedLogListener> callbacks = new CopyOnWriteArrayList<NormalizedLogListener>();
		CopyOnWriteArrayList<NormalizedLogListener> old = normalizedLogCallbacks.putIfAbsent(category, callbacks);
		if (old != null)
			callbacks = old;

		callbacks.add(callback);
	}

	@Override
	public void removeNormalizedLogListener(String category, NormalizedLogListener callback) {
		CopyOnWriteArrayList<NormalizedLogListener> callbacks = normalizedLogCallbacks.get(category);
		if (callbacks != null)
			callbacks.remove(callback);
	}

	//
	// LogPipe callbacks
	//

	@Override
	public void onLog(Logger logger, Log log) {
		String fullName = logger.getFullName();
		logStorage.write(convert(fullName, log));

		ManagedLogger ml = managedLoggers.get(fullName);
		if (ml == null) {
			slog.trace("kraken siem: managed logger not found, {}", fullName);
			return;
		}

		LogParser parser = parsers.get(fullName);
		if (parser == null) {
			slog.trace("kraken siem: parser not found for logger [{}]", fullName);
			return;
		}

		Map<String, Object> parsed = parser.parse(log.getParams());
		if (parsed == null) {
			slog.debug("kraken siem: parser returned null");
			return;
		}

		LogNormalizer normalizer = normalizers.get(fullName);
		if (normalizer == null) {
			slog.trace("kraken siem: normalizer not found for logger [{}]", fullName);
			return;
		}

		Map<String, Object> normalized = normalizer.normalize(parsed);
		if (normalized == null) {
			slog.debug("kraken siem: normalizer returned null");
			return;
		}

		NormalizedLog normalizedLog = new NormalizedLog(ml.getOrgDomain(), normalized);
		String category = (String) normalizedLog.get("category");
		if (category == null) {
			slog.debug("kraken siem: normalization category not found");
			return;
		}

		CopyOnWriteArrayList<NormalizedLogListener> callbacks = normalizedLogCallbacks.get(category);
		if (callbacks == null)
			return;

		for (NormalizedLogListener callback : callbacks) {
			try {
				if (slog.isTraceEnabled())
					slog.trace("kraken siem: normalized log [{}]", normalizedLog);

				callback.onLog(normalizedLog);
			} catch (Exception e) {
				slog.warn("kraken siem: normalized log listener callback should not throw any exception", e);
			}
		}
	}

	private Properties getTableMetadata(String tableName) {
		Set<String> keys = logTableRegistry.getTableMetadataKeys(tableName);

		Properties p = new Properties();
		for (String key : keys)
			p.put(key, logTableRegistry.getTableMetadata(tableName, key));

		return p;
	}

	private org.krakenapps.logstorage.Log convert(String fullName, Log log) {
		return new org.krakenapps.logstorage.Log(fullName, log.getDate(), log.getParams());
	}

	//
	// LoggerRegistryEventListener 1
	//

	@Override
	public void loggerAdded(Logger logger) {
		connectManagedLogger(logger);
	}

	@Override
	public void loggerRemoved(Logger logger) {
		disconnectManagedLogger(logger);
	}

	private void connectManagedLogger(Logger logger) {
		// check if it is managed loggers
		String fullName = logger.getFullName();
		ManagedLogger ml = getManagedLogger(fullName);
		if (ml == null)
			return;

		// connect log pipe
		managedLoggers.put(fullName, ml);
		logger.addLogPipe(this);

		// create log parser
		Properties config = getTableMetadata(fullName);
		String parserFactoryName = config.getProperty("logparser");
		if (parserFactoryName == null) {
			slog.debug("kraken siem: parser name [{}] not found for logger [{}]", parserFactoryName, fullName);
			return;
		}

		LogParserFactory parserFactory = parserFactoryRegistry.get(parserFactoryName);
		if (parserFactory == null) {
			slog.trace("kraken siem: parser factory [{}] not found for logger [{}]", parserFactoryName, fullName);
			return;
		}

		LogParser parser = parserFactory.createParser(config);
		parsers.put(fullName, parser);

		// create log normalizer
		String normalizerName = config.getProperty("normalizer");
		if (normalizerName == null) {
			slog.debug("kraken siem: normalizer name [{}] not found for logger [{}]", normalizerName, fullName);
			return;
		}

		LogNormalizerFactory normalizerFactory = normalizerFactoryRegistry.get(normalizerName);
		if (normalizerFactory == null) {
			slog.trace("kraken siem: normalizer factory [{}] not found for logger [{}]", normalizerName, fullName);
			return;
		}

		LogNormalizer normalizer = normalizerFactory.createNormalizer(config);
		normalizers.put(fullName, normalizer);
	}

	private void disconnectManagedLogger(Logger logger) {
		String fullName = logger.getFullName();
		managedLoggers.remove(fullName);
		logger.removeLogPipe(this);
		normalizers.remove(fullName);
		parsers.remove(fullName);
	}

	private ConfigCollection getCol() {
		ConfigDatabase db = configManager.getDatabase();
		ConfigCollection col = db.ensureCollection("managed_logger");
		return col;
	}

}
