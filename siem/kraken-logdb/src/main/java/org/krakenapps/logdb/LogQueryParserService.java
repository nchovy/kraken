package org.krakenapps.logdb;

public interface LogQueryParserService {
	LogQuery parse(LogQueryContext context, String queryString);

	void addCommandParser(LogQueryCommandParser parser);

	void removeCommandParser(LogQueryCommandParser parser);
}
