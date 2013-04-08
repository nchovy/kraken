/*
 * Copyright 2011 Future Systems
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

import java.util.Date;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table extends LogQueryCommand {
	private Logger logger = LoggerFactory.getLogger(Table.class);
	private LogStorage storage;
	private String tableName;
	private int offset;
	private int limit;
	private Date from;
	private Date to;
	private LogParser parser;

	public Table(String tableName) {
		this(tableName, 0);
	}

	public Table(String tableName, int limit) {
		this(tableName, limit, null, null);
	}

	public Table(String tableName, Date from, Date to) {
		this(tableName, 0, from, to);
	}

	public Table(String tableName, int limit, Date from, Date to) {
		this(tableName, 0, 0, from, to);
	}

	public Table(String tableName, int offset, int limit, Date from, Date to) {
		this(tableName, offset, limit, from, to, null);
	}

	public Table(String tableName, int offset, int limit, Date from, Date to, LogParser parser) {
		this.tableName = tableName;
		this.offset = offset;
		this.limit = limit;
		this.from = from;
		this.to = to;
		this.parser = parser;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public LogStorage getStorage() {
		return storage;
	}

	public void setStorage(LogStorage storage) {
		this.storage = storage;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public void start() {
		try {
			status = Status.Running;
			storage.search(tableName, from, to, offset, limit, new LogSearchCallbackImpl());
		} catch (InterruptedException e) {
			logger.trace("kraken logdb: query interrupted");
		} catch (Exception e) {
			logger.error("kraken logdb: table exception", e);
		} catch (Error e) {
			logger.error("kraken logdb: table error", e);
		}
		eof();
	}

	@Override
	public void push(LogMap m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	private class LogSearchCallbackImpl implements LogSearchCallback {
		@Override
		public void onLog(Log log) {
			Map<String, Object> m = null;

			if (parser != null) {
				Map<String, Object> parsed = parser.parse(log.getData());
				if (parsed != null) {
					parsed.put("_table", log.getTableName());
					parsed.put("_id", log.getId());
					parsed.put("_time", log.getDate());
					m = parsed;
				} else {
					logger.debug("kraken logdb: cannot parse log [{}]", log.getData());
					return;
				}
			} else {
				m = log.getData();
				m.put("_table", log.getTableName());
				m.put("_id", log.getId());
				m.put("_time", log.getDate());
			}

			write(new LogMap(m));
		}

		@Override
		public void interrupt() {
			eof();
		}

		@Override
		public boolean isInterrupted() {
			return status.equals(Status.End);
		}
	}
}
