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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.jpa.ThreadLocalEntityManagerService;
import org.krakenapps.jpa.handler.JpaConfig;
import org.krakenapps.jpa.handler.Transactional;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogNormalizer;
import org.krakenapps.log.api.LogNormalizerRegistry;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerRegistry;
import org.krakenapps.log.api.LoggerRegistryEventListener;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.criterion.Criterion;
import org.krakenapps.siem.LogServer;
import org.krakenapps.siem.NormalizedLog;
import org.krakenapps.siem.NormalizedLogListener;
import org.krakenapps.siem.model.LogParserOption;
import org.krakenapps.siem.model.ManagedLogger;

@Component(name = "siem-log-server")
@JpaConfig(factory = "siem")
@Provides
public class LogServerEngine implements LogServer, LogPipe, LoggerRegistryEventListener {
	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LogServerEngine.class.getName());

	@Requires
	private ThreadLocalEntityManagerService entityManagerService;

	@Requires
	private LoggerRegistry loggerRegistry;

	@Requires
	private LogParserFactoryRegistry parserFactoryRegistry;

	@Requires
	private LogNormalizerRegistry normalizerRegistry;

	@Requires
	private LogStorage logStorage;

	private ConcurrentMap<String, CopyOnWriteArrayList<NormalizedLogListener>> normalizedLogCallbacks;

	@Validate
	public void start() {
		normalizedLogCallbacks = new ConcurrentHashMap<String, CopyOnWriteArrayList<NormalizedLogListener>>();

		// add logger registration monitor
		loggerRegistry.addListener(this);

		// get all managed loggers and connect log pipes
		for (ManagedLogger m : getManagedLoggers()) {
			Logger logger = loggerRegistry.getLogger(m.getFullName());
			if (logger == null)
				continue;

			logger.addLogPipe(this);
		}
	}

	@Invalidate
	public void stop() {
		loggerRegistry.removeListener(this);
	}

	@Override
	public int search(Date from, Date to, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException {
		return logStorage.search(from, to, limit, pred, callback);
	}

	@Override
	public int search(Date from, Date to, int offset, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException {
		return logStorage.search(from, to, offset, limit, pred, callback);
	}

	@Override
	public int search(String fullName, Date from, Date to, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException {
		return logStorage.search(fullName, from, to, limit, pred, callback);
	}

	@Override
	public int search(String fullName, Date from, Date to, int offset, int limit, Criterion pred,
			LogSearchCallback callback) throws InterruptedException {
		return logStorage.search(fullName, from, to, offset, limit, pred, callback);
	}

	@Override
	public void write(org.krakenapps.logstorage.Log log) {
		logStorage.write(log);
	}

	@Transactional
	@Override
	public ManagedLogger getManagedLogger(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		return em.find(ManagedLogger.class, id);
	}

	@Transactional
	@Override
	public ManagedLogger getManagedLogger(String fullName) {
		try {
			EntityManager em = entityManagerService.getEntityManager();
			return (ManagedLogger) em.createQuery("FROM ManagedLogger m WHERE m.fullName = ?")
					.setParameter(1, fullName).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public Collection<ManagedLogger> getManagedLoggers() {
		EntityManager em = entityManagerService.getEntityManager();
		return em.createQuery("FROM ManagedLogger").getResultList();
	}

	@Transactional
	@Override
	public int createManagedLogger(int organizationId, String fullName, String parserName, Properties parserOptions) {
		Logger logger = loggerRegistry.getLogger(fullName);
		if (logger == null)
			throw new IllegalStateException("logger not found: " + fullName);

		EntityManager em = entityManagerService.getEntityManager();

		// check if duplicated managed logger exists
		int size = em.createQuery("FROM ManagedLogger l WHERE l.organizationId = ? AND l.fullName = ?")
				.setParameter(1, organizationId).setParameter(2, fullName).getResultList().size();
		if (size > 0)
			throw new IllegalStateException("duplicated managed logger exists: " + fullName);

		ManagedLogger m = new ManagedLogger();
		m.setOrganizationId(organizationId);
		m.setFullName(fullName);
		m.setParserFactoryName(parserName);
		m.setEnabled(true);
		em.persist(m);

		for (Object k : parserOptions.keySet()) {
			LogParserOption option = new LogParserOption();
			option.setName((String) k);
			option.setValue(parserOptions.getProperty((String) k));

			option.setManagedLogger(m);
			m.getLogParserOptions().add(option);

			em.persist(option);
		}
		
		em.merge(m);

		// create log table
		logStorage.createTable(m.getFullName());

		// connect pipe
		logger.addLogPipe(this);

		return m.getId();
	}

	@Transactional
	@Override
	public void removeManagedLogger(int id) {
		EntityManager em = entityManagerService.getEntityManager();
		ManagedLogger m = em.find(ManagedLogger.class, id);
		if (m == null)
			return;

		// disconnect pipe
		Logger logger = loggerRegistry.getLogger(m.getFullName());
		if (logger != null)
			logger.removeLogPipe(this);

		// remove configuration
		em.remove(m);

		// drop log table
		logStorage.dropTable(m.getFullName());
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

		ManagedLogger ml = getManagedLogger(fullName);
		if (ml == null) {
			slog.trace("kraken siem: managed logger not found, {}", fullName);
			return;
		}

		String parserFactoryName = ml.getParserFactoryName();
		if (parserFactoryName == null) {
			slog.debug("kraken siem: parser name not found, {}", parserFactoryName);
			return;
		}

		LogParserFactory factory = parserFactoryRegistry.get(parserFactoryName);
		if (factory == null) {
			slog.trace("kraken siem: parser factory not found, {}", parserFactoryName);
			return;
		}

		Properties config = convert(ml.getLogParserOptions());
		LogParser parser = factory.createParser(config);

		if (parser == null) {
			slog.trace("kraken siem: parser not created, {}", parserFactoryName);
			return;
		}

		Map<String, Object> parsed = parser.parse(log.getParams());
		if (parsed == null) {
			slog.debug("kraken siem: parser returned null");
			return;
		}

		String type = (String) parsed.get("logtype");
		if (type == null) {
			slog.debug("kraken siem: logtype not found");
			return;
		}

		LogNormalizer normalizer = normalizerRegistry.get(type);
		if (normalizer == null) {
			slog.trace("kraken siem: normalizer [{}] not found", type);
			return;
		}

		Map<String, Object> normalized = normalizer.normalize(parsed);
		if (normalized == null) {
			slog.debug("kraken siem: normalizer returned null");
			return;
		}

		NormalizedLog normalizedLog = new NormalizedLog(ml.getOrganizationId(), normalized);
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
				callback.onLog(normalizedLog);
			} catch (Exception e) {
				slog.warn("kraken siem: normalized log listener callback should not throw any exception", e);
			}
		}
	}

	private Properties convert(List<LogParserOption> options) {
		Properties p = new Properties();
		for (LogParserOption o : options)
			p.put(o.getName(), o.getValue());

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
		// check if it is managed loggers
		ManagedLogger m = getManagedLogger(logger.getFullName());
		if (m != null) {
			logger.addLogPipe(this);
		}
	}

	@Override
	public void loggerRemoved(Logger logger) {
		logger.removeLogPipe(this);
	}
}
