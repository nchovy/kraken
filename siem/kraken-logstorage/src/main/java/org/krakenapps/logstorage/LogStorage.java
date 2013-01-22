/*
 * Copyright 2010 NCHOVY
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface LogStorage {
	/**
	 * @return the storage directory
	 */
	File getDirectory();

	/**
	 * @param tableName
	 *            the table name
	 * @return the directory path which contains table files
	 */
	File getTableDirectory(String tableName);

	/**
	 * set storage directory
	 * 
	 * @param f
	 *            the storage directory path
	 */
	void setDirectory(File f);

	LogStorageStatus getStatus();

	void start();

	void stop();

	void createTable(String tableName);

	void createTable(String tableName, Map<String, String> tableMetadata);

	void dropTable(String tableName);

	LogRetentionPolicy getRetentionPolicy(String tableName);

	void setRetentionPolicy(LogRetentionPolicy policy);

	/**
	 * reload parameters
	 */
	void reload();

	void flush();

	Date getPurgeBaseline(String tableName);

	void purge(String tableName, Date fromDay, Date toDay);

	Collection<Date> getLogDates(String tableName);

	void write(Log log);

	void write(Collection<Log> logs);

	Collection<Log> getLogs(String tableName, Date from, Date to, int limit);

	Collection<Log> getLogs(String tableName, Date from, Date to, int offset, int limit);

	CachedRandomSeeker openCachedRandomSeeker();

	Log getLog(LogKey logKey);

	Log getLog(String tableName, Date date, int id);

	LogCursor openCursor(String tableName, Date day, boolean ascending) throws IOException;

	int search(Date from, Date to, int limit, LogSearchCallback callback) throws InterruptedException;

	int search(Date from, Date to, int offset, int limit, LogSearchCallback callback) throws InterruptedException;

	int search(String tableName, Date from, Date to, int limit, LogSearchCallback callback) throws InterruptedException;

	int search(String tableName, Date from, Date to, int offset, int limit, LogSearchCallback callback)
			throws InterruptedException;

	void addLogListener(LogCallback callback);

	void removeLogListener(LogCallback callback);

	List<LogWriterStatus> getWriterStatuses();
}
