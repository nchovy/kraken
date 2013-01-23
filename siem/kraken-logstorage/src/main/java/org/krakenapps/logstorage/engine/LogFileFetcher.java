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
package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.file.LogFileReader;

/**
 * 
 * @author xeraph
 * @since 0.9
 */
class LogFileFetcher {
	private LogTableRegistry tableRegistry;

	public LogFileFetcher(LogTableRegistry tableRegistry) {
		this.tableRegistry = tableRegistry;
	}

	public LogFileReader fetch(String tableName, Date day) throws IOException {
		int tableId = tableRegistry.getTableId(tableName);

		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		if (!indexPath.exists())
			throw new IllegalStateException("log table not found: " + tableName + ", " + day);

		File dataPath = DatapathUtil.getDataFile(tableId, day);
		if (!dataPath.exists())
			throw new IllegalStateException("log table not found: " + tableName + ", " + day);

		return LogFileReader.getLogFileReader(indexPath, dataPath);

	}
}
