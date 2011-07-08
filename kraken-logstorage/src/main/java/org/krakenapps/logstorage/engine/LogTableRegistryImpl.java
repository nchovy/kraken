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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.logstorage.LogTableNotFoundException;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.TableMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "logstorage-table-registry")
@Provides
public class LogTableRegistryImpl implements LogTableRegistry {
	private final Logger logger = LoggerFactory.getLogger(LogTableRegistryImpl.class.getName());

	private static final File tableMappingFile = new File("data/kraken-logstorage/tables");

	// table managements
	private AtomicInteger nextTableId;
	private Properties tableProps;

	private ConcurrentMap<String, LogTable> tableSchemas;
	private ConcurrentMap<Integer, TableMetadata> metadataMap;

	public LogTableRegistryImpl() {
		tableSchemas = new ConcurrentHashMap<String, LogTable>();
		metadataMap = new ConcurrentHashMap<Integer, TableMetadata>();

		// load table id mappings
		loadTableMappings();
	}

	@Invalidate
	public void stop() {
		updateTableMappings();
	}

	private void loadTableMappings() {
		int maxId = 0;

		FileInputStream fis = null;
		try {
			if (!tableMappingFile.exists()) {
				// avoid exception when parent path of tableMappingFile is not
				// exists.
				tableMappingFile.getParentFile().mkdirs();
				new FileOutputStream(tableMappingFile).close();
			}

			fis = new FileInputStream(tableMappingFile);
			tableProps = new Properties();
			tableProps.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

		for (Object key : tableProps.keySet()) {
			String tableName = key.toString();
			if (!tableName.contains(".")) {
				int id = Integer.valueOf(tableProps.getProperty(tableName));
				if (id > maxId)
					maxId = id;

				logger.trace("log storage: loading table [{}]", tableName);
				tableSchemas.put(tableName, new LogTable(id, tableName));
				TableMetadata tm = new TableMetadata(tableProps, tableName);
				metadataMap.put(id, tm);
			}
		}

		nextTableId = new AtomicInteger(maxId);
	}

	@Override
	public boolean exists(String tableName) {
		return tableSchemas.containsKey(tableName);
	}

	@Override
	public Collection<String> getTableNames() {
		return new ArrayList<String>(tableSchemas.keySet());
	}

	@Override
	public int getTableId(String tableName) {
		LogTable table = tableSchemas.get(tableName);
		if (table == null)
			throw new LogTableNotFoundException(tableName);

		return table.getId();
	}

	@Override
	public String getTableName(int tableId) {
		if (metadataMap.containsKey(tableId))
			return metadataMap.get(tableId).getTableName();
		else
			return null;
	}

	@Override
	public void createTable(String tableName, Map<String, String> tableMetadata) {
		if (tableSchemas.containsKey(tableName))
			throw new IllegalStateException("table already exists: " + tableName);

		int newId = nextTableId.incrementAndGet();

		synchronized (tableProps) {
			tableProps.put(tableName, Integer.toString(newId));
			TableMetadata tm = new TableMetadata(tableProps, tableName);
			if (tableMetadata != null)
				tm.putAll(tableMetadata);
			metadataMap.put(newId, tm);

			updateTableMappings();
		}

		tableSchemas.put(tableName, new LogTable(newId, tableName));
	}

	private void updateTableMappings() {
		FileOutputStream fos = null;

		try {
			synchronized (tableProps) {
				fos = new FileOutputStream(tableMappingFile);
				tableProps.store(fos, null);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@Override
	public void dropTable(String tableName) {
		LogTable old = tableSchemas.remove(tableName);
		if (old == null)
			throw new IllegalStateException("table not found: " + tableName);

		TableMetadata oldMetadata = metadataMap.remove(old.getId());
		oldMetadata.clear();

		synchronized (tableProps) {
			tableProps.remove(tableName);
			updateTableMappings();
		}
	}

	@Override
	public TableMetadata getTableMetadata(int tableId) {
		return metadataMap.get(tableId);
	}

}
