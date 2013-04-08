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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicate;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.logstorage.BatchIndexingStatus;
import org.krakenapps.logstorage.BatchIndexingTask;
import org.krakenapps.logstorage.IndexTokenizer;
import org.krakenapps.logstorage.IndexTokenizerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogCallback;
import org.krakenapps.logstorage.LogCursor;
import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexQuery;
import org.krakenapps.logstorage.LogIndexSchema;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogIndexerStatus;
import org.krakenapps.logstorage.LogRetentionPolicy;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableEventListener;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.index.InvertedIndexFileSet;
import org.krakenapps.logstorage.index.InvertedIndexItem;
import org.krakenapps.logstorage.index.InvertedIndexUtil;
import org.krakenapps.logstorage.index.InvertedIndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 0.9
 * @author xeraph
 */
@Component(name = "logstorage-indexer")
@Provides
public class LogIndexerEngine implements LogIndexer {
	private final Logger logger = LoggerFactory.getLogger(LogIndexerEngine.class);
	private final File indexBaseDir;
	private final File queueDir;

	@Requires
	private IndexTokenizerRegistry tokenizerRegistry;

	@Requires
	private LogStorage storage;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private ConfigService conf;

	// table name to index config mappings
	private ConcurrentMap<String, Set<LogIndexSchema>> tableIndexes;

	// memory-buffered indexes
	private ConcurrentMap<OnlineIndexerKey, OnlineIndexer> onlineIndexers;

	// background index building
	private ExecutorService executor;

	// for realtime log indexing
	private LogReceiver receiver;

	// drop index when table drops, cache table name-id mappings
	private TableEventListener tableRegistryListener;

	// periodically flush and evict indexers
	private IndexerSweeper sweeper;
	private Thread sweeperThread;

	// to assign unique index id
	private AtomicInteger indexIdCounter;

	// cache table name->id mappings for index drop
	private ConcurrentMap<String, Integer> tableNameIdMap;

	private ConcurrentMap<BatchIndexKey, BatchIndexingTask> batchJobs;

	// index id, prevent also online indexing
	private CopyOnWriteArraySet<Integer> deleteLocks;

	// index id
	private CopyOnWriteArraySet<Integer> mergeLocks;

	public LogIndexerEngine() {
		indexBaseDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/index");
		queueDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/index/queue");
		queueDir.mkdirs();

		receiver = new LogReceiver();
		tableRegistryListener = new TableEventListener();
		tableIndexes = new ConcurrentHashMap<String, Set<LogIndexSchema>>();
		onlineIndexers = new ConcurrentHashMap<OnlineIndexerKey, OnlineIndexer>();
		tableNameIdMap = new ConcurrentHashMap<String, Integer>();
		batchJobs = new ConcurrentHashMap<BatchIndexKey, BatchIndexingTask>();
		deleteLocks = new CopyOnWriteArraySet<Integer>();
		mergeLocks = new CopyOnWriteArraySet<Integer>();

		sweeper = new IndexerSweeper();

		indexIdCounter = new AtomicInteger();
	}

	@Validate
	public void start() {
		tableIndexes.clear();
		onlineIndexers.clear();
		tableNameIdMap.clear();

		// build threads
		int cpuCount = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(cpuCount, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "Batch Log Indexer");
			}
		});

		// load table name-id mappings
		for (String tableName : tableRegistry.getTableNames())
			tableNameIdMap.put(tableName, tableRegistry.getTableId(tableName));

		// load index configurations
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Collection<LogIndexSchema> indexes = db.findAll(LogIndexSchema.class).getDocuments(LogIndexSchema.class);

		for (LogIndexSchema index : indexes) {
			Set<LogIndexSchema> s = tableIndexes.get(index.getTableName());
			if (s == null) {
				s = new CopyOnWriteArraySet<LogIndexSchema>();
				tableIndexes.put(index.getTableName(), s);
			}

			s.add(index);

			if (indexIdCounter.get() < index.getId())
				indexIdCounter.set(index.getId());
		}

		// start index sweeper
		sweeperThread = new Thread(sweeper, "LogStorage IndexWriter Sweeper");
		sweeperThread.start();

		// to listen drop event and drop all related indexes
		tableRegistry.addListener(tableRegistryListener);

		// receive all logs and index
		storage.addLogListener(receiver);
	}

	@Invalidate
	public void stop() {
		if (storage != null)
			storage.removeLogListener(receiver);

		if (tableRegistry != null)
			tableRegistry.removeListener(tableRegistryListener);

		sweeper.doStop = true;
		sweeperThread.interrupt();

		executor.shutdownNow();
	}

	@Override
	public void createIndex(LogIndexSchema config) {
		if (config == null)
			throw new IllegalArgumentException("config can not be null");

		if (!tableRegistry.exists(config.getTableName()))
			throw new IllegalStateException("table does not exist: " + config.getTableName());

		// assign new index id
		config.setId(indexIdCounter.incrementAndGet());

		// check duplicated index
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Predicate cond = Predicates.or(
				Predicates.field("id", config.getId()),
				Predicates.and(Predicates.field("table_name", config.getTableName()),
						Predicates.field("index_name", config.getIndexName())));
		if (db.findOne(LogIndexSchema.class, cond) != null)
			throw new IllegalStateException("same index id (" + config.getId() + ") or name (" + config.getIndexName()
					+ ") already exist in metadata");

		// register index
		Set<LogIndexSchema> indexes = tableIndexes.get(config.getTableName());
		if (indexes == null) {
			indexes = new CopyOnWriteArraySet<LogIndexSchema>();
			tableIndexes.putIfAbsent(config.getTableName(), indexes);
		}

		// ensure index directory
		int tableId = tableRegistry.getTableId(config.getTableName());
		File dir = new File(indexBaseDir, tableId + "/" + config.getId());
		dir.mkdirs();

		// save index metadata
		db.add(config);
		indexes.add(config);

		enqueueBatchIndexJobs(config);
		logger.info("kraken logstorage: created index => " + config);
	}

	private void enqueueBatchIndexJobs(LogIndexSchema config) {
		BatchIndexingTask task = new BatchIndexingTask();
		task.setTableName(config.getTableName());
		task.setIndexName(config.getIndexName());
		task.setTableId(tableNameIdMap.get(config.getTableName()));
		task.setIndexId(config.getId());
		task.setMinDay(config.getMinIndexDay());

		batchJobs.put(new BatchIndexKey(config.getTableName(), config.getIndexName()), task);

		List<BatchIndexingStatus> builders = new ArrayList<BatchIndexingStatus>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		List<Date> days = DateUtil.filt(storage.getLogDates(task.getTableName()), task.getMinDay(), task.getMaxDay());
		for (Date day : days) {
			try {
				int tableId = tableRegistry.getTableId(task.getTableName());
				String prefix = tableId + "-" + config.getId() + "-" + dateFormat.format(day) + "-";
				File indexFile = File.createTempFile(prefix, ".pos", queueDir);
				File dataFile = File.createTempFile(prefix, ".seg", queueDir);

				BatchIndexingStatus s = new BatchIndexingStatus();
				s.setTask(task);
				s.setDay(day);
				s.setFiles(new InvertedIndexFileSet(indexFile, dataFile));
				task.getBuilds().put(day, s);
				builders.add(s);
			} catch (IOException e) {
				logger.error("kraken logstorage: cannot queue batch indexing for table " + config.getTableName() + ", index "
						+ config.getIndexName(), e);
			}
		}

		for (BatchIndexingStatus s : builders)
			executor.execute(new IndexRunner(s));
	}

	@Override
	public void dropIndex(String tableName, String indexName) {
		// check metadata
		LogIndexSchema found = getIndexConfig(tableName, indexName);

		// check database metadata
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogIndexSchema.class, Predicates.field("id", found.getId()));
		if (c == null)
			throw new IllegalStateException("index metadata not found, table=" + tableName + ", index=" + indexName);

		// set delete lock
		try {
			deleteLocks.add(found.getId());

			// remove from memory and database
			Set<LogIndexSchema> s = tableIndexes.get(tableName);
			s.remove(found);
			db.remove(c);

			// cancel all batch jobs
			BatchIndexingTask task = batchJobs.get(new BatchIndexKey(tableName, indexName));
			if (task != null) {
				task.setCanceled(true);
			}

			// evict online indexer for dropping index id
			for (OnlineIndexer indexer : new ArrayList<OnlineIndexer>(onlineIndexers.values())) {
				if (indexer.id != found.getId())
					continue;

				onlineIndexers.remove(new OnlineIndexerKey(indexer.id, indexer.day, indexer.tableId, indexer.tableName,
						indexer.indexName));
				logger.trace("kraken logstorage: closing online indexer [{}, {}] due to [{}] table drop",
						new Object[] { found.getId(), found.getIndexName(), found.getTableName() });
				indexer.close();
			}

			// purge index files
			int tableId = tableNameIdMap.get(tableName);
			File dir = new File(indexBaseDir, tableId + "/" + found.getId());
			File[] files = dir.listFiles();
			if (files != null) {
				for (File f : files) {
					String fileName = f.getName();
					if (f.isFile() && (fileName.endsWith(".pos") || fileName.endsWith(".seg"))) {
						ensureDelete(f);
					}
				}
			}

			// try to delete empty directory
			dir.delete();
		} finally {
			deleteLocks.remove(found.getId());
		}
	}

	private LogIndexSchema getIndexSchema(String tableName, String indexName) {
		Set<LogIndexSchema> s = tableIndexes.get(tableName);
		if (s == null)
			throw new IllegalStateException("index not found, table=" + tableName + ", index=" + indexName);

		LogIndexSchema found = null;
		for (LogIndexSchema c : s) {
			if (c.getIndexName().equals(indexName))
				found = c;
		}

		if (found == null)
			throw new IllegalStateException("index not found, table=" + tableName + ", index=" + indexName);

		return found;
	}

	private boolean ensureDelete(File f) {
		final int MAX_TIMEOUT = 30000;

		long begin = System.currentTimeMillis();

		while (true) {
			if (f.delete()) {
				logger.trace("kraken logstorage: deleted index file [{}]", f.getAbsolutePath());
				return true;
			}

			if (System.currentTimeMillis() - begin > MAX_TIMEOUT) {
				logger.error("kraken logstorage: delete timeout, cannot delete index file [{}]", f.getAbsolutePath());
				return false;
			}
		}
	}

	@Override
	public void dropAllIndexes(String tableName) {
		Set<LogIndexSchema> indexes = tableIndexes.get(tableName);
		if (indexes == null)
			return;

		for (LogIndexSchema c : indexes) {
			try {
				dropIndex(tableName, c.getIndexName());
			} catch (Throwable t) {
				logger.error("kraken logstorage: cannot drop index [" + c.getIndexName() + "] of table [" + tableName + "]", t);
			}
		}

		// try to delete index directory if empty
		int tableId = tableNameIdMap.get(tableName);
		File dir = new File(indexBaseDir, Integer.toString(tableId));
		dir.delete();
	}

	@Override
	public Set<String> getIndexNames(String tableName) {
		TreeSet<String> names = new TreeSet<String>();
		Set<LogIndexSchema> indexes = tableIndexes.get(tableName);
		if (indexes == null)
			return names;

		for (LogIndexSchema c : indexes)
			names.add(c.getIndexName());

		return names;
	}

	@Override
	public LogIndexSchema getIndexConfig(String tableName, String indexName) {
		Set<LogIndexSchema> indexes = tableIndexes.get(tableName);
		for (LogIndexSchema c : indexes)
			if (c.getIndexName().equals(indexName))
				return c;

		return null;
	}

	@Override
	public List<Date> getIndexedDays(String tableName, String indexName) {
		if (!tableRegistry.exists(tableName))
			return null;

		int tableId = tableRegistry.getTableId(tableName);

		LogIndexSchema config = getIndexConfig(tableName, indexName);
		if (config == null)
			return null;

		return getIndexedDays(tableId, config.getId());
	}

	private List<Date> getIndexedDays(int tableId, int indexId) {
		File tableIndexDir = new File(indexBaseDir, tableId + "/" + indexId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList<Date> days = new ArrayList<Date>();
		File[] files = tableIndexDir.listFiles();
		if (files == null)
			return days;

		for (File f : files) {
			if (!f.isFile() || !f.canRead())
				continue;

			String fileName = f.getName();
			if (!fileName.endsWith(".seg"))
				continue;

			try {
				String dateString = fileName.substring(0, fileName.length() - 4);
				Date day = dateFormat.parse(dateString);
				days.add(day);
				logger.debug("kraken logstorage: fetched indexed days, file={}, day={}", f.getAbsolutePath(), day);
			} catch (Throwable t) {
				logger.error("kraken logstorage: cannot parse index file name", t);
			}
		}

		return days;
	}

	@Override
	public LogIndexCursor search(LogIndexQuery q) throws IOException {
		List<IndexCursorItem> cursorItems = new ArrayList<IndexCursorItem>();

		for (Entry<String, Set<LogIndexSchema>> pair : tableIndexes.entrySet()) {
			String tableName = pair.getKey();
			if (q.getTableName() != null && !q.getTableName().equals(tableName))
				continue;

			for (LogIndexSchema c : pair.getValue()) {
				// skip delete locked index (no more access)
				if (deleteLocks.contains(c.getId())) {
					logger.trace("kraken logstorage: skipping deleted locked index, table={}, index={}", c.getTableName(),
							c.getIndexName());
					continue;
				}

				// skip merging index
				if (mergeLocks.contains(c.getId())) {
					logger.trace("kraken logstorage: skipping merging index, table={}, index={}", c.getTableName(),
							c.getIndexName());
					continue;
				}

				if (q.getIndexName() != null && !q.getIndexName().equals(c.getIndexName()))
					continue;

				Integer tableId = tableNameIdMap.get(tableName);
				if (tableId == null) {
					logger.warn("kraken logstorage: garbage index metadata found [table={}, index={}]", tableName,
							c.getIndexName());
					continue;
				}

				List<InvertedIndexItem> buffer = getIndexBuffer(tableName);
				cursorItems.add(new IndexCursorItem(tableId, c.getId(), tableName, c.getIndexName(), buffer));
			}
		}

		return new MergedIndexCursor(this, q, cursorItems, indexBaseDir);
	}

	private List<InvertedIndexItem> getIndexBuffer(String tableName) {
		List<InvertedIndexItem> l = new LinkedList<InvertedIndexItem>();
		for (OnlineIndexer indexer : onlineIndexers.values()) {
			l.addAll(indexer.queue);
		}
		return l;
	}

	private class IndexRunner implements Runnable {

		private BatchIndexingStatus status;

		public IndexRunner(BatchIndexingStatus status) {
			this.status = status;
		}

		@Override
		public void run() {
			boolean fail = false;
			try {
				buildIndex();
			} catch (Throwable t) {
				logger.error("kraken logstorage: indexing failed, " + status, t);
				fail = true;
			} finally {
				if (!fail)
					logger.trace("kraken logstorage: batch indexing is completed - [{}]", status);
			}
		}

		private void buildIndex() throws IOException {
			long begin = System.currentTimeMillis();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			BatchIndexingTask task = status.getTask();
			logger.trace("kraken logstorage: building index for table [{}], day [{}]", task.getTableName(),
					dateFormat.format(status.getDay()));

			// open index writer
			LogCursor cursor = null;
			InvertedIndexWriter writer = null;

			try {
				cursor = storage.openCursor(task.getTableName(), status.getDay(), true);
				writer = new InvertedIndexWriter(status.getFiles());

				// prepare tokenizer
				LogIndexSchema schema = getIndexSchema(task.getTableName(), task.getIndexName());
				IndexTokenizer tok = tokenizerRegistry.newTokenizer(schema.getTokenizerName(), schema.getTokenizerConfigs());

				while (cursor.hasNext()) {
					if (task.isCanceled())
						break;

					Log log = cursor.next();
					Set<String> tokens = tok.tokenize(log.getData());
					if (tokens == null)
						continue;

					status.addLogCount(1);
					status.addTokenCount(tokens.size());

					long timestamp = log.getDate().getTime();
					writer.write(new InvertedIndexItem(task.getTableName(), timestamp, log.getId(), tokens.toArray(new String[0])));
				}
			} finally {
				if (cursor != null)
					cursor.close();

				if (writer != null)
					writer.close();

				if (task.isCanceled()) {
					boolean indexDeleted = status.getIndexFile().delete();
					boolean dataDeleted = status.getDataFile().delete();
					logger.trace(
							"kraken logstorage: batch indexing is canceled, table [{}], index [{}], day [{}], index deleted={}, data deleted={}",
							new Object[] { task.getTableName(), task.getIndexName(), dateFormat.format(status.getDay()),
									indexDeleted, dataDeleted });
					status.setDone(true);
					return;
				}

				// move to index directory (or copy if partition is different)
				long elapsed = System.currentTimeMillis() - begin;
				logger.trace("kraken logstorage: indexing completed for table [{}], day [{}], elapsed [{}]sec", new Object[] {
						task.getTableName(), dateFormat.format(status.getDay()), elapsed / 1000 });

				File destIndexFile = getIndexFilePath(task.getTableId(), task.getIndexId(), status.getDay(), ".pos");
				File destDataFile = getIndexFilePath(task.getTableId(), task.getIndexId(), status.getDay(), ".seg");

				OnlineIndexerKey key = new OnlineIndexerKey(task.getIndexId(), status.getDay(), task.getTableId(),
						task.getTableName(), task.getIndexName());
				if (!destIndexFile.exists() && !destDataFile.exists()) {
					move(status.getIndexFile(), destIndexFile);
					move(status.getDataFile(), destDataFile);
				} else {
					InvertedIndexFileSet newer = new InvertedIndexFileSet(destIndexFile, destDataFile);
					InvertedIndexFileSet older = new InvertedIndexFileSet(status.getIndexFile(), status.getDataFile());
					merge(key, older, newer);
				}

				status.setDone(true);
			}
		}
	}

	private void merge(OnlineIndexerKey key, InvertedIndexFileSet older, InvertedIndexFileSet newer) throws IOException {
		// prevent flush and evict
		OnlineIndexer indexer = getOnlineIndexer(key);
		indexer.prepareMerge();

		// do merge
		File mergedIndexFile = null;
		File mergedDataFile = null;
		try {
			// prevent read/write file access
			mergeLocks.add(key.indexId);

			File dir = newer.getIndexFile().getParentFile();
			mergedIndexFile = File.createTempFile("index-", ".mpos", dir);
			mergedDataFile = File.createTempFile("index-", ".mseg", dir);

			InvertedIndexFileSet merged = new InvertedIndexFileSet(mergedIndexFile, mergedDataFile);
			InvertedIndexUtil.merge(older, newer, merged);

			boolean success = true;
			if (!ensureDelete(newer.getIndexFile())) {
				success = false;
				logger.error("kraken logstorage: cannot delete online index file, {}", newer.getIndexFile().getName());
			}

			if (!ensureDelete(newer.getDataFile())) {
				success = false;
				logger.error("kraken logstorage: cannot delete online index file, {}", newer.getDataFile().getName());
			}

			if (!ensureDelete(older.getIndexFile())) {
				success = false;
				logger.error("kraken logstorage: cannot delete batch index file, {}", older.getIndexFile().getName());
			}

			if (!ensureDelete(older.getDataFile())) {
				success = false;
				logger.error("kraken logstorage: cannot delete batch index file, {}", older.getDataFile().getName());
			}

			if (!merged.getIndexFile().renameTo(newer.getIndexFile())) {
				success = false;
				logger.error("kraken logstorage: cannot rename [{}] to [{}]", merged.getIndexFile().getName(), newer
						.getIndexFile().getName());
			}

			if (!merged.getDataFile().renameTo(newer.getDataFile())) {
				success = false;
				logger.error("kraken logstorage: cannot rename [{}] to [{}]", merged.getDataFile().getName(), newer.getDataFile()
						.getName());
			}

			if (success)
				logger.trace("kraken logstorage: merge success for {}", key);
			else
				logger.error("kraken logstorage: merge failed for {}", key);
		} catch (Throwable t) {
			logger.error("kraken logstorage: merge failed for " + key + ", deleting temp files", t);
			// purge temporary files if failed
			if (mergedIndexFile != null)
				mergedIndexFile.delete();
			if (mergedDataFile != null)
				mergedDataFile.delete();
		} finally {
			// allow index access
			mergeLocks.remove(key.indexId);
			indexer.finishMerge();
		}
	}

	private void move(File src, File dst) {
		String srcPath = src.getAbsolutePath();
		if (!src.exists())
			throw new IllegalStateException("source file not found: " + srcPath);

		// try rename
		String dstPath = dst.getAbsolutePath();
		if (src.renameTo(dst)) {
			logger.trace("kraken logstorage: moved index file [{}] to [{}]", srcPath, dstPath);
			return;
		}
		// if rename fails, copy and delete old file
		if (dst.exists()) {
			logger.warn("kraken logstorage: need to merge file [{}] with [{}], not supported yet", dstPath, srcPath);
			return;
		}

		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			byte[] b = new byte[8096];
			is = new FileInputStream(src);
			os = new FileOutputStream(dst);

			while (true) {
				int len = is.read(b);
				if (len <= 0)
					break;
				os.write(b, 0, len);
			}

			logger.trace("kraken logstorage: rename failed, copied index file [{}] to [{}], and deleted old one", srcPath,
					dstPath);
		} catch (IOException e) {
			logger.error("kraken logstorage: cannot copy file [" + srcPath + "] to [" + dstPath + "]", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}

			boolean ret = src.delete();
			if (!ret)
				logger.error("kraken logstorage: cannot delete temporary index file, {}", src.getAbsolutePath());
		}
	}

	@Override
	public File getIndexDirectory(String tableName, String indexName) {
		LogIndexSchema schema = getIndexSchema(tableName, indexName);
		int tableId = tableRegistry.getTableId(tableName);
		return new File(indexBaseDir, tableId + "/" + schema.getId());
	}

	private File getIndexFilePath(int tableId, int indexId, Date day, String suffix) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String relativePath = tableId + "/" + indexId + "/" + dateFormat.format(day) + suffix;
		return new File(indexBaseDir, relativePath);
	}

	@Override
	public Date getPurgeBaseline(String tableName, String indexName) {
		LogRetentionPolicy p = storage.getRetentionPolicy(tableName);
		if (p == null || p.getRetentionDays() == 0)
			return null;

		List<Date> days = getIndexedDays(tableName, indexName);
		Date lastDay = getMaxDay(days.iterator());
		if (lastDay == null)
			return null;

		return getBaseline(lastDay, p.getRetentionDays());
	}

	private Date getMaxDay(Iterator<Date> days) {
		Date max = null;
		while (days.hasNext()) {
			Date day = days.next();
			if (max == null)
				max = day;
			else if (max != null && day.after(max))
				max = day;
		}
		return max;
	}

	private Date getBaseline(Date lastDay, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(lastDay);
		c.add(Calendar.DAY_OF_MONTH, -days);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	@Override
	public void purge(String tableName, String indexName, Date fromDay, Date toDay) {
		LogIndexSchema schema = getIndexConfig(tableName, indexName);
		if (schema == null)
			throw new IllegalStateException("index not found for table=" + tableName + ", index=" + indexName);

		File dir = getIndexDirectory(tableName, indexName);
		if (dir == null)
			return;

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (File f : dir.listFiles()) {
			if (!f.isFile())
				continue;

			if (!f.getName().endsWith(".pos") && !f.getName().endsWith(".seg"))
				continue;

			String dayStr = null;
			Date day = null;
			try {
				dayStr = f.getName().substring(0, f.getName().indexOf('.'));
				day = dateFormat.parse(dayStr);
			} catch (ParseException e) {
				continue;
			}

			if (fromDay != null && day.before(fromDay))
				continue;

			if (toDay != null && day.after(toDay))
				continue;

			try {
				deleteLocks.add(schema.getId());
				logger.trace("kraken logstorage: try to purge index [{}] of table [{}], day [{}]",
						new Object[] { schema.getIndexName(), schema.getTableName(), dayStr });
				ensureDelete(f);
			} finally {
				deleteLocks.remove(schema.getId());
			}
		}
	}

	private class LogReceiver implements LogCallback {

		@Override
		public void onLog(Log log) {
			Set<LogIndexSchema> indexes = tableIndexes.get(log.getTableName());
			if (indexes == null)
				return;

			int tableId = tableRegistry.getTableId(log.getTableName());
			for (LogIndexSchema index : indexes) {
				try {
					// skip if delete locked
					if (deleteLocks.contains(index.getId()))
						continue;

					OnlineIndexer indexer = getOnlineIndexer(new OnlineIndexerKey(index.getId(), log.getDay(), tableId,
							index.getTableName(), index.getIndexName()));
					indexer.write(log);
				} catch (IOException e) {
					String msg = "kraken logstorage: cannot index log, table " + index.getTableName() + ", index "
							+ index.getIndexName();
					logger.error(msg, e);
				}
			}
		}
	}

	@Override
	public List<LogIndexerStatus> getIndexerStatuses() {
		List<LogIndexerStatus> indexers = new ArrayList<LogIndexerStatus>(onlineIndexers.size());
		for (OnlineIndexer indexer : onlineIndexers.values()) {
			LogIndexerStatus s = new LogIndexerStatus();
			s.setTableName(indexer.tableName);
			s.setIndexName(indexer.indexName);
			s.setDay(indexer.day);
			s.setQueueCount(indexer.queue.size());
			s.setLastFlush(indexer.getLastFlush());
			indexers.add(s);
		}

		return indexers;
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

	private OnlineIndexer loadNewIndexer(OnlineIndexerKey key) throws IOException {
		OnlineIndexer online = null;
		LogIndexSchema schema = getIndexSchema(key.tableName, key.indexName);
		IndexTokenizer tok = tokenizerRegistry.newTokenizer(schema.getTokenizerName(), schema.getTokenizerConfigs());
		OnlineIndexer newWriter = new OnlineIndexer(key.tableName, key.indexName, key.tableId, key.indexId, key.day, tok);
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

	@Override
	public List<BatchIndexingTask> getBatchIndexingTasks() {
		// TODO: clone each items (prevent modification)
		return new ArrayList<BatchIndexingTask>(batchJobs.values());
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
						cleanBatchJobs();
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
				// skip delete locked index
				if (deleteLocks.contains(indexer.id))
					continue;

				boolean doFlush = (now - indexer.getLastFlush().getTime()) > 10000 || indexer.needFlush();
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
				logger.trace("kraken logstorage: closing index writer [{}]", indexer);
				indexer.close();
				onlineIndexers.remove(new OnlineIndexerKey(indexer.id, indexer.day, indexer.tableId, indexer.tableName,
						indexer.indexName));
			}
		}

		private void cleanBatchJobs() {
			for (BatchIndexKey key : new ArrayList<BatchIndexKey>(batchJobs.keySet())) {
				BatchIndexingTask task = batchJobs.get(key);
				if (task.isDone()) {
					batchJobs.remove(key);
					logger.info("kraken logstorage: batch indexing job [table={}, index={}] is completed", key.tableName,
							key.indexName);
				}
			}
		}
	}

	private class OnlineIndexer {
		private int id;

		/**
		 * only yyyy-MM-dd (excluding hour, min, sec, milli)
		 */
		private Date day;

		private int tableId;

		private String tableName;

		private String indexName;

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

		private File indexFile;
		private File dataFile;

		public OnlineIndexer(String tableName, String indexName, int tableId, int indexId, Date day, IndexTokenizer tokenizer)
				throws IOException {
			this.tableName = tableName;
			this.indexName = indexName;
			this.tableId = tableId;
			this.id = indexId;
			this.day = day;
			this.tokenizer = tokenizer;
			this.queue = new ArrayList<InvertedIndexItem>(10000);
			this.indexFile = getIndexFilePath(tableId, indexId, day, ".pos");
			this.dataFile = getIndexFilePath(tableId, indexId, day, ".seg");
			this.writer = new InvertedIndexWriter(indexFile, dataFile);
		}

		public void prepareMerge() {
			synchronized (this) {
				this.merging = true;
				writer.close();
			}
		}

		public void finishMerge() throws IOException {
			synchronized (this) {
				writer = new InvertedIndexWriter(indexFile, dataFile);
				this.merging = false;
			}
		}

		public boolean isOpen() {
			return writer != null && closing == false;
		}

		public boolean isClosed() {
			return closing == true && writer == null;
		}

		public Date getLastFlush() {
			return writer.getLastFlush();
		}

		public void write(Log log) throws IOException {
			if (logger.isDebugEnabled())
				logger.debug("kraken logstorage: write to index, {}", log.getData());

			Set<String> tokens = tokenizer.tokenize(log.getData());
			if (tokens == null)
				return;

			long timestamp = log.getDate().getTime();
			synchronized (this) {
				queue.add(new InvertedIndexItem(log.getTableName(), timestamp, log.getId(), tokens.toArray(new String[0])));
				if (logger.isDebugEnabled())
					logger.debug("kraken logstorage: queued tokens for index, {}", tokens);

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

		@Override
		public String toString() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return "id=" + id + ", day=" + dateFormat.format(day);
		}
	}

	private class TableEventListener implements LogTableEventListener {
		@Override
		public void onCreate(String tableName, Map<String, String> tableMetadata) {
			tableNameIdMap.put(tableName, tableRegistry.getTableId(tableName));
		}

		@Override
		public void onDrop(String tableName) {
			// cancel index build job
			for (BatchIndexKey key : new ArrayList<BatchIndexKey>(batchJobs.keySet())) {
				if (!key.tableName.equals(tableName))
					continue;

				BatchIndexingTask task = batchJobs.remove(key);
				task.setCanceled(true);
			}

			// delete index
			logger.info("kraken logstorage: dropping all indexes of table " + tableName);
			dropAllIndexes(tableName);

			tableNameIdMap.remove(tableName);
		}
	}
}
