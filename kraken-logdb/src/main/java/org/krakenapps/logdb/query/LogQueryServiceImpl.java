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
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryEventListener;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LookupHandler;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.LogQueryImpl;
import org.krakenapps.logdb.query.parser.DropParser;
import org.krakenapps.logdb.query.parser.FieldsParser;
import org.krakenapps.logdb.query.parser.FunctionParser;
import org.krakenapps.logdb.query.parser.LookupParser;
import org.krakenapps.logdb.query.parser.OptionParser;
import org.krakenapps.logdb.query.parser.QueryParser;
import org.krakenapps.logdb.query.parser.RenameParser;
import org.krakenapps.logdb.query.parser.SearchParser;
import org.krakenapps.logdb.query.parser.SortParser;
import org.krakenapps.logdb.query.parser.StatsParser;
import org.krakenapps.logdb.query.parser.TableParser;
import org.krakenapps.logdb.query.parser.TimechartParser;
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

	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();

	private Map<String, LookupHandler> lookupHandlers = new HashMap<String, LookupHandler>();

	private CopyOnWriteArraySet<LogQueryEventListener> callbacks;

	public LogQueryServiceImpl() {
		this.callbacks = new CopyOnWriteArraySet<LogQueryEventListener>();
	}

	@Validate
	public void start() {
		@SuppressWarnings("unchecked")
		List<Class<? extends QueryParser>> parserClazzes = Arrays.asList(DropParser.class, SearchParser.class,
				FieldsParser.class, FunctionParser.class, OptionParser.class, RenameParser.class, SortParser.class,
				StatsParser.class, TimechartParser.class);

		List<QueryParser> parsers = new ArrayList<QueryParser>();
		for (Class<? extends QueryParser> clazz : parserClazzes) {
			try {
				parsers.add(clazz.newInstance());
			} catch (Exception e) {
				logger.error("kraken logstorage: failed to add syntax: " + clazz.getSimpleName(), e);
			}
		}

		// add table and lookup (need some constructor injection)
		TableParser tableParser = new TableParser(logStorage, tableRegistry);
		LookupParser lookupParser = new LookupParser(this);

		parsers.add(tableParser);
		parsers.add(lookupParser);

		syntaxProvider.addParsers(parsers);

		// receive log table event and register it to data source registry
	}

	@Override
	public LogQuery createQuery(String query) {
		LogQuery lq = new LogQueryImpl(syntaxProvider, this, logStorage, tableRegistry, query);
		queries.put(lq.getId(), lq);

		// invoke callbacks
		for (LogQueryEventListener callback : callbacks) {
			try {
				callback.onCreate(lq);
			} catch (Exception e) {
				logger.warn("kraken logdb: query event listener should not throw any exception", e);
			}
		}

		return lq;
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

		// invoke callbacks
		for (LogQueryEventListener callback : callbacks) {
			try {
				callback.onRemove(lq);
			} catch (Exception e) {
				logger.warn("kraken logdb: query event listener should not throwy any exception", e);
			}
		}
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
	public void addLookupHandler(String name, LookupHandler handler) {
		lookupHandlers.put(name, handler);
	}

	@Override
	public LookupHandler getLookupHandler(String name) {
		return lookupHandlers.get(name);
	}

	@Override
	public void removeLookupHandler(String name) {
		lookupHandlers.remove(name);
	}

	@Override
	public void addListener(LogQueryEventListener listener) {
		callbacks.add(listener);
	}

	@Override
	public void removeListener(LogQueryEventListener listener) {
		callbacks.remove(listener);
	}
}
