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
package org.krakenapps.logdb.query.command;

import java.util.Map;

import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logstorage.CachedRandomSeeker;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexItem;
import org.krakenapps.logstorage.LogIndexQuery;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.9
 * @author xeraph
 */
public class Fulltext extends LogQueryCommand {
	private final Logger logger = LoggerFactory.getLogger(Fulltext.class);
	private LogStorage storage;
	private LogIndexer indexer;
	private LogIndexQuery query;

	public Fulltext(LogStorage storage, LogIndexer indexer, LogIndexQuery query) {
		this.storage = storage;
		this.indexer = indexer;
		this.query = query;
	}

	@Override
	public void push(LogMap m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	@Override
	public void start() {
		LogIndexCursor cursor = null;
		CachedRandomSeeker seeker = null;
		try {
			status = Status.Running;
			cursor = indexer.search(query);
			seeker = storage.openCachedRandomSeeker();

			while (cursor.hasNext()) {
				if (status.equals(Status.End))
					break;

				LogIndexItem item = cursor.next();
				Log log = seeker.getLog(item.getTableName(), item.getDay(), (int) item.getLogId());
				if (log == null) {
					logger.error("kraken logdb: cannot find indexed log for table [{}], day [{}], id [{}]",
							new Object[] { item.getTableName(), item.getDay(), item.getLogId() });
					continue;
				}

				Map<String, Object> m = log.getData();
				m.put("_table", log.getTableName());
				m.put("_id", log.getId());
				m.put("_time", log.getDate());
				write(new LogMap(log.getData()));
			}

		} catch (Exception e) {
			logger.error("kraken logdb: fulltext exception", e);
		} catch (Error e) {
			logger.error("kraken logdb: fulltext error", e);
		} finally {
			if (cursor != null)
				cursor.close();
			if (seeker != null)
				seeker.close();
		}
		eof();
	}
}
