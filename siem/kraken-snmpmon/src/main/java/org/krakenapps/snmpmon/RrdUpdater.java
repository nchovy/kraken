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
package org.krakenapps.snmpmon;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.log.api.Log;
import org.krakenapps.log.api.LogPipe;
import org.krakenapps.log.api.Logger;
import org.krakenapps.log.api.LoggerFactory;
import org.krakenapps.log.api.LoggerFactoryEventListener;
import org.krakenapps.log.api.LoggerFactoryRegistry;
import org.krakenapps.rrd.CompactRrd;
import org.krakenapps.rrd.ConsolidateFunc;
import org.krakenapps.rrd.DataSourceConfig;
import org.krakenapps.rrd.DataSourceType;
import org.krakenapps.rrd.Rrd;
import org.krakenapps.rrd.RrdConfig;
import org.krakenapps.rrd.io.FilePersistentLayer;

@Component(name = "snmpmon-rrd-updater")
public class RrdUpdater implements LogPipe, LoggerFactoryEventListener {
	private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RrdUpdater.class);

	@Requires
	private LoggerFactoryRegistry factoryRegistry;
	private LoggerFactory factory;

	private File rootDir;
	private boolean run = true;

	private ConcurrentMap<String, Rrd> rrds;
	private ConcurrentMap<String, Cache> cache;

	@Validate
	public void validate() {
		factory = factoryRegistry.getLoggerFactory("snmpmon");
		factory.addListener(this);

		rootDir = new File(System.getProperty("kraken.data.dir"), "kraken-snmpmon/rrd/");
		rootDir.mkdirs();
		rrds = new ConcurrentHashMap<String, Rrd>();
		cache = new ConcurrentHashMap<String, Cache>();
	}

	@Invalidate
	public void invalidate() {
		run = false;
		factory.removeListener(this);
		rrds.clear();
		cache.clear();
	}

	@Override
	public void loggerCreated(LoggerFactory factory, Logger logger, Properties config) {
		logger.addLogPipe(this);
	}

	@Override
	public void loggerDeleted(LoggerFactory factory, Logger logger) {
		logger.removeLogPipe(this);
		File file = new File(rootDir, logger.getFullName().replace("\\", "$") + ".rrd");
		if (file.exists())
			file.delete();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onLog(Logger logger, Log log) {
		if (!run || !logger.getFactoryFullName().equals(factory.getFullName()))
			return;

		Map<String, Object> data = new HashMap<String, Object>(log.getParams());
		data.remove("_total");
		data.remove("msg");
		if (!"network-usage".equals(data.remove("logtype")))
			return;

		String[] columns = { "rx_bytes_delta", "tx_bytes_delta", "rx_discards_delta", "tx_discards_delta", "rx_errors_delta",
				"tx_errors_delta", "rx_ucast_pkts_delta", "tx_ucast_pkts_delta" };
		try {
			Cache cache = this.cache.get(logger.getFullName());
			if (cache == null) {
				cache = new Cache();
				this.cache.put(logger.getFullName(), cache);
			}

			for (String key : data.keySet()) {
				try {
					Map<String, Object> value = (Map<String, Object>) data.get(key);

					for (String column : columns) {
						String cacheKey = key + ":" + column;
						double v = cache.delta.containsKey(cacheKey) ? cache.delta.get(cacheKey) : 0;
						try {
							v += ((Long) value.get(column)).doubleValue() / 60.0;
							cache.delta.put(cacheKey, v);
						} catch (ClassCastException e) {
						} catch (NullPointerException e) {
						}
					}
				} catch (ClassCastException e) {
				}
			}

			if (cache.beforeWrite.before(new Date(System.currentTimeMillis() - 60000))) {
				Rrd rrd = rrds.get(logger.getFullName());
				if (rrd == null) {
					File file = new File(rootDir, logger.getFullName().replace("\\", "$") + ".rrd");
					RrdConfig config = null;

					if (!file.exists()) {
						config = new RrdConfig(log.getDate(), 60);
						for (String key : data.keySet()) {
							for (String column : columns)
								config.addDataSource(key + ":" + column, DataSourceType.GAUGE, 120, Double.MIN_VALUE,
										Double.MAX_VALUE);
						}

						config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 1, 360); // 6h / 1min
						config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 5, 288); // Daily / 5min
						config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 30, 336); // Weekly / 30min
						config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 120, 360); // Monthly / 2h
						config.addArchive(ConsolidateFunc.AVERAGE, 0.5, 1440, 365); // Yearly / 1d
					}

					rrd = new CompactRrd(new FilePersistentLayer(file, 512 * 1024), config);
					rrds.put(logger.getFullName(), rrd);
				}

				Set<String> exist = new HashSet<String>();
				for (DataSourceConfig ds : rrd.getDataSources())
					exist.add(ds.getName().substring(0, ds.getName().indexOf(':')));
				for (String key : data.keySet()) {
					if (!exist.contains(key)) {
						for (String column : columns)
							rrd.addDataSource(key + ":" + column, DataSourceType.GAUGE, 120, Double.MIN_VALUE, Double.MAX_VALUE);
					}
				}
				for (String key : exist) {
					if (!data.keySet().contains(key)) {
						for (String column : columns)
							rrd.removeDataSource(key + ":" + column);
					}
				}

				rrd.update(log.getDate(), cache.delta);
				cache.beforeWrite = new Date();
				cache.delta.clear();
			}
		} catch (IOException e) {
			this.logger.error("kraken snmpmon: io exception", e);
		}
	}

	private class Cache {
		private Date beforeWrite = new Date();
		private Map<String, Double> delta = new HashMap<String, Double>();
	}
}
