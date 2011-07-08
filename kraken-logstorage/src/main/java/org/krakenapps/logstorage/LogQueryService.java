package org.krakenapps.logstorage;

import java.util.Collection;

public interface LogQueryService {
	LogQuery createQuery(String query);

	void removeQuery(int id);

	Collection<LogQuery> getQueries();

	LogQuery getQuery(int id);
}
