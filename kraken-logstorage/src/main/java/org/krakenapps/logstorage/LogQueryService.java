package org.krakenapps.logstorage;

import java.util.Collection;

public interface LogQueryService {
	LogQuery createQuery(String query);

	void removeQuery(int id);

	Collection<LogQuery> getQueries();

	LogQuery getQuery(int id);

	void addLookupHandler(String name, LookupHandler handler);

	LookupHandler getLookupHandler(String name);

	void removeLookupHandler(String name);
}
