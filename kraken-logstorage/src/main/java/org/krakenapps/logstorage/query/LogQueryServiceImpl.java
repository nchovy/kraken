package org.krakenapps.logstorage.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;

@Component(name = "log-query-service")
@Provides
public class LogQueryServiceImpl implements LogQueryService {
	@Requires
	private LogStorage logStorage;

	@Requires
	private LogTableRegistry tableRegistry;

	private Map<Integer, LogQuery> queries = new HashMap<Integer, LogQuery>();

	@Override
	public LogQuery createQuery(String query) {
		LogQuery lq = new LogQueryImpl(logStorage, tableRegistry, query);
		queries.put(lq.getId(), lq);
		return lq;
	}

	@Override
	public void removeQuery(int id) {
		LogQuery lq = queries.get(id);
		if (lq != null)
			lq.cancel();
		queries.remove(id);
	}

	@Override
	public Collection<LogQuery> getQueries() {
		return queries.values();
	}

	@Override
	public LogQuery getQuery(int id) {
		return queries.get(id);
	}
}
