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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logdb.EmptyLogQueryCallback;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryEventListener;
import org.krakenapps.logdb.LogQueryParser;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQueryStatus;
import org.krakenapps.logdb.SyntaxProvider;
import org.krakenapps.logdb.impl.ResourceManager;
import org.krakenapps.logdb.query.parser.DatasourceParser;
import org.krakenapps.logdb.query.parser.DropParser;
import org.krakenapps.logdb.query.parser.EvalParser;
import org.krakenapps.logdb.query.parser.FieldsParser;
import org.krakenapps.logdb.query.parser.FunctionParser;
import org.krakenapps.logdb.query.parser.LookupParser;
import org.krakenapps.logdb.query.parser.OptionCheckerParser;
import org.krakenapps.logdb.query.parser.OptionParser;
import org.krakenapps.logdb.query.parser.RenameParser;
import org.krakenapps.logdb.query.parser.ReplaceParser;
import org.krakenapps.logdb.query.parser.ScriptParser;
import org.krakenapps.logdb.query.parser.SearchParser;
import org.krakenapps.logdb.query.parser.SortParser;
import org.krakenapps.logdb.query.parser.StatsParser;
import org.krakenapps.logdb.query.parser.TableParser;
import org.krakenapps.logdb.query.parser.TermParser;
import org.krakenapps.logdb.query.parser.TextFileParser;
import org.krakenapps.logdb.query.parser.TimechartParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logdb-query-service")
@Provides
public class LogQueryServiceImpl implements LogQueryService {
	private final Logger logger = LoggerFactory.getLogger(LogQueryServiceImpl.class);

	@Requires
	private ResourceManager resman;

	private ConcurrentMap<Integer, LogQuery> queries = new ConcurrentHashMap<Integer, LogQuery>();
	private CopyOnWriteArraySet<LogQueryEventListener> callbacks = new CopyOnWriteArraySet<LogQueryEventListener>();

	@SuppressWarnings("unchecked")
	@Validate
	public void start() {
		File buffer = new File(System.getProperty("kraken.data.dir"), "kraken-logdb/buffer/");
		buffer.mkdirs();
		for (File f : buffer.listFiles()) {
			String name = f.getName();
			if (f.isFile() && (name.startsWith("fbl") || name.startsWith("fbm")) && name.endsWith(".buf"))
				f.delete();
		}
		FileBufferList.setFileDir(buffer);

		List<Class<? extends LogQueryParser>> parsers = Arrays.asList(DatasourceParser.class, DropParser.class, EvalParser.class,
				SearchParser.class, FieldsParser.class, FunctionParser.class, LookupParser.class, OptionCheckerParser.class,
				OptionParser.class, RenameParser.class, ReplaceParser.class, ScriptParser.class, SortParser.class, StatsParser.class,
				TableParser.class, TermParser.class, TextFileParser.class, TimechartParser.class);
		resman.get(SyntaxProvider.class).addParsers(parsers, resman);

		// receive log table event and register it to data source registry
	}

	@Override
	public LogQuery createQuery(String query) {
		LogQuery lq = new LogQueryImpl(resman, query);
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

		lq.start();
		invokeCallbacks(lq, LogQueryStatus.Started);
	}

	@Override
	public void removeQuery(int id) {
		LogQuery lq = queries.get(id);
		if (lq == null)
			return;
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
