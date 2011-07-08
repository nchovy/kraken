package org.krakenapps.logstorage.query.command;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.query.LogQueryCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table extends LogQueryCommand {
	private Logger logger = LoggerFactory.getLogger(Table.class);
	private LogStorage storage;
	private String tableName;
	private String[] logFormat;
	private int limit;

	public Table(String tableName) {
		this(tableName, 10000);
	}

	public Table(String tableName, int limit) {
		this.tableName = tableName;
		this.limit = limit;
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

	public String[] getLogFormat() {
		return logFormat;
	}

	public void setLogFormat(String[] logFormat) {
		this.logFormat = logFormat;
	}

	public void setLogFormat(String logFormat) {
		this.logFormat = logFormat.split(" ");
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
			storage.search(tableName, new Date(0), new Date(), limit, null, new Callback());
		} catch (Exception e) {
			logger.error("kraken logstorage: table exception", e);
		} catch (Error e) {
			logger.error("kraken logstorage: table error", e);
		}
		eof();
	}

	@Override
	public void push(Map<String, Object> m) {
	}

	private class Callback implements LogSearchCallback {
		@Override
		public void onLog(Log log) {
			String l = (String) log.getData().get("log");
			String[] values = l.split(" ");

			if (logFormat.length < values.length)
				throw new IllegalArgumentException("invalid log: " + l);

			Map<String, Object> m = new HashMap<String, Object>();
			for (int i = 0; i < logFormat.length; i++)
				m.put(logFormat[i], values[i]);
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
