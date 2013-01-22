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
package org.krakenapps.logstorage.engine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.cron.PeriodicJob;
import org.krakenapps.logstorage.DiskLackAction;
import org.krakenapps.logstorage.DiskLackCallback;
import org.krakenapps.logstorage.DiskSpaceType;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogRetentionPolicy;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogStorageMonitor;
import org.krakenapps.logstorage.LogTableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PeriodicJob("* * * * *")
@Component(name = "logstorage-monitor")
@Provides
public class LogStorageMonitorEngine implements LogStorageMonitor {
	private static final String DEFAULT_MIN_FREE_SPACE_TYPE = DiskSpaceType.Percentage.toString();
	private static final int DEFAULT_MIN_FREE_SPACE_VALUE = 10;
	private static final String DEFAULT_DISK_LACK_ACTION = DiskLackAction.StopLogging.toString();

	private final Logger logger = LoggerFactory.getLogger(LogStorageMonitorEngine.class.getName());

	@Requires
	private LogTableRegistry tableRegistry;

	@Requires
	private LogStorage storage;

	@Requires
	private LogIndexer indexer;

	@Requires
	private ConfigService conf;

	// last check time, check retention policy and purge files for every hour
	private long lastPurgeCheck;

	private DiskSpaceType minFreeSpaceType;
	private int minFreeSpaceValue;
	private DiskLackAction diskLackAction;
	private Set<DiskLackCallback> diskLackCallbacks = new HashSet<DiskLackCallback>();

	public LogStorageMonitorEngine() {
		minFreeSpaceType = DiskSpaceType.valueOf(getStringParameter(Constants.MinFreeDiskSpaceType, DEFAULT_MIN_FREE_SPACE_TYPE));
		minFreeSpaceValue = getIntParameter(Constants.MinFreeDiskSpaceValue, DEFAULT_MIN_FREE_SPACE_VALUE);
		diskLackAction = DiskLackAction.valueOf(getStringParameter(Constants.DiskLackAction, DEFAULT_DISK_LACK_ACTION));
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

		ConfigUtil.set(conf, Constants.MinFreeDiskSpaceType, type.toString());
		ConfigUtil.set(conf, Constants.MinFreeDiskSpaceValue, Integer.toString(value));
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

		ConfigUtil.set(conf, Constants.DiskLackAction, action.toString());
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
	public void forceRetentionCheck() {
		checkRetentions(true);
	}

	private boolean isDiskLack() {
		File dir = storage.getDirectory();
		long usable = dir.getUsableSpace();
		long total = dir.getTotalSpace();

		logger.trace("kraken logstorage: check {} {} free space", minFreeSpaceValue, minFreeSpaceType.toString().toLowerCase());
		if (minFreeSpaceType == DiskSpaceType.Percentage) {
			int percent = (int) (usable * 100 / total);
			if (percent < minFreeSpaceValue) {
				return true;
			}
		} else if (minFreeSpaceType == DiskSpaceType.Megabyte) {
			int mega = (int) (usable / 1048576);
			if (mega < minFreeSpaceValue) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void run() {
		try {
			runOnce();
		} catch (Exception e) {
			logger.error("kraken logstorage: storage monitor error", e);
		}
	}

	private void runOnce() {
		checkRetentions(false);
		checkDiskLack();
	}

	private void checkRetentions(boolean force) {
		long now = System.currentTimeMillis();
		if (!force && now - lastPurgeCheck < 3600 * 1000)
			return;

		for (String tableName : tableRegistry.getTableNames()) {
			checkAndPurgeFiles(tableName);
		}

		lastPurgeCheck = now;
	}

	private void checkAndPurgeFiles(String tableName) {
		LogRetentionPolicy p = storage.getRetentionPolicy(tableName);
		if (p == null || p.getRetentionDays() == 0) {
			logger.debug("kraken logstorage: no retention policy for table [{}]", tableName);
			return;
		}

		int retentionDays = p.getRetentionDays();

		// purge tables
		Date logBaseline = storage.getPurgeBaseline(tableName);
		if (logBaseline != null)
			storage.purge(tableName, null, logBaseline);

		// purge index files
		for (String indexName : indexer.getIndexNames(tableName)) {
			Date indexBaseline = indexer.getPurgeBaseline(tableName, indexName);
			if (indexBaseline == null)
				continue;

			if (logger.isTraceEnabled()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				logger.trace("kraken logstorage: table [{}] retention days [{}] purging index [{}] baseline [{}]", new Object[] {
						tableName, retentionDays, indexName, dateFormat.format(indexBaseline) });
			}

			indexer.purge(tableName, indexName, null, indexBaseline);
		}
	}

	private void checkDiskLack() {
		if (isDiskLack()) {
			logger.warn("kraken logstorage: not enough disk space, current minimum free space config [{}] {}", minFreeSpaceValue,
					(minFreeSpaceType == DiskSpaceType.Percentage ? "%" : "MB"));
			if (diskLackAction == DiskLackAction.StopLogging) {
				logger.info("kraken logstorage: stop logging");
				storage.stop();
			} else if (diskLackAction == DiskLackAction.RemoveOldLog) {
				List<LogFile> files = new ArrayList<LogFile>();
				for (String tableName : tableRegistry.getTableNames()) {
					for (Date date : storage.getLogDates(tableName))
						files.add(new LogFile(tableName, date));
				}
				Collections.sort(files, new LogFileComparator());
				int index = 0;
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

				do {
					if (index >= files.size()) {
						logger.info("kraken logstorage: stop logging");
						storage.stop();
						break;
					}
					LogFile lf = files.get(index++);
					logger.info("kraken logstorage: remove old log, table {}, {}", lf.tableName, sdf.format(lf.date));
					lf.remove();
				} while (isDiskLack());

				for (DiskLackCallback callback : diskLackCallbacks)
					callback.callback();
			}
		}
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

	private class LogFile {
		private String tableName;
		private Date date;
		private File index;
		private File data;

		private LogFile(String tableName, Date date) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			File tableDir = storage.getTableDirectory(tableName);

			this.tableName = tableName;
			this.date = date;
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
}
