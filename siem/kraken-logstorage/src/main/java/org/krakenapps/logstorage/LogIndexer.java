/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logstorage;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @since 0.9
 * @author xeraph
 */
public interface LogIndexer {
	void createIndex(LogIndexSchema config);

	void dropIndex(String tableName, String indexName);

	void dropAllIndexes(String tableName);

	Set<String> getIndexNames(String tableName);

	LogIndexSchema getIndexConfig(String tableName, String indexName);

	LogIndexCursor search(LogIndexQuery q) throws IOException;

	List<LogIndexerStatus> getIndexerStatuses();

	List<Date> getIndexedDays(String tableName, String indexName);

	List<BatchIndexingTask> getBatchIndexingTasks();

	File getIndexDirectory(String tableName, String indexName);

	Date getPurgeBaseline(String tableName, String indexName);

	void purge(String tableName, String indexName, Date fromDay, Date toDay);
}
