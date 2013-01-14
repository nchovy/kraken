package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogCallback;
import org.krakenapps.logstorage.LogCursor;
import org.krakenapps.logstorage.LogIndexConfig;
import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexItem;
import org.krakenapps.logstorage.LogIndexQuery;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogIndexingTask;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.index.InvertedIndexCursor;
import org.krakenapps.logstorage.index.InvertedIndexItem;
import org.krakenapps.logstorage.index.InvertedIndexReader;
import org.krakenapps.logstorage.index.InvertedIndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-indexer")
@Provides
public class LogIndexerEngine implements LogIndexer {
	private final Logger logger = LoggerFactory.getLogger(LogIndexerEngine.class);
	private final File indexDir;
	private final File queueDir;

	@Requires
	private IndexTokenizerRegistry tokenizerRegistry;

	@Requires
	private LogStorage storage;

	@Requires
	private LogTableRegistry tableRegistry;

	// table name to index config mappings
	private ConcurrentMap<String, List<LogIndexConfig>> tableIndexes;

	private ConcurrentMap<OnlineIndexerKey, OnlineIndexer> onlineIndexers;

	private ExecutorService executor;
	private LinkedBlockingQueue<Runnable> taskQueue;
	private LogReceiver receiver;

	public LogIndexerEngine() {
		indexDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/index");
		queueDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/index/queue");
		queueDir.mkdirs();

		receiver = new LogReceiver();
		tableIndexes = new ConcurrentHashMap<String, List<LogIndexConfig>>();
		onlineIndexers = new ConcurrentHashMap<OnlineIndexerKey, OnlineIndexer>();
	}

	@Validate
	public void start() {
		tableIndexes.clear();
		onlineIndexers.clear();

		// load index configurations

		// build threads
		int cpuCount = Runtime.getRuntime().availableProcessors();
		taskQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(1, cpuCount, 10, TimeUnit.SECONDS, taskQueue, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Log Indexer");
			}
		});

		// start log listening
		storage.addLogListener(receiver);
	}

	@Invalidate
	public void stop() {
		if (storage != null)
			storage.removeLogListener(receiver);

		executor.shutdownNow();
	}

	@Override
	public void createIndex(LogIndexConfig config) {
		if (config == null)
			throw new IllegalArgumentException("config can not be null");

		LogIndexingTask task = new LogIndexingTask();
		task.setTableName(config.getTableName());
		task.setMinDay(config.getMinIndexDay());
		task.setMaxDay(config.getMaxIndexDay());

		executor.submit(new IndexRunner(task));
	}

	@Override
	public void dropIndex(String tableName, String indexName, boolean purgeAll) {

	}

	@Override
	public Set<String> getIndexNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LogIndexConfig getIndexConfig(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Date> getIndexedDays(String tableName, String indexName) {
		int tableId = tableRegistry.getTableId(tableName);
		File tableIndexDir = new File(indexDir, Integer.toString(tableId));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList<Date> days = new ArrayList<Date>();
		for (File f : tableIndexDir.listFiles()) {
			logger.info("kraken logstorage: checking indexed days, file={}", f.getAbsolutePath());
			if (!f.isFile() || !f.canRead())
				continue;

			String fileName = f.getName();
			if (!fileName.endsWith(".seg"))
				continue;

			try {
				String dateString = fileName.substring(0, fileName.length() - 4);
				Date day = dateFormat.parse(dateString);
				days.add(day);
				logger.info("kraken logstorage: fetched indexed days, file={}, day={}", f.getAbsolutePath(), day);
			} catch (Throwable t) {
				logger.error("kraken logstorage: cannot parse index file name", t);
			}
		}

		return days;
	}

	@Override
	public LogIndexCursor search(LogIndexQuery q) throws IOException {
		int tableId = tableRegistry.getTableId(q.getTableName());
		List<Date> totalDays = getIndexedDays(q.getTableName(), q.getIndexName());
		List<Date> filtered = DateUtil.filt(totalDays, q.getMinDay(), q.getMaxDay());
		DateUtil.sortByDesc(filtered);

		return new IndexCursorImpl(tableId, q.getTableName(), filtered, q.getTerm());
	}

	private class IndexRunner implements Runnable {

		private LogIndexingTask task;

		public IndexRunner(LogIndexingTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			boolean fail = false;
			try {
				buildIndexes();
			} catch (Throwable t) {
				logger.error("kraken logstorage: indexing failed, " + task, t);
				fail = true;
			} finally {
				if (!fail)
					logger.info("kraken logstorage: indexing is completed - [{}]", task);
			}
		}

		private void buildIndexes() throws IOException {
			List<Date> days = DateUtil.filt(storage.getLogDates(task.getTableName()), task.getMinDay(), task.getMaxDay());

			for (Date day : days)
				buildIndex(day);
		}

		private void buildIndex(Date day) throws IOException {
			long begin = System.currentTimeMillis();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("kraken logstorage: building index for table [{}], day [{}]", task.getTableName(), dateFormat.format(day));
			LogCursor cursor = storage.openCursor(task.getTableName(), day, true);

			// open index writer
			int tableId = tableRegistry.getTableId(task.getTableName());
			String prefix = tableId + "-" + dateFormat.format(day) + "-";
			File indexFile = File.createTempFile(prefix, ".pos", queueDir);
			File dataFile = File.createTempFile(prefix, ".seg", queueDir);
			InvertedIndexWriter writer = null;

			try {
				writer = new InvertedIndexWriter(indexFile, dataFile);

				// prepare tokenizer
				IndexTokenizer tok = loadTokenizer();

				while (cursor.hasNext()) {
					Log log = cursor.next();
					Set<String> tokens = tok.tokenize(log.getData());
					if (tokens == null)
						continue;

					long timestamp = log.getDate().getTime();
					writer.write(new InvertedIndexItem(timestamp, log.getId(), tokens.toArray(new String[0])));
				}
			} finally {
				if (writer != null)
					writer.close();

				// move to index directory (or copy if partition is different)

				long elapsed = System.currentTimeMillis() - begin;
				logger.info("kraken logstorage: indexing completed for table [{}], day [{}], elapsed [{}]sec", new Object[] {
						task.getTableName(), dateFormat.format(day), elapsed / 1000 });

				String destPrefix = tableId + "/" + dateFormat.format(day);
				File destIndexFile = new File(indexDir, destPrefix + ".pos");
				File destDataFile = new File(indexDir, destPrefix + ".seg");
				move(indexFile, destIndexFile);
				move(dataFile, destDataFile);
			}
		}
	}

	private void move(File src, File dst) {
		// try rename
		src.renameTo(dst);

		// if rename fails, copy and delete old file
	}

	private File getIndexFilePath(int tableId, Date day, String name, String suffix) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String relativePath = tableId + "/" + dateFormat.format(day) + suffix;
		return new File(indexDir, relativePath);
	}

	private class IndexCursorImpl implements LogIndexCursor {
		private int tableId;
		private String tableName;

		// sorted indexed days (from latest to oldest)
		private List<Date> days;

		private String term;

		private SimpleDateFormat dateFormat;

		// loading index (point to days)
		private int index;
		private int dayCount;

		private Date currentDay;
		private InvertedIndexReader currentReader;
		private InvertedIndexCursor currentCursor;

		private Long prefetch;

		public IndexCursorImpl(int tableId, String tableName, List<Date> days, String term) throws IOException {
			this.tableId = tableId;
			this.tableName = tableName;
			this.days = days;
			this.dayCount = days.size();
			this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			this.term = term;

			if (days.size() > 0)
				load(days.get(0));
		}

		private boolean loadNext() throws IOException {
			if (index >= dayCount - 1)
				return false;

			load(days.get(index));
			index++;
			return true;
		}

		private void load(Date day) throws IOException {
			String relativePath = tableId + "/" + dateFormat.format(day);
			File indexFile = getIndexFilePath(tableId, day, null, ".pos");
			File dataFile = getIndexFilePath(tableId, day, null, ".seg");

			currentDay = day;
			currentReader = new InvertedIndexReader(indexFile, dataFile);
			currentCursor = currentReader.openCursor(term);
		}

		@Override
		public boolean hasNext() {
			if (prefetch != null)
				return true;

			// no index files
			if (currentCursor == null)
				return false;

			if (currentCursor.hasNext()) {
				try {
					prefetch = currentCursor.next();
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			currentReader.close();

			try {
				if (loadNext())
					return hasNext();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return false;
		}

		@Override
		public LogIndexItem next() {
			if (!hasNext())
				throw new NoSuchElementException("no more indexed log id");

			Long ret = prefetch;
			prefetch = null;
			return new IndexItem(tableName, currentDay, ret);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void skip(long offset) {
		}

		@Override
		public void close() {
		}
	}

	private static class IndexItem implements LogIndexItem {
		private String tableName;
		private Date day;
		private long id;

		public IndexItem(String tableName, Date day, long id) {
			this.tableName = tableName;
			this.day = day;
			this.id = id;
		}

		@Override
		public String getTableName() {
			return tableName;
		}

		@Override
		public Date getDay() {
			return day;
		}

		@Override
		public long getLogId() {
			return id;
		}

		@Override
		public String toString() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return "index item, table=" + tableName + ", day=" + dateFormat.format(day) + ", id=" + id;
		}

	}

	private class LogReceiver implements LogCallback {

		@Override
		public void onLog(Log log) {
			List<LogIndexConfig> indexes = tableIndexes.get(log.getTableName());
			for (LogIndexConfig index : indexes) {
				onlineIndexers.get(index.getId());
			}
		}
	}

	private IndexTokenizer loadTokenizer() {
		Map<String, String> config = new HashMap<String, String>();
		config.put("delimiters", " []/_+=|&,@():;");
		return tokenizerRegistry.newTokenizer("delimiter", config);
	}

	private OnlineIndexer getOnlineIndexer(OnlineIndexerKey key) {
		OnlineIndexer online = onlineIndexers.get(key);
		if (online != null && online.isOpen())
			return online;

		try {
			OnlineIndexer oldIndexer = onlineIndexers.get(key);

			if (oldIndexer != null) {
				synchronized (oldIndexer) {
					if (!oldIndexer.isOpen() && !oldIndexer.isClosed()) { // closing
						while (!oldIndexer.isClosed()) {
							try {
								oldIndexer.wait(1000);
							} catch (InterruptedException e) {
							}
						}

						while (onlineIndexers.get(key) == oldIndexer) {
							Thread.yield();
						}

						online = loadNewIndexer(key);
					} else if (oldIndexer.isClosed()) {
						while (onlineIndexers.get(key) == oldIndexer) {
							Thread.yield();
						}

						online = loadNewIndexer(key);
					} else {
						online = oldIndexer;
					}
				}
			} else {
				online = loadNewIndexer(key);
			}
		} catch (Exception e) {
			throw new IllegalStateException("cannot open indexer: " + key.indexId + ", date=" + key.day, e);
		}

		return online;
	}

	private OnlineIndexer loadNewIndexer(OnlineIndexerKey key) {
		OnlineIndexer online = null;
		IndexTokenizer tok = loadTokenizer();
		OnlineIndexer newWriter = new OnlineIndexer(key.indexId, key.day, tok);
		OnlineIndexer consensus = onlineIndexers.putIfAbsent(key, newWriter);
		if (consensus == null)
			online = newWriter;
		else {
			online = consensus;
			if (consensus != newWriter)
				newWriter.close();
		}
		return online;
	}

	private class IndexerSweeper implements Runnable {
		private volatile boolean doStop = false;

		@Override
		public void run() {
			try {
				while (true) {
					try {
						if (doStop)
							break;

						Thread.sleep(1000);
						sweep();
					} catch (InterruptedException e) {
						logger.trace("kraken logstorage: indexer sweeper interrupted");
					} catch (Exception e) {
						logger.error("krakne logstorage: indexer sweeper error", e);
					}
				}
			} finally {
				doStop = false;
			}

			logger.info("kraken logstorage: indexer sweeper stopped");
		}

		private void sweep() {
			List<OnlineIndexer> evicts = new ArrayList<OnlineIndexer>();

			long now = System.currentTimeMillis();
			for (OnlineIndexer indexer : onlineIndexers.values()) {
				boolean doFlush = (now - indexer.lastAccess) > 10000 || indexer.needFlush();
				if (doFlush) {
					try {
						logger.trace("kraken logstorage: flushing index [{}]", indexer.id);
						indexer.flush();
					} catch (IOException e) {
						logger.error("kraken logstorage: cannot flush index " + indexer.id, e);
					}
				}

				// close file if indexer is in idle state
				int interval = (int) (now - indexer.lastAccess);
				if (interval > 30000 && !indexer.merging)
					evicts.add(indexer);
			}

			// evict
			for (OnlineIndexer indexer : evicts) {
				indexer.close();
				onlineIndexers.remove(indexer.id);
			}
		}
	}

	private class OnlineIndexer {
		private int id;

		/**
		 * only yyyy-MM-dd (excluding hour, min, sec, milli)
		 */
		private Date day;

		/**
		 * is in closing state?
		 */
		private boolean closing;

		/**
		 * maintain last write access time. idle indexer should be evicted
		 */
		private long lastAccess = System.currentTimeMillis();

		private boolean merging;

		// waiting flush queue
		private List<InvertedIndexItem> queue;

		private InvertedIndexWriter writer;

		private IndexTokenizer tokenizer;

		public OnlineIndexer(int indexId, Date day, IndexTokenizer tokenizer) {
			this.id = indexId;
			this.day = day;
			this.tokenizer = tokenizer;
			this.queue = new ArrayList<InvertedIndexItem>(10000);
		}

		public boolean isOpen() {
			return writer != null && closing == false;
		}

		public boolean isClosed() {
			return closing == true && writer == null;
		}

		public void write(Log log) throws IOException {
			Set<String> tokens = tokenizer.tokenize(log.getData());
			if (tokens == null)
				return;

			long timestamp = log.getDate().getTime();
			synchronized (this) {
				queue.add(new InvertedIndexItem(timestamp, log.getId(), tokens.toArray(new String[0])));
				if (needFlush())
					flush();
			}
		}

		public boolean needFlush() {
			return queue.size() > 10000;
		}

		public void flush() throws IOException {
			if (merging) {
				logger.trace("kraken logstorage: merging in-progress, ignore flush index [{}]", id);
				return;
			}

			if (logger.isTraceEnabled()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				logger.trace("kraken logstorage: flushing index [{}], day [{}]", id, dateFormat.format(day));
			}

			synchronized (this) {
				for (InvertedIndexItem item : queue)
					writer.write(item);

				queue.clear();
				writer.flush();
				notifyAll();
			}
		}

		public void close() {
			if (closing)
				return;

			try {
				synchronized (this) {
					closing = true;
					flush();
					writer.close();
					notifyAll();
					writer = null;
				}

			} catch (IOException e) {
				logger.error("cannot close online index writer", e);
			}
		}
	}

	private static class OnlineIndexerKey {
		private int indexId;
		private Date day;

		public OnlineIndexerKey(int indexId, Date day) {
			this.indexId = indexId;
			this.day = day;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((day == null) ? 0 : day.hashCode());
			result = prime * result + indexId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OnlineIndexerKey other = (OnlineIndexerKey) obj;
			if (day == null) {
				if (other.day != null)
					return false;
			} else if (!day.equals(other.day))
				return false;
			if (indexId != other.indexId)
				return false;
			return true;
		}
	}
}
