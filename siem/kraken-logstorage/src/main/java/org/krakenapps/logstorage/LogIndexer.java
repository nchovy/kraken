package org.krakenapps.logstorage;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface LogIndexer {
	void createIndex(LogIndexConfig config);

	void dropIndex(String tableName, String indexName, boolean purgeAll);

	Set<String> getIndexNames();

	List<Date> getIndexedDays(String tableName, String indexName);

	LogIndexConfig getIndexConfig(String name);

	LogIndexCursor search(LogIndexQuery q) throws IOException;
}
