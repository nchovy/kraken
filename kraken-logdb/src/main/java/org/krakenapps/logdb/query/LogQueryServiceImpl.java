/*
 * Copyright 2011 Future Systems
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
package org.krakenapps.logdb.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.logdb.EmptyLogQueryCallback;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryEventListener;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQueryStatus;
import org.krakenapps.logdb.LogScriptRegistry;
import org.krakenapps.logdb.LookupHandlerRegistry;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.LogQueryImpl;
import org.krakenapps.logdb.query.parser.DropParser;
import org.krakenapps.logdb.query.parser.FieldsParser;
import org.krakenapps.logdb.query.parser.FunctionParser;
import org.krakenapps.logdb.query.parser.LookupParser;
import org.krakenapps.logdb.query.parser.OptionParser;
import org.krakenapps.logdb.query.parser.RenameParser;
import org.krakenapps.logdb.query.parser.ScriptParser;
import org.krakenapps.logdb.query.parser.SearchParser;
import org.krakenapps.logdb.query.parser.SortParser;
import org.krakenapps.logdb.query.parser.StatsParser;
import org.krakenapps.logdb.query.parser.TableParser;
import org.krakenapps.logdb.query.parser.TimechartParser;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logdb-query")
@Provides
public class LogQueryServiceImpl implements LogQueryService {
	private final Logger logger = LoggerFactory.getLogger(LogQueryServiceImpl.class);

	@Requires
	private LogStorage logStorage;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private SyntaxProvider syntaxProvider;

	@Requires
	private LookupHandlerRegistry lookupRegistry;

	@Requires
	private LogScriptRegistry scriptRegistry;

	@Requires
	private LogParserFactoryRegistry parserFactoryRegistry;

	private BundleContext bc;
	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();

	private CopyOnWriteArraySet<LogQueryEventListener> callbacks;

	public LogQueryServiceImpl(BundleContext bc) {
		this.bc = bc;
		this.callbacks = new CopyOnWriteArraySet<LogQueryEventListener>();
	}

	@Validate
	public void start() {
		@SuppressWarnings("unchecked")
		List<Class<? extends LogQueryParser>> parserClazzes = Arrays.asList(DropParser.class, SearchParser.class, FieldsParser.class,
				FunctionParser.class, OptionParser.class, RenameParser.class, SortParser.class, StatsParser.class, TimechartParser.class);

		List<LogQueryParser> parsers = new ArrayList<LogQueryParser>();
		for (Class<? extends LogQueryParser> clazz : parserClazzes) {
			try {
				parsers.add(clazz.newInstance());
			} catch (Exception e) {
				logger.error("kraken logstorage: failed to add syntax: " + clazz.getSimpleName(), e);
			}
		}

		// add table and lookup (need some constructor injection)
		TableParser tableParser = new TableParser(logStorage, tableRegistry, parserFactoryRegistry);
		LookupParser lookupParser = new LookupParser(lookupRegistry);
		ScriptParser scriptParser = new ScriptParser(bc, scriptRegistry);

		parsers.add(tableParser);
		parsers.add(lookupParser);
		parsers.add(scriptParser);

		syntaxProvider.addParsers(parsers);

		// receive log table event and register it to data source registry
	}

	@Override
	public LogQuery createQuery(String query) {
		LogQuery lq = new LogQueryImpl(syntaxProvider, query, parserFactoryRegistry);
		queries.put(lq.getId(), lq);
		lq.registerQueryCallback(new EofReceiver(lq));
		invokeCallbacks(lq, LogQueryStatus.Created);

		return lq;
	}

	@Override
	public void startQuery(int id) {
		LogQuery lq = getQuery(id);
		if (lq == null)
			throw new IllegalArgumentException("invalid log query id: " + id);

		new Thread(lq, "Log Query " + id).start();
		invokeCallbacks(lq, LogQueryStatus.Started);
	}

	@Override
	public void removeQuery(int id) {
		LogQuery lq = queries.get(id);
		if (lq == null)
			return;

		if (!lq.isEnd())
			lq.cancel();

		FileBufferList<Map<String, Object>> fbl = (FileBufferList<Map<String, Object>>) lq.getResult();
		if (fbl != null)
			fbl.close();

		queries.remove(id);

		invokeCallbacks(lq, LogQueryStatus.Removed);
	}

	@Override
	public Collection<LogQuery> getQueries() {
		return queries.values();
	}

	@Override
	public LogQuery getQuery(int id) {
		return queries.get(id);
	}

	@Override
	public void addListener(LogQueryEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeListener(LogQueryEventListener listener) {
		callbacks.remove(listener);
	}

	private void invokeCallbacks(LogQuery lq, LogQueryStatus status) {
		for (LogQueryEventListener callback : callbacks) {
			try {
				callback.onQueryStatusChange(lq, status);
			} catch (Exception e) {
				logger.warn("kraken logdb: query event listener should not throw any exception", e);
			}
		}
	}

	private class EofReceiver extends EmptyLogQueryCallback {
		private LogQuery query;

		public EofReceiver(LogQuery query) {
			this.query = query;
		}

		@Override
		public void onEof() {
			invokeCallbacks(query, LogQueryStatus.Eof);
		}
	}
}
