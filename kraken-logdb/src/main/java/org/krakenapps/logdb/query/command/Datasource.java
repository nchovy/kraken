package org.krakenapps.logdb.query.command;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;

import org.krakenapps.log.api.LogParser;
import org.krakenapps.log.api.LogParserFactory;
import org.krakenapps.log.api.LogParserFactoryRegistry;
import org.krakenapps.log.api.LoggerConfigOption;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;

public class Datasource extends LogQueryCommand {
	private static final int SIZE = 100;

	private Collection<DataSource> sources;
	private LogStorage logStorage;
	private LogTableRegistry tableRegistry;
	private LogParserFactoryRegistry parserFactoryRegistry;
	private int offset;
	private int limit;
	private Date from;
	private Date to;
	private PriorityQueue<DataWrapper> q;

	public Datasource(Collection<DataSource> sources) {
		this.sources = sources;
		this.q = new PriorityQueue<DataWrapper>(SIZE * sources.size() + 1, new DataWrapperComparator());
		
	}

	public Collection<DataSource> getSources() {
		return sources;
	}

	public void setSources(Collection<DataSource> sources) {
		this.sources = sources;
	}

	public LogStorage getLogStorage() {
		return logStorage;
	}

	public void setLogStorage(LogStorage logStorage) {
		this.logStorage = logStorage;
	}

	public LogTableRegistry getTableRegistry() {
		return tableRegistry;
	}

	public void setTableRegistry(LogTableRegistry tableRegistry) {
		this.tableRegistry = tableRegistry;
	}

	public LogParserFactoryRegistry getParserFactoryRegistry() {
		return parserFactoryRegistry;
	}

	public void setParserFactoryRegistry(LogParserFactoryRegistry parserFactoryRegistry) {
		this.parserFactoryRegistry = parserFactoryRegistry;
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

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	@Override
	public void start() {
		status = Status.Running;
		for (DataSource source : sources)
			new SourceWrapper(source);

		int write = 0;
		while (!q.isEmpty()) {
			DataWrapper data = q.poll();
			data.source.remain--;
			if (data.source.remain == 0)
				data.source.load();

			write(new LogMap(data.get()));
			if (++write == limit)
				break;
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

	private class SourceWrapper {
		private DataSource source;
		private LogParser parser;
		private int remain;
		private int loaded;

		public SourceWrapper(DataSource source) {
			this.source = source;
			if (source.getType().equals("table")) {
				String parserName = tableRegistry.getTableMetadata(source.getName(), "logparser");
				LogParserFactory parserFactory = parserFactoryRegistry.get(parserName);
				if (parserFactory != null) {
					Properties prop = new Properties();
					for (LoggerConfigOption configOption : parserFactory.getConfigOptions()) {
						String optionName = configOption.getName();
						String optionValue = tableRegistry.getTableMetadata(source.getName(), optionName);
						if (optionValue == null)
							throw new IllegalArgumentException("require table metadata " + optionName);
						prop.put(optionName, optionValue);
					}
					this.parser = parserFactory.createParser(prop);
				}
			}
			load();
		}

		public void load() {
			if (remain > 0)
				return;

			if (source.getType().equals("table")) {
				try {
					remain = logStorage.search(source.getName(), from, to, loaded, SIZE, new LogSearchCallbackImpl(this));
					loaded += remain;
				} catch (InterruptedException e) {
				}
			} else if (source.getType().equals("rpc")) {
				// TODO
			}
		}
	}

	private class LogSearchCallbackImpl implements LogSearchCallback {
		private SourceWrapper source;
		private boolean interrupt = false;

		public LogSearchCallbackImpl(SourceWrapper source) {
			this.source = source;
		}

		@Override
		public void onLog(Log log) {
			q.add(new DataWrapper(source, log));
		}

		@Override
		public void interrupt() {
			this.interrupt = true;
		}

		@Override
		public boolean isInterrupted() {
			return interrupt;
		}
	}

	private class DataWrapper {
		private SourceWrapper source;
		private Object data;

		public DataWrapper(SourceWrapper source, Object data) {
			this.source = source;
			this.data = data;
		}

		public Map<String, Object> get() {
			if (data instanceof Log) {
				Log log = (Log) data;
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("_table", log.getTableName());
				m.put("_id", log.getId());
				m.put("_time", log.getDate());
				if (source.parser != null)
					m.putAll(source.parser.parse(log.getData()));
				else
					m.putAll(log.getData());
				return m;
			}
			// TODO get rpc source data
			return null;
		}
	}

	private class DataWrapperComparator implements Comparator<DataWrapper> {
		@Override
		public int compare(DataWrapper o1, DataWrapper o2) {
			Date d1 = null;
			Date d2 = null;
			if (o1.data instanceof Log)
				d1 = ((Log) o1.data).getDate();
			if (o2.data instanceof Log)
				d2 = ((Log) o2.data).getDate();
			// TODO get rpc source date
			return -d1.compareTo(d2);
		}
	}
}
