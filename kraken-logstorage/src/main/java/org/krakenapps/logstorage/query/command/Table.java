package org.krakenapps.logstorage.query.command;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table extends LogQueryCommand {
	private Logger logger = LoggerFactory.getLogger(Table.class);
	private LogStorage storage;
	private String tableName;
	private int limit;
	private Date from;
	private Date to;

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
		this.tableName = tableName;
		this.limit = limit;
		this.from = from;
		this.to = to;
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

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public void start() {
		try {
			if (from == null)
				from = new Date(0);
			if (to == null)
				to = new Date();

			status = Status.Running;
			storage.search(tableName, from, to, limit, null, new Callback());
		} catch (Exception e) {
			logger.error("kraken logstorage: table exception", e);
		} catch (Error e) {
			logger.error("kraken logstorage: table error", e);
		}
		eof();
	}

	@Override
	public void push(Map<String, Object> m) {
		throw new UnsupportedOperationException();
	}

	private class Callback implements LogSearchCallback {
		@Override
		public void onLog(Log log) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("_table", log.getTableName());
			m.put("_id", log.getId());
			m.put("_time", log.getDate());
			m.put("_data", log.getData().get("log"));
			log.getData().remove("log");
			m.putAll(log.getData());
			write(m);
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
