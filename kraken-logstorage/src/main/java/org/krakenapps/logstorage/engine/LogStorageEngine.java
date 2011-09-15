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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.codec.EncodingRule;
import org.krakenapps.logstorage.DiskLackAction;
import org.krakenapps.logstorage.DiskLackCallback;
import org.krakenapps.logstorage.DiskSpaceType;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogCallback;
import org.krakenapps.logstorage.LogKey;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogStorageStatus;
import org.krakenapps.logstorage.LogTableRegistry;
import org.osgi.service.prefs.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-engine")
@Provides
public class LogStorageEngine implements LogStorage {
	private final Logger logger = LoggerFactory.getLogger(LogStorageEngine.class.getName());
	private final Logger debuglogger = LoggerFactory.getLogger("OnlineWriterDebugLogger");
	private boolean isDebugloggerTraceEnabled = debuglogger.isTraceEnabled();

	private static final int DEFAULT_MAX_IDLE_TIME = 10000;
	private static final int DEFAULT_MAX_LOG_BUFFERING = 10000;
	private static final int DEFAULT_LOG_FLUSH_INTERVAL = 3600000;
	private static final String DEFAULT_MIN_FREE_SPACE_TYPE = DiskSpaceType.Percentage.toString();
	private static final int DEFAULT_MIN_FREE_SPACE_VALUE = 10;
	private static final String DEFAULT_DISK_LACK_ACTION = DiskLackAction.StopLogging.toString();

	private LogStorageStatus status = LogStorageStatus.Closed;

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private PreferencesService prefsvc;

	// online writers
	private ConcurrentMap<OnlineWriterKey, OnlineWriter> onlineWriters;

	private CopyOnWriteArraySet<LogCallback> callbacks;

	// sweeping and flushing data
	private WriterSweeper writerSweeper;
	private Thread writerSweeperThread;

	private static final File logDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/log");
	private DiskSpaceType minFreeSpaceType;
	private int minFreeSpaceValue;
	private DiskLackAction diskLackAction;
	private Set<DiskLackCallback> diskLackCallbacks = new HashSet<DiskLackCallback>();

	static {
		logDir.mkdirs();
	}

	public LogStorageEngine() {
		int maxIdleTime = getIntParameter(Constants.LogMaxIdleTime, DEFAULT_MAX_IDLE_TIME);
		int flushInterval = getIntParameter(Constants.LogFlushInterval, DEFAULT_LOG_FLUSH_INTERVAL);

		onlineWriters = new ConcurrentHashMap<OnlineWriterKey, OnlineWriter>();
		writerSweeper = new WriterSweeper(maxIdleTime, flushInterval);
		callbacks = new CopyOnWriteArraySet<LogCallback>();
		minFreeSpaceType = DiskSpaceType.valueOf(getStringParameter(Constants.MinFreeDiskSpaceType,
				DEFAULT_MIN_FREE_SPACE_TYPE));
		minFreeSpaceValue = getIntParameter(Constants.MinFreeDiskSpaceValue, DEFAULT_MIN_FREE_SPACE_VALUE);
		diskLackAction = DiskLackAction.valueOf(getStringParameter(Constants.DiskLackAction, DEFAULT_DISK_LACK_ACTION));
	}

	private String getStringParameter(Constants key, String defaultValue) {
		String value = ConfigUtil.get(prefsvc, key);
		if (value != null)
			return value;
		return defaultValue;
	}

	private int getIntParameter(Constants key, int defaultValue) {
		String value = ConfigUtil.get(prefsvc, key);
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

		writerSweeperThread = new Thread(writerSweeper, "LogStorage Sweeper");
		writerSweeperThread.start();

		status = LogStorageStatus.Open;

		isDebugloggerTraceEnabled = debuglogger.isTraceEnabled();

		File queryDir = new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/query");
		queryDir.mkdirs();

		for (File f : queryDir.listFiles()) {
			if ((f.getName().startsWith("fbl") || f.getName().startsWith("fbm")) && f.getName().endsWith(".buf"))
				f.delete();
		}
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

	@Override
	public void createTable(String tableName) {
		tableRegistry.createTable(tableName, null);
	}

	@Override
	public void dropTable(String tableName) {
		int tableId = tableRegistry.getTableId(tableName);
		Collection<Date> dates = getLogDates(tableName);

		// drop table metadata
		tableRegistry.dropTable(tableName);

		// evict online writers
		for (Date day : dates) {
			OnlineWriterKey key = new OnlineWriterKey(tableName, day);
			OnlineWriter writer = onlineWriters.get(key);
			if (writer != null) {
				writer.close();
				if (isDebugloggerTraceEnabled)
					debuglogger.trace("loggerRemoving: {}", key);
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
					logger.info("log storage: cannot delete log data {} of table {}", f.getAbsolutePath(), tableName);
			}
		}

		// delete directory if empty
		if (tableDir.listFiles().length == 0) {
			logger.info("log storage: deleted table {} directory", tableName);
			tableDir.delete();
		}
	}

	private File getTableDirectory(int tableId) {
		File tableDir = new File(new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/log"),
				Integer.toString(tableId));
		return tableDir;
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
	public int getMinFreeSpaceValue() {
		return minFreeSpaceValue;
	}

	@Override
	public DiskSpaceType getMinFreeSpaceType() {
		return minFreeSpaceType;
	}

	@Override
	public void setMinFreeSpace(int value, DiskSpaceType type) {
		if (type == DiskSpaceType.Percentage) {
			if (value <= 0 || value >= 100)
				throw new IllegalArgumentException("invalid value");
		} else if (type == DiskSpaceType.Megabyte) {
			if (value <= 0)
				throw new IllegalArgumentException("invalid value");
		} else if (type == null)
			throw new IllegalArgumentException("type cannot be null");

		this.minFreeSpaceType = type;
		this.minFreeSpaceValue = value;

		ConfigUtil.set(prefsvc, Constants.MinFreeDiskSpaceType, type.toString());
		ConfigUtil.set(prefsvc, Constants.MinFreeDiskSpaceValue, Integer.toString(value));
	}

	@Override
	public DiskLackAction getDiskLackAction() {
		return diskLackAction;
	}

	@Override
	public void setDiskLackAction(DiskLackAction action) {
		if (action == null)
			throw new IllegalArgumentException("action cannot be null");

		this.diskLackAction = action;

		ConfigUtil.set(prefsvc, Constants.DiskLackAction, action.toString());
	}

	@Override
	public void registerDiskLackCallback(DiskLackCallback callback) {
		diskLackCallbacks.add(callback);
	}

	@Override
	public void unregisterDiskLackCallback(DiskLackCallback callback) {
		diskLackCallbacks.remove(callback);
	}

	@Override
	public Log getLog(LogKey logKey) {
		String tableName = tableRegistry.getTableName(logKey.getTableId());
		return getLog(tableName, logKey.getDay(), logKey.getLogId());
	}

	@Override
	public Log getLog(String tableName, Date day, int id) {
		verify();

		int tableId = tableRegistry.getTableId(tableName);

		File indexPath = DatapathUtil.getIndexFile(tableId, day);
		if (!indexPath.exists())
			throw new IllegalStateException("log table not found: " + tableName + ", " + day);

		File dataPath = DatapathUtil.getDataFile(tableId, day);
		if (!dataPath.exists())
			throw new IllegalStateException("log table not found: " + tableName + ", " + day);

		LogFileReader reader = null;
		try {
			reader = LogFileReader.getLogFileReader(indexPath, dataPath);
			LogRecord logdata = reader.find(id);
			if (logdata == null) {
				if (logger.isTraceEnabled()) {
					String dayText = DateUtil.getDayText(day);
					logger.trace("log storage: log [table={}, date={}, id={}] not found", new Object[] { tableName,
							dayText, id });
				}
				return null;
			}
			return convert(tableName, logdata);
		} catch (IOException e) {
			throw new IllegalStateException("cannot read log: " + tableName + ", " + day + ", " + id);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("log storage: cannot close logfile reader", e);
				}
		}
	}

	private Log convert(String tableName, LogRecord logdata) {
		int pos = logdata.getData().position();
		Map<String, Object> m = EncodingRule.decodeMap(logdata.getData());
		logdata.getData().position(pos);
		return new Log(tableName, logdata.getDate(), logdata.getId(), m);
	}

	@Override
	public int search(Date from, Date to, int limit, LogSearchCallback callback) throws InterruptedException {
		return search(from, to, 0, limit, callback);
	}

	@Override
	public int search(Date from, Date to, int offset, int limit, LogSearchCallback callback)
			throws InterruptedException {
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
	public int search(String tableName, Date from, Date to, int limit, LogSearchCallback callback)
			throws InterruptedException {
		return search(tableName, from, to, 0, limit, callback);
	}

	@Override
	public int search(String tableName, Date from, Date to, int offset, int limit, LogSearchCallback callback)
			throws InterruptedException {
		verify();

		Collection<Date> days = getLogDates(tableName);

		int found = 0;
		List<Date> filtered = DateUtil.filt(days, from, to);
		logger.trace("log storage: searching {} tablets of table [{}]", filtered.size(), tableName);

		for (Date day : filtered) {
			if (logger.isTraceEnabled())
				logger.trace("log storage: searching table {}, date={}", tableName, DateUtil.getDayText(day));

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
		TraverseCallback c = new TraverseCallback(tableName, from, to, offset, callback);

		try {
			OnlineWriter onlineWriter = getOnlineWriter(tableName, day);
			List<LogRecord> buffer = onlineWriter.getBuffer();
			reader = LogFileReader.getLogFileReader(indexPath, dataPath);

			if (buffer != null) {
				logger.trace("kraken logstorage: {} logs in writer buffer.", buffer.size());
				ListIterator<LogRecord> li = buffer.listIterator(buffer.size());
				while (li.hasPrevious()) {
					LogRecord logData = li.previous();
					if ((from == null || logData.getDate().after(from)) && (to == null || logData.getDate().before(to))) {
						if (c.onLog(logData)) {
							if (--limit == 0)
								return c.matched;
						}
					}
				}
			}

			reader.traverse(from, to, limit, c);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("log storage: search tablet failed", e);
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("log storage: search tablet close failed", e);
				}
		}

		return c.matched;
	}

	private class TraverseCallback implements LogRecordCallback {
		private Logger logger = LoggerFactory.getLogger(TraverseCallback.class);
		private String tableName;
		private Date from;
		private Date to;
		private int offset;
		private LogSearchCallback callback;
		private int matched = 0;

		public TraverseCallback(String tableName, Date from, Date to, int offset, LogSearchCallback callback) {
			this.tableName = tableName;
			this.from = from;
			this.to = to;
			this.offset = offset;
			this.callback = callback;
		}

		@Override
		public boolean onLog(LogRecord logData) throws InterruptedException {
			Log log = convert(tableName, logData);
			logger.debug("log storage: traverse log [{}]", log);

			Date d = log.getDate();
			if (d.before(from) || d.after(to))
				return false;

			return onMatch(log);
		}

		private boolean onMatch(Log log) throws InterruptedException {
			if (callback.isInterrupted())
				throw new InterruptedException("interrupted log traverse");

			matched++;

			if (offset > 0) {
				offset--;
				return false;
			} else {
				try {
					callback.onLog(log);
				} catch (Exception e) {
					if (callback.isInterrupted())
						throw new InterruptedException("interrupted log traverse");
					else
						throw new RuntimeException(e);
				}
				return true;
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
			int maxBuffering = getIntParameter(Constants.LogMaxBuffering, DEFAULT_MAX_LOG_BUFFERING);
			OnlineWriter oldWriter = onlineWriters.get(key);
			String defaultLogVersion = tableRegistry.getTableMetadata(tableId).get("logversion");

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
						OnlineWriter newWriter = new OnlineWriter(tableId, day, maxBuffering, defaultLogVersion);
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
						OnlineWriter newWriter = new OnlineWriter(tableId, day, maxBuffering, defaultLogVersion);
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
				OnlineWriter newWriter = new OnlineWriter(tableId, day, maxBuffering, defaultLogVersion);
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

		minFreeSpaceType = DiskSpaceType.valueOf(getStringParameter(Constants.MinFreeDiskSpaceType,
				DEFAULT_MIN_FREE_SPACE_TYPE));
		minFreeSpaceValue = getIntParameter(Constants.MinFreeDiskSpaceValue, DEFAULT_MIN_FREE_SPACE_VALUE);
		diskLackAction = DiskLackAction.valueOf(getStringParameter(Constants.DiskLackAction, DEFAULT_DISK_LACK_ACTION));
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

	private class LogFile {
		private String tableName;
		private Date date;
		private File index;
		private File data;

		private LogFile(String tableName, Date date) {
			this.tableName = tableName;
			this.date = date;
			int tableId = tableRegistry.getTableId(tableName);
			File tableDir = getTableDirectory(tableId);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			this.index = new File(tableDir, sdf.format(date) + ".idx");
			this.data = new File(tableDir, sdf.format(date) + ".dat");
		}

		public void remove() {
			index.delete();
			data.delete();
		}
	}

	private class LogFileComparator implements Comparator<LogFile> {
		@Override
		public int compare(LogFile o1, LogFile o2) {
			return o1.date.compareTo(o2.date);
		}
	}

	private class WriterSweeper implements Runnable {
		private final Logger logger = LoggerFactory.getLogger(WriterSweeper.class.getName());
		private volatile int maxIdleTime;
		private volatile int flushInterval;

		private volatile boolean doStop = false;
		private volatile boolean isStopped = true;
		private volatile boolean forceFlush = false;

		public WriterSweeper(int maxIdleTime, int flushInterval) {
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
					if (isDiskLack()) {
						logger.warn("kraken logstorage: not enough disk space");
						if (diskLackAction == DiskLackAction.StopLogging) {
							logger.info("kraken logstorage: stop logging");
							stop();
						} else if (diskLackAction == DiskLackAction.RemoveOldLog) {
							List<LogFile> files = new ArrayList<LogFile>();
							for (String tableName : tableRegistry.getTableNames()) {
								for (Date date : getLogDates(tableName))
									files.add(new LogFile(tableName, date));
							}
							Collections.sort(files, new LogFileComparator());
							int index = 0;
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							do {
								if (index >= files.size()) {
									logger.info("kraken logstorage: stop logging");
									stop();
									break;
								}
								LogFile lf = files.get(index++);
								logger.info("kraken logstorage: remove old log, table {}, {}", lf.tableName,
										sdf.format(lf.date));
								lf.remove();
							} while (isDiskLack());

							for (DiskLackCallback callback : diskLackCallbacks)
								callback.callback();
						}
					}

					try {
						if (doStop)
							break;

						Thread.sleep(flushInterval);
						sweep();
					} catch (InterruptedException e) {
						if (forceFlush) {
							sweep();
							forceFlush = false;
						}
						logger.trace("log storage: sweeper interrupted");
					} catch (Exception e) {
						logger.error("log storage: sweeper error", e);
					}
				}
			} finally {
				doStop = false;
				isStopped = true;
			}

			logger.info("log storage: writer sweeper stopped");
		}

		private void sweep() {
			List<OnlineWriterKey> evicts = new ArrayList<OnlineWriterKey>();
			try {
				// periodic log flush
				for (OnlineWriterKey key : onlineWriters.keySet()) {
					OnlineWriter writer = onlineWriters.get(key);
					try {
						logger.trace("log storage: flushing writer [{}]", key);
						writer.flush();
					} catch (IOException e) {
						logger.error("log storage: log flush failed", e);
					}

					// close file if writer is in idle state
					int interval = (int) (new Date().getTime() - writer.getLastAccess().getTime());
					if (interval > maxIdleTime)
						evicts.add(key);
				}
			} catch (ConcurrentModificationException e) {
			}

			for (OnlineWriterKey key : evicts) {
				OnlineWriter evictee = onlineWriters.get(key);
				if (evictee != null) {
					evictee.close();
					if (isDebugloggerTraceEnabled)
						debuglogger.trace("loggerRemoving: {}", key);
					onlineWriters.remove(key);
				}
			}
		}

		private boolean isDiskLack() {
			long usable = logDir.getUsableSpace();
			long total = logDir.getTotalSpace();

			logger.trace("kraken logstorage: check disk lack, {} {}", minFreeSpaceValue, minFreeSpaceType);
			if (minFreeSpaceType == DiskSpaceType.Percentage) {
				int percent = (int) (usable * 100 / total);
				if (percent < minFreeSpaceValue) {
					logger.warn("kraken logstorage: setted minimum free space {}%, now free space {}%",
							minFreeSpaceValue, percent);
					return true;
				}
			} else if (minFreeSpaceType == DiskSpaceType.Megabyte) {
				int mega = (int) (usable / 1048576);
				if (mega < minFreeSpaceValue) {
					logger.warn("kraken logstorage: setted minimum free space {} MB, now free space {} MB",
							minFreeSpaceValue, mega);
					return true;
				}
			}

			return false;
		}
	}
}
