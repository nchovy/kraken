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

import java.util.Collection;
import java.util.Date;

import org.krakenapps.logstorage.criterion.Criterion;

public interface LogStorage {
	LogStorageStatus getStatus();

	void start();

	void stop();

	void createTable(String tableName);

	void dropTable(String tableName);

	/**
	 * reload parameters
	 */
	void reload();

	void flush();

	Collection<Date> getLogDates(String tableName);

	void write(Log log);

	void write(Collection<Log> logs);

	Log getLog(LogKey logKey);

	Log getLog(String tableName, Date date, int id);

	int search(Date from, Date to, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException;

	int search(Date from, Date to, int offset, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException;

	int search(String tableName, Date from, Date to, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException;

	int search(String tableName, Date from, Date to, int offset, int limit, Criterion pred, LogSearchCallback callback)
			throws InterruptedException;

	void addLogListener(LogCallback callback);

	void removeLogListener(LogCallback callback);
}
