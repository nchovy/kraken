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
package org.krakenapps.logstorage.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.api.PrimitiveConverter;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.confdb.Config;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.confdb.Predicates;
import org.krakenapps.logstorage.CachedRandomSeeker;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogCallback;
import org.krakenapps.logstorage.LogCursor;
import org.krakenapps.logstorage.LogKey;
import org.krakenapps.logstorage.LogRestoreService;
import org.krakenapps.logstorage.LogRetentionPolicy;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogStorageStatus;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.LogWriterStatus;
import org.krakenapps.logstorage.file.LogFileFixReport;
import org.krakenapps.logstorage.file.LogFileReader;
import org.krakenapps.logstorage.file.LogFileReaderV2;
import org.krakenapps.logstorage.file.LogFileRepairer;
import org.krakenapps.logstorage.file.LogRecord;
import org.krakenapps.logstorage.file.LogRecordCallback;
import org.krakenapps.logstorage.file.LogRecordCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-engine")
@Provides
public class LogStorageEngine implements LogStorage {
	private final Logger logger = LoggerFactory.getLogger(LogStorageEngine.class.getName());

	private static final int DEFAULT_LOG_CHECK_INTERVAL = 1000;
	private static final int DEFAULT_MAX_IDLE_TIME = 600000; // 10min
	private static final int DEFAULT_LOG_FLUSH_INTERVAL = 60000; // 60sec
	private static final int DEFAULT_BLOCK_SIZE = 640 * 1024; // 640KB

	private LogStorageStatus status = LogStorageStatus.Closed;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private ConfigService conf;

	@Requires
	private LogRestoreService logRestore;

	// online writers
	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;

	private CopyOnWriteArraySet<LogCallback> callbacks;

	// sweeping and flushing data
	private WriterSweeper writerSweeper;
	private Thread writerSweeperThread;

	private LogFileFetcher fetcher;

	private File logDir;

	public LogStorageEngine() {
		int checkInterval = getIntParameter(Constants.LogCheckInterval, DEFAULT_LOG_CHECK_INTERVAL);
		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);

		onlineWriters = new ConcurrentHashMap<OnlineWriterKey, OnlineWriter>();
		writerSweeper = new WriterSweeper(checkInterval, maxIdleTime, flushInterval);
		callbacks = new CopyOnWriteArraySet<LogCallback>();

		logDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/log");
		logDir = new File(getStringParameter(Constants.LogStorageDirectory, logDir.getAbsolutePath()));
		logDir.mkdirs();
		DatapathUtil.setLogDir(logDir);
	}

	@Override
	public File getDirectory() {
		return logDir;
	}

	@Override
	public void setDirectory(File f) {
		if (f == null)
			throw new IllegalArgumentException("storage path should be not null");

		if (!f.isDirectory())
			throw new IllegalArgumentException("storage path should be directory");

		ConfigUtil.set(conf, Constants.LogStorageDirectory, f.getAbsolutePath());
		logDir = f;
		DatapathUtil.setLogDir(logDir);
	}

	private String getStringParameter(Constants key, String defaultValue) {
		String value = ConfigUtil.get(conf, key);
		if (value != null)
			return value;
		return defaultValue;
	}

	private int getIntParameter(Constants key, int defaultValue) {
		String value = ConfigUtil.get(conf, key);
		if (value != null)
			return Integer.valueOf(value);
		return defaultValue;
	}

	@Override
	public LogStorageStatus getStatus() {
		return status;
	}

	@Validate
	@Override
	public void start() {
		if (status != LogStorageStatus.Closed)
			throw new IllegalStateException("log archive already started");

		status = LogStorageStatus.Starting;
		fetcher = new LogFileFetcher(tableRegistry);

		// checkAllLogFiles();
		checkLatestLogFiles();

		writerSweeperThread = new Thread(writerSweeper, "LogStorage LogWriter Sweeper");
		writerSweeperThread.start();

		status = LogStorageStatus.Open;
	}

	@Invalidate
	@Override
	public void stop() {
		if (status != LogStorageStatus.Open)
			throw new IllegalStateException("log archive already stopped");

		status = LogStorageStatus.Stopping;

		writerSweeper.doStop = true;
		writerSweeperThread.interrupt();

		// wait writer sweeper stop
		try {
			for (int i = 0; i < 25; i++) {
				if (writerSweeper.isStopped)
					break;

				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
		}

		// close all writers
		for (OnlineWriterKey key : onlineWriters.keySet()) {
			OnlineWriter writer = onlineWriters.get(key);
			if (writer != null)
				writer.close();
		}

		onlineWriters.clear();

		status = LogStorageStatus.Closed;
	}

	@SuppressWarnings("unused")
	private void checkAllLogFiles() {
		logger.info("kraken logstorage: verifying all log tables");
		for (String tableName : tableRegistry.getTableNames()) {
			File dir = getTableDirectory(tableName);
			if (dir == null) {
				logger.error("kraken logstorage: table [{}] directory not found", tableName);
				continue;
			}

			logger.trace("kraken logstorage: checking for [{}] table", tableName);

			File[] files = dir.listFiles();
			if (files == null)
				continue;

			for (File f : files) {
				if (f.getName().endsWith(".idx")) {
					String datFileName = f.getName().replace(".idx", ".dat");
					File indexPath = f;
					File dataPath = new File(dir, datFileName);

					try {
						LogFileFixReport report = new LogFileRepairer().fix(indexPath, dataPath);
						if (report != null)
							logger.info("kraken logstorage: fixed log table [{}], detail report: \n{}", tableName, report);
					} catch (IOException e) {
						logger.error("kraken logstorage: cannot fix index [" + indexPath.getAbsoluteFile() + "], data ["
								+ dataPath.getAbsolutePath() + "]", e);
					}
				}
			}
		}
		logger.info("kraken logstorage: all table verifications are completed");
	}

	private void checkLatestLogFiles() {
		logger.info("kraken logstorage: verifying all log tables");
		for (String tableName : tableRegistry.getTableNames()) {
			File dir = getTableDirectory(tableName);
			if (dir == null) {
				logger.error("kraken logstorage: table [{}] directory not found", tableName);
				continue;
			}

			logger.trace("kraken logstorage: checking for [{}] table", tableName);

			File[] files = dir.listFiles();
			if (files == null)
				continue;

			long lastModified = 0;
			File lastModifiedFile = null;

			// max lastmodified
			for (File f : files) {
				if (f.getName().endsWith(".idx") && lastModified < f.lastModified()) {
					lastModified = f.lastModified();
					lastModifiedFile = f;
				}
			}

			if (lastModifiedFile == null) {
				logger.trace("kraken logstorage: empty table [{}], skip verification", tableName);
				continue;
			}

			if (logger.isDebugEnabled())
				logger.debug("kraken logstorage: table [{}], last modified [{}]", tableName, new Date(lastModified));

			String datFileName = lastModifiedFile.getName().replace(".idx", ".dat");
			File indexPath = lastModifiedFile;
			File dataPath = new File(dir, datFileName);

			try {
				LogFileFixReport report = new LogFileRepairer().fix(indexPath, dataPath);
				if (report != null)
					logger.info("kraken logstorage: fixed log table [{}], detail report: \n{}", tableName, report);
			} catch (IOException e) {
				logger.error(
						"kraken logstorage: cannot fix index [" + indexPath.getAbsoluteFile() + "], data ["
								+ dataPath.getAbsolutePath() + "]", e);
				logRestore.restoreByDelete(tableName);
			}
		}
		logger.info("kraken logstorage: all table verifications are completed");
	}

	@Override
	public void createTable(String tableName) {
		createTable(tableName, null);
	}

	@Override
	public void createTable(String tableName, Map<String, String> tableMetadata) {
		tableRegistry.createTable(tableName, tableMetadata);
	}

	@Override
	public void dropTable(String tableName) {
		int tableId = tableRegistry.getTableId(tableName);
		Collection<Date> dates = getLogDates(tableName);

		// drop retention policy
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", tableName));
		if (c != null)
			c.remove();

		// drop table metadata
		tableRegistry.dropTable(tableName);

		// evict online writers
		for (Date day : dates) {
			OnlineWriterKey key = new OnlineWriterKey(tableName, day);
			OnlineWriter writer = onlineWriters.get(key);
			if (writer != null) {
				writer.close();
				logger.trace("kraken logstorage: removing logger [{}] according to table drop", key);
				onlineWriters.remove(key);
			}
		}

		// purge existing files
		File tableDir = getTableDirectory(tableId);
		if (!tableDir.exists())
			return;

		// delete all .idx and .dat files
		for (File f : tableDir.listFiles()) {
			if (f.isFile() && (f.getName().endsWith(".idx") || f.getName().endsWith(".dat"))) {
				if (!f.delete())
					logger.error("kraken logstorage: cannot delete log data {} of table {}", f.getAbsolutePath(), tableName);
			}
		}

		// delete directory if empty
		if (tableDir.listFiles().length == 0) {
			logger.info("kraken logstorage: deleted table {} directory", tableName);
			tableDir.delete();
		}
	}

	@Override
	public LogRetentionPolicy getRetentionPolicy(String tableName) {
		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", tableName));
		if (c == null)
			return null;
		return c.getDocument(LogRetentionPolicy.class);
	}

	@Override
	public void setRetentionPolicy(LogRetentionPolicy policy) {
		if (policy == null)
			throw new IllegalArgumentException("policy should not be null");

		ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
		Config c = db.findOne(LogRetentionPolicy.class, Predicates.field("table_name", policy.getTableName()));
		if (c == null) {
			db.add(policy);
		} else {
			c.setDocument(PrimitiveConverter.serialize(policy));
			c.update();
		}
	}

	@Override
	public File getTableDirectory(String tableName) {
		if (!tableRegistry.exists(tableName))
			throw new IllegalArgumentException("table not exists: " + tableName);

		int tableId = tableRegistry.getTableId(tableName);
		return getTableDirectory(tableId);
	}

	private File getTableDirectory(int tableId) {
		return new File(logDir, Integer.toString(tableId));
	}

	@Override
	public Collection<Date> getLogDates(String tableName) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int tableId = tableRegistry.getTableId(tableName);

		File tableDir = getTableDirectory(tableId);
		File[] files = tableDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".idx");
			}
		});

		List<Date> dates = new ArrayList<Date>();
		if (files != null) {
			for (File file : files) {
				try {
					dates.add(dateFormat.parse(file.getName().split("\\.")[0]));
				} catch (ParseException e) {
					logger.error("kraken logstorage: invalid log filename, table {}, {}", tableName, file.getName());
				}
			}
		}

		Collections.sort(dates, Collections.reverseOrder());

		return dates;
	}

	@Override
	public void write(Log log) {
		verify();

		// write data
		String tableName = log.getTableName();
		LogRecord record = convert(log);

		for (int i = 0; i < 2; i++) {
			try {
				OnlineWriter writer = getOnlineWriter(tableName, log.getDate());
				writer.write(record);

				log.setId(record.getId());
				break;
			} catch (IOException e) {
				if (e.getMessage().contains("closed")) {
					logger.info("closed online writer: trying one more time");
					continue;
				}

				throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());
			}
		}

		if (record.getId() == 0)
			throw new IllegalStateException("cannot write log: " + tableName + ", " + log.getDate());

		// invoke log callbacks
		for (LogCallback callback : callbacks) {
			try {
				callback.onLog(log);
			} catch (Exception e) {
				logger.warn("kraken logstorage: log callback should not throw any exception", e);
			}
		}
	}

	@Override
	public void write(Collection<Log> logs) {
		for (Log log : logs)
			write(log);
	}

	private LogRecord convert(Log log) {
		int length = EncodingRule.lengthOf(log.getData());
		ByteBuffer bb = ByteBuffer.allocate(length);
		EncodingRule.encode(bb, log.getData());
		bb.flip();
		LogRecord logdata = new LogRecord(log.getDate(), log.getId(), bb);
		return logdata;
	}

	@Override
	public Collection<Log> getLogs(String tableName, Date from, Date to, int limit) {
		return getLogs(tableName, from, to, 0, limit);
	}

	@Override
	public Collection<Log> getLogs(String tableName, Date from, Date to, int offset, int limit) {
		final List<Log> logs = new ArrayList<Log>(limit);
		try {
			search(tableName, from, to, offset, limit, new LogSearchCallback() {
				@Override
				public void onLog(Log log) {
					logs.add(log);
				}

				@Override
				public boolean isInterrupted() {
					return false;
				}

				@Override
				public void interrupt() {
				}
			});
		} catch (InterruptedException e) {
			throw new RuntimeException("interrupted");
		}
		return logs;
	}

	@Override
	public Date getPurgeBaseline(String tableName) {
		LogRetentionPolicy p = getRetentionPolicy(tableName);
		if (p == null || p.getRetentionDays() == 0)
			return null;

		Collection<Date> logDays = getLogDates(tableName);
		Date lastLogDay = getMaxDay(logDays.iterator());
		if (lastLogDay == null)
			return null;

		return getBaseline(lastLogDay, p.getRetentionDays());
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

	@Override
	public void purge(String tableName, Date fromDay, Date toDay) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		File dir = getTableDirectory(tableName);

		String from = "unbound";
		if (fromDay != null)
			from = dateFormat.format(fromDay);
		String to = "unbound";
		if (toDay != null)
			to = dateFormat.format(toDay);

		logger.debug("kraken logstorage: try to purge log data of table [{}], range [{}~{}]",
				new Object[] { tableName, from, to });

		for (File f : dir.listFiles()) {
			if (!f.isFile())
				continue;

			String fileName = f.getName();
			if (!fileName.endsWith(".idx") && !fileName.endsWith(".dat"))
				continue;

			String dayStr = fileName.substring(0, fileName.indexOf('.'));
			Date day = null;
			try {
				day = dateFormat.parse(dayStr);
			} catch (ParseException e) {
				continue;
			}

			// check range
			if (fromDay != null && day.before(fromDay))
				continue;

			if (toDay != null && day.after(toDay))
				continue;

			// TODO: lock and ensure delete
			logger.debug("kraken logstorage: try to purge log data of table [{}], day [{}]", tableName, dayStr);
			ensureDelete(f);
		}
	}

	private boolean ensureDelete(File f) {
		final int MAX_TIMEOUT = 30000;

		long begin = System.currentTimeMillis();

		while (true) {
			if (f.delete()) {
				logger.trace("kraken logstorage: deleted log file [{}]", f.getAbsolutePath());
				return true;
			}

			if (System.currentTimeMillis() - begin > MAX_TIMEOUT) {
				logger.error("kraken logstorage: delete timeout, cannot delete log file [{}]", f.getAbsolutePath());
				return false;
			}
		}
	}

	@Override
	public CachedRandomSeeker openCachedRandomSeeker() {
		verify();

		return new CachedRandomSeekerImpl(tableRegistry, fetcher, onlineWriters);
	}

	@Override
	public Log getLog(LogKey logKey) {
		String tableName = tableRegistry.getTableName(logKey.getTableId());
		return getLog(tableName, logKey.getDay(), logKey.getLogId());
	}

	@Override
	public Log getLog(String tableName, Date day, int id) {
		verify();

		// check memory buffer (flush waiting)
		OnlineWriter writer = onlineWriters.get(new OnlineWriterKey(tableName, day));
		if (writer != null) {
			for (LogRecord r : writer.getBuffer())
				if (r.getId() == id)
					return new Log(tableName, r.getDate(), id, EncodingRule.decodeMap(r.getData().duplicate()));
		}

		// load from disk
		LogFileReader reader = null;
		try {
			reader = fetcher.fetch(tableName, day);
			LogRecord logdata = reader.find(id);
			if (logdata == null) {
				if (logger.isTraceEnabled()) {
					String dayText = DateUtil.getDayText(day);
					logger.trace("kraken logstorage: log [table={}, date={}, id={}] not found", new Object[] { tableName,
							dayText, id });
				}
				return null;
			}
			return LogMarshaler.convert(tableName, logdata);
		} catch (IOException e) {
			throw new IllegalStateException("cannot read log: " + tableName + ", " + day + ", " + id);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("kraken logstorage: cannot close logfile reader", e);
				}
		}
	}

	@Override
	public LogCursor openCursor(String tableName, Date day, boolean ascending) throws IOException {
		verify();

		OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day));
		ArrayList<LogRecord> buffer = null;
		if (onlineWriter != null)
			buffer = (ArrayList<LogRecord>) onlineWriter.getBuffer();

		int tableId = tableRegistry.getTableId(tableName);
		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		File dataPath = DatapathUtil.getDataFile(tableId, day);
		LogFileReaderV2 reader = (LogFileReaderV2) LogFileReader.getLogFileReader(indexPath, dataPath);

		return new LogCursorImpl(tableName, day, buffer, reader, ascending);
	}

	@Override
	public int search(Date from, Date to, int limit, LogSearchCallback callback) throws InterruptedException {
		return search(from, to, 0, limit, callback);
	}

	@Override
	public int search(Date from, Date to, int offset, int limit, LogSearchCallback callback) throws InterruptedException {
		verify();

		int found = 0;
		for (String tableName : tableRegistry.getTableNames()) {
			int needed = limit - found;
			if (needed <= 0)
				break;

			found += search(tableName, from, to, offset, needed, callback);

			if (offset > 0) {
				if (found > offset) {
					found -= offset;
					offset = 0;
				} else {
					offset -= found;
					found = 0;
				}
			}
		}

		return found;
	}

	@Override
	public int search(String tableName, Date from, Date to, int limit, LogSearchCallback callback) throws InterruptedException {
		return search(tableName, from, to, 0, limit, callback);
	}

	@Override
	public int search(String tableName, Date from, Date to, int offset, int limit, LogSearchCallback callback)
			throws InterruptedException {
		verify();

		Collection<Date> days = getLogDates(tableName);

		int found = 0;
		List<Date> filtered = DateUtil.filt(days, from, to);
		logger.trace("kraken logstorage: searching {} tablets of table [{}]", filtered.size(), tableName);

		for (Date day : filtered) {
			if (logger.isTraceEnabled())
				logger.trace("kraken logstorage: searching table {}, date={}", tableName, DateUtil.getDayText(day));

			int needed = limit - found;
			if (limit != 0 && needed <= 0)
				break;

			found += searchTablet(tableName, day, from, to, offset, needed, callback);

			if (offset > 0) {
				if (found > offset) {
					found -= offset;
					offset = 0;
				} else {
					offset -= found;
					found = 0;
				}
			}
		}

		return found;
	}

	private int searchTablet(String tableName, Date day, Date from, Date to, int offset, int limit,
			final LogSearchCallback callback) throws InterruptedException {
		int tableId = tableRegistry.getTableId(tableName);

		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		File dataPath = DatapathUtil.getDataFile(tableId, day);
		LogFileReader reader = null;
		TraverseCallback c = new TraverseCallback(tableName, from, to, callback);

		try {
			// do NOT use getOnlineWriter() here (it loads empty writer on cache
			// automatically if writer not found)
			OnlineWriter onlineWriter = onlineWriters.get(new OnlineWriterKey(tableName, day));
			if (onlineWriter != null) {
				List<LogRecord> buffer = onlineWriter.getBuffer();

				if (buffer != null && !buffer.isEmpty()) {
					logger.trace("kraken logstorage: {} logs in writer buffer.", buffer.size());
					ListIterator<LogRecord> li = buffer.listIterator(buffer.size());
					while (li.hasPrevious()) {
						LogRecord logData = li.previous();
						if ((from == null || logData.getDate().after(from)) && (to == null || logData.getDate().before(to))) {
							if (offset > 0) {
								offset--;
								continue;
							}

							if (c.onLog(logData)) {
								if (--limit == 0)
									return c.matched;
							}
						}
					}
				}
			}

			reader = LogFileReader.getLogFileReader(indexPath, dataPath);
			reader.traverse(from, to, offset, limit, c);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("kraken logstorage: search tablet failed", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("kraken logstorage: search tablet close failed", e);
				}
		}

		return c.matched;
	}

	private class TraverseCallback implements LogRecordCallback {
		private Logger logger = LoggerFactory.getLogger(TraverseCallback.class);
		private String tableName;
		private Date from;
		private Date to;
		private LogSearchCallback callback;
		private int matched = 0;

		public TraverseCallback(String tableName, Date from, Date to, LogSearchCallback callback) {
			this.tableName = tableName;
			this.from = from;
			this.to = to;
			this.callback = callback;
		}

		@Override
		public boolean onLog(LogRecord logData) throws InterruptedException {
			Date d = logData.getDate();
			if (from != null && d.before(from))
				return false;
			if (to != null && d.after(to))
				return false;

			if (callback.isInterrupted())
				throw new InterruptedException("interrupted log traverse");

			try {
				matched++;

				Log log = LogMarshaler.convert(tableName, logData);
				logger.debug("kraken logdb: traverse log [{}]", log);
				callback.onLog(log);

				return true;
			} catch (Exception e) {
				if (callback.isInterrupted())
					throw new InterruptedException("interrupted log traverse");
				else
					throw new RuntimeException(e);
			}
		}
	}

	private OnlineWriter getOnlineWriter(String tableName, Date date) {
		// check table existence
		int tableId = tableRegistry.getTableId(tableName);

		Date day = DateUtil.getDay(date);
		OnlineWriterKey key = new OnlineWriterKey(tableName, day);

		OnlineWriter online = onlineWriters.get(key);
		if (online != null && online.isOpen())
			return online;

		try {
			int blockSize = getIntParameter(Constants.LogBlockSize, DEFAULT_BLOCK_SIZE);
			OnlineWriter oldWriter = onlineWriters.get(key);
			String defaultLogVersion = tableRegistry.getTableMetadata(tableName, "logversion");

			if (oldWriter != null) {
				synchronized (oldWriter) {
					if (!oldWriter.isOpen() && !oldWriter.isClosed()) { // closing
						while (!oldWriter.isClosed()) {
							try {
								oldWriter.wait(1000);
							} catch (InterruptedException e) {
							}
						}
						while (onlineWriters.get(key) == oldWriter) {
							Thread.yield();
						}
						OnlineWriter newWriter = new OnlineWriter(tableId, day, blockSize, defaultLogVersion);
						OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
						if (consensus == null)
							online = newWriter;
						else {
							online = consensus;
							if (consensus != newWriter)
								newWriter.close();
						}
					} else if (oldWriter.isClosed()) {
						while (onlineWriters.get(key) == oldWriter) {
							Thread.yield();
						}
						OnlineWriter newWriter = new OnlineWriter(tableId, day, blockSize, defaultLogVersion);
						OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
						if (consensus == null)
							online = newWriter;
						else {
							online = consensus;
							if (consensus != newWriter)
								newWriter.close();
						}
					} else {
						online = oldWriter;
					}
				}
			} else {
				OnlineWriter newWriter = new OnlineWriter(tableId, day, blockSize, defaultLogVersion);
				OnlineWriter consensus = onlineWriters.putIfAbsent(key, newWriter);
				if (consensus == null)
					online = newWriter;
				else {
					online = consensus;
					if (consensus != newWriter)
						newWriter.close();
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("cannot open writer: " + tableName + ", date=" + day, e);
		}

		return online;
	}

	@Override
	public void reload() {
		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);
		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
		writerSweeper.setFlushInterval(flushInterval);
		writerSweeper.setMaxIdleTime(maxIdleTime);
	}

	@Override
	public void flush() {
		writerSweeper.setForceFlush(true);
		writerSweeperThread.interrupt();
	}

	@Override
	public void addLogListener(LogCallback callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeLogListener(LogCallback callback) {
		callbacks.remove(callback);
	}

	private void verify() {
		if (status != LogStorageStatus.Open)
			throw new IllegalStateException("archive not opened");
	}

	@Override
	public List<LogWriterStatus> getWriterStatuses() {
		List<LogWriterStatus> writers = new ArrayList<LogWriterStatus>(onlineWriters.size());
		for (OnlineWriterKey key : onlineWriters.keySet()) {
			OnlineWriter writer = onlineWriters.get(key);
			LogWriterStatus s = new LogWriterStatus();
			s.setTableName(key.getTableName());
			s.setDay(key.getDay());
			s.setLastWrite(writer.getLastAccess());
			writers.add(s);
		}

		return writers;
	}

	private class WriterSweeper implements Runnable {
		private final Logger logger = LoggerFactory.getLogger(WriterSweeper.class.getName());
		private volatile int checkInterval;
		private volatile int maxIdleTime;
		private volatile int flushInterval;

		private volatile boolean doStop = false;
		private volatile boolean isStopped = true;
		private volatile boolean forceFlush = false;

		public WriterSweeper(int checkInterval, int maxIdleTime, int flushInterval) {
			this.checkInterval = checkInterval;
			this.maxIdleTime = maxIdleTime;
			this.flushInterval = flushInterval;
		}

		public void setFlushInterval(int flushInterval) {
			this.flushInterval = flushInterval;
		}

		public void setMaxIdleTime(int maxIdleTime) {
			this.maxIdleTime = maxIdleTime;
		}

		public void setForceFlush(boolean forceFlush) {
			this.forceFlush = forceFlush;
		}

		@Override
		public void run() {
			try {
				isStopped = false;

				while (true) {
					try {
						if (doStop)
							break;

						Thread.sleep(checkInterval);
						sweep();
					} catch (InterruptedException e) {
						if (forceFlush) {
							sweep();
							forceFlush = false;
						}
						logger.trace("kraken logstorage: sweeper interrupted");
					} catch (Exception e) {
						logger.error("krakne logstorage: sweeper error", e);
					}
				}
			} finally {
				doStop = false;
				isStopped = true;
			}

			logger.info("kraken logstorage: writer sweeper stopped");
		}

		private void sweep() {
			List<OnlineWriterKey> evicts = new ArrayList<OnlineWriterKey>();
			long now = new Date().getTime();
			try {
				// periodic log flush
				for (OnlineWriterKey key : onlineWriters.keySet()) {
					OnlineWriter writer = onlineWriters.get(key);
					boolean doFlush = (now - writer.getLastFlush().getTime()) > flushInterval;
					if (doFlush) {
						try {
							logger.trace("kraken logstorage: flushing writer [{}]", key);
							writer.flush();
						} catch (IOException e) {
							logger.error("kraken logstorage: log flush failed", e);
						}
					}

					// close file if writer is in idle state
					int interval = (int) (now - writer.getLastAccess().getTime());
					if (interval > maxIdleTime)
						evicts.add(key);
				}
			} catch (ConcurrentModificationException e) {
			}

			for (OnlineWriterKey key : evicts) {
				OnlineWriter evictee = onlineWriters.get(key);
				if (evictee != null) {
					evictee.close();
					logger.trace("kraken logstorage: evict logger [{}]", key);
					onlineWriters.remove(key);
				}
			}
		}
	}

	private static class LogCursorImpl implements LogCursor {
		private String tableName;
		private Date day;
		private ArrayList<LogRecord> buffer;
		private LogFileReaderV2 reader;
		private LogRecordCursor cursor;
		private boolean ascending;

		private Log prefetch;
		private int bufferNext;
		private int bufferTotal;

		public LogCursorImpl(String tableName, Date day, ArrayList<LogRecord> buffer, LogFileReaderV2 reader, boolean ascending) {
			this.tableName = tableName;
			this.day = day;
			this.reader = reader;
			this.cursor = reader.getCursor(ascending);
			this.ascending = ascending;

			if (buffer != null) {
				this.buffer = buffer;
				this.bufferTotal = buffer.size();
				this.bufferNext = ascending ? 0 : bufferTotal - 1;
			}
		}

		@Override
		public boolean hasNext() {
			if (prefetch != null)
				return true;

			if (ascending) {
				if (cursor.hasNext()) {
					prefetch = LogMarshaler.convert(tableName, cursor.next());
					return true;
				}

				if (bufferNext < bufferTotal) {
					LogRecord r = buffer.get(bufferNext++);
					prefetch = LogMarshaler.convert(tableName, r);
					return true;
				}

				return false;
			} else {
				if (bufferNext < 0) {
					LogRecord r = buffer.get(bufferNext--);
					prefetch = LogMarshaler.convert(tableName, r);
					return true;
				}

				if (cursor.hasNext()) {
					prefetch = LogMarshaler.convert(tableName, cursor.next());
					return true;
				}

				return false;
			}
		}

		@Override
		public Log next() {
			if (!hasNext())
				throw new NoSuchElementException("end of log cursor");

			Log log = prefetch;
			prefetch = null;
			return log;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}

		@Override
		public String toString() {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			return "log cursor for table " + tableName + ", day " + dateFormat.format(day);
		}
	}
}
