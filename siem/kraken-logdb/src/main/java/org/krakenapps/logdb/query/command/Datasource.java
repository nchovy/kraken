package org.krakenapps.logdb.query.command;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
	private Collection<DataSource> sources;
	private int cacheSize;
	private LogStorage logStorage;
	private LogTableRegistry tableRegistry;
	private LogParserFactoryRegistry parserFactoryRegistry;
	private int offset;
	private int limit;
	private Date from;
	private Date to;
	private Set<SourceWrapper> wrappers = new HashSet<SourceWrapper>();
	private PriorityQueue<DataWrapper> q;

	public Datasource(Collection<DataSource> sources) {
		this(sources, 5000);
	}

	public Datasource(Collection<DataSource> sources, int cacheSize) {
		this.sources = sources;
		this.cacheSize = cacheSize;
		this.q = new PriorityQueue<DataWrapper>(cacheSize * sources.size(), new DataWrapperComparator());
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

		if (from == null)
			from = new Date(0);
		if (to == null)
			to = new Date();

		for (DataSource source : sources)
			wrappers.add(new SourceWrapper(source));

		int write = 0;
		while (!q.isEmpty() || !isEnd()) {
			if (status.equals(Status.End))
				break;

			DataWrapper data = poll();
			if (data == null) {
				for (SourceWrapper wrapper : wrappers) {
					if (wrapper.cache.size() > 0) {
						int s = wrapper.cache.drainTo(q);
						wrapper.remain.addAndGet(s);
					}
				}
				continue;
			}

			write(new LogMap(data.get()));
			if (limit > 0 && ++write >= limit)
				break;
		}
		for (SourceWrapper wrapper : wrappers)
			wrapper.interrupt();
		eof();
	}

	private boolean isEnd() {
		for (SourceWrapper wrapper : wrappers) {
			if (!wrapper.isEnd())
				return false;
		}
		return true;
	}

	private DataWrapper poll() {
		DataWrapper data = q.poll();
		if (data == null)
			return null;

		SourceWrapper source = data.source;
		if (source.remain.decrementAndGet() <= 0) {
			int s = source.cache.drainTo(q);
			source.remain.addAndGet(s);
		}
		return data;
	}

	@Override
	public void push(LogMap m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReducer() {
		return false;
	}

	private class SourceWrapper implements Runnable {
		private DataSource source;
		private LogParser parser;
		private Thread thread;
		private AtomicInteger remain = new AtomicInteger();
		private BlockingQueue<DataWrapper> cache = new ArrayBlockingQueue<DataWrapper>(cacheSize);
		private volatile boolean end = false;

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

			String threadName = String.format("Log Query %d: Datasource %s", logQuery.getId(), source.getName());
			this.thread = new Thread(this, threadName);
			thread.start();
		}

		public boolean isEnd() {
			return end;
		}

		public void interrupt() {
			if (thread != null)
				thread.interrupt();
		}

		@Override
		public void run() {
			try {
				if (source.getType().equals("table")) {
					try {
						logStorage.search(source.getName(), from, to, 0, limit, new LogSearchCallbackImpl());
					} catch (InterruptedException e) {
					}
				} else if (source.getType().equals("rpc")) {
					// TODO
				}
			} finally {
				end = true;
			}
		}

		private class LogSearchCallbackImpl implements LogSearchCallback {
			private boolean interrupt = false;

			@Override
			public void onLog(Log log) {
				try {
					cache.put(new DataWrapper(SourceWrapper.this, log));
					if (SourceWrapper.this.remain.get() == 0) {
						int s = cache.drainTo(q);
						remain.addAndGet(s);
					}
				} catch (InterruptedException e) {
				}
			}

			@Override
			public void interrupt() {
				this.interrupt = true;
			}

			@Override
			public boolean isInterrupted() {
				return interrupt | status.equals(Status.End);
			}
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
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;

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
