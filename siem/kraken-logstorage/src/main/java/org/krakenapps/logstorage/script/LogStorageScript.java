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
package org.krakenapps.logstorage.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.confdb.ConfigDatabase;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.logstorage.BatchIndexingStatus;
import org.krakenapps.logstorage.BatchIndexingTask;
import org.krakenapps.logstorage.IndexConfigSpec;
import org.krakenapps.logstorage.IndexTokenizerFactory;
import org.krakenapps.logstorage.IndexTokenizerRegistry;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogIndexCursor;
import org.krakenapps.logstorage.LogIndexItem;
import org.krakenapps.logstorage.LogIndexQuery;
import org.krakenapps.logstorage.LogIndexSchema;
import org.krakenapps.logstorage.LogIndexer;
import org.krakenapps.logstorage.LogIndexerStatus;
import org.krakenapps.logstorage.LogKey;
import org.krakenapps.logstorage.LogRetentionPolicy;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogStorageMonitor;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.LogWriterStatus;
import org.krakenapps.logstorage.engine.ConfigUtil;
import org.krakenapps.logstorage.engine.Constants;
import org.krakenapps.logstorage.engine.LogTableSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStorageScript implements Script {
	private final Logger logger = LoggerFactory.getLogger(LogStorageScript.class);
	private ScriptContext context;
	private LogTableRegistry tableRegistry;
	private LogStorage storage;
	private LogIndexer indexer;
	private LogStorageMonitor monitor;
	private IndexTokenizerRegistry tokenizerRegistry;
	private ConfigService conf;

	public LogStorageScript(LogTableRegistry tableRegistry, LogStorage archive, LogIndexer indexer, LogStorageMonitor monitor,
			IndexTokenizerRegistry tokenizerRegistry, ConfigService conf) {
		this.tableRegistry = tableRegistry;
		this.storage = archive;
		this.indexer = indexer;
		this.monitor = monitor;
		this.tokenizerRegistry = tokenizerRegistry;
		this.conf = conf;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void forceRetentionCheck(String[] args) {
		monitor.forceRetentionCheck();
		context.println("triggered");
	}

	@ScriptUsage(description = "set retention policy", arguments = { @ScriptArgument(name = "table name", type = "string", description = "table name") })
	public void retention(String[] args) {
		String tableName = args[0];
		LogRetentionPolicy p = storage.getRetentionPolicy(tableName);
		context.println(p.getRetentionDays() + "days");
	}

	@ScriptUsage(description = "set retention policy", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "retention days", type = "int", description = "retention days (0 for infinite)") })
	public void setRetention(String[] args) {
		LogRetentionPolicy p = new LogRetentionPolicy();
		p.setTableName(args[0]);
		p.setRetentionDays(Integer.valueOf(args[1]));

		storage.setRetentionPolicy(p);
		context.println("set");
	}

	@ScriptUsage(description = "purge index files in specified date range", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "index name", type = "string", description = "index name"),
			@ScriptArgument(name = "from day", type = "string", description = "yyyyMMdd format"),
			@ScriptArgument(name = "to day", type = "string", description = "yyyyMMdd format") })
	public void purgeIndexRange(String[] args) {
		String tableName = args[0];
		String indexName = args[1];

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		try {
			Date fromDay = dateFormat.parse(args[2]);
			Date toDay = dateFormat.parse(args[3]);
			indexer.purge(tableName, indexName, fromDay, toDay);
			context.println("purge completed");
		} catch (Throwable t) {
			context.println("cannot purge index range, " + t.getMessage());
			logger.error("kraken logstorage: cannot purge index range, table=" + tableName + ", index=" + indexName, t);
		}
	}

	public void batchIndexTasks(String[] args) {
		context.println("Batch Indexing Tasks");
		context.println("------------------------");
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for (BatchIndexingTask task : indexer.getBatchIndexingTasks()) {
			long elapsed = (System.currentTimeMillis() - task.getSince().getTime()) / 1000;
			String since = dateFormat.format(task.getSince());

			context.println(String.format("table [%s] index [%s] since %s (elapsed %dsec)", task.getTableName(),
					task.getIndexName(), since, elapsed));

			ArrayList<BatchIndexingStatus> builds = new ArrayList<BatchIndexingStatus>(task.getBuilds().values());
			Collections.sort(builds);
			for (BatchIndexingStatus s : builds)
				context.println(String.format("\tday=%s, logs=%s, tokens=%s, done=%s", dayFormat.format(s.getDay()),
						s.getLogCount(), s.getTokenCount(), s.isDone()));
		}
	}

	@ScriptUsage(description = "print all indexing configurations", arguments = { @ScriptArgument(name = "table name", type = "string", description = "table name") })
	public void indexes(String[] args) {
		String tableName = args[0];
		if (!tableRegistry.exists(tableName)) {
			context.println("table does not exists");
			return;
		}

		Set<String> indexNames = indexer.getIndexNames(tableName);
		if (indexNames.isEmpty()) {
			context.println("no index found");
			return;
		}

		context.println("Index for table [" + tableName + "]");
		context.println("-------------------------------");
		for (String indexName : indexNames) {
			LogIndexSchema c = indexer.getIndexConfig(tableName, indexName);
			context.println(c);
		}
	}

	@ScriptUsage(description = "print specific index configuration", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "index name", type = "string", description = "index name") })
	public void index(String[] args) {
		String tableName = args[0];
		String indexName = args[1];

		if (!tableRegistry.exists(tableName)) {
			context.println("table does not exists");
			return;
		}

		LogIndexSchema schema = indexer.getIndexConfig(tableName, indexName);
		if (schema == null) {
			context.println("index [" + indexName + "] not found");
			return;
		}

		context.println("Index Detail");
		context.println("------------------");
		List<Date> days = indexer.getIndexedDays(tableName, indexName);
		Date min = null;
		Date max = null;
		for (Date day : days) {
			if (min == null)
				min = day;
			else if (day.before(min))
				min = day;

			if (max == null)
				max = day;
			else if (day.after(max))
				max = day;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateRange = "N/A";
		if (min != null && max != null)
			dateRange = dateFormat.format(min) + " ~ " + dateFormat.format(max);

		context.println("Table Name: " + schema.getTableName());
		context.println("Index Name (ID): " + schema.getIndexName() + " (" + schema.getId() + ")");
		context.println("Indexed Days: " + dateRange);
		context.println("Tokenizer: " + schema.getTokenizerName());
		context.println("");

		context.println("Tokenizer Config");
		context.println("------------------");

		for (Entry<String, String> pair : schema.getTokenizerConfigs().entrySet()) {
			context.println(pair.getKey() + ": " + pair.getValue());
		}

		long total = 0;
		File dir = indexer.getIndexDirectory(tableName, indexName);
		if (dir.exists()) {
			for (File f : dir.listFiles())
				total += f.length();
		}

		context.println();
		context.println("Storage Consumption");
		context.println("---------------------");
		NumberFormat nf = NumberFormat.getNumberInstance();
		context.println(nf.format(total) + " bytes");
	}

	public void indexTokenizers(String[] args) {
		context.println("Index Tokenizers");
		for (IndexTokenizerFactory f : tokenizerRegistry.getFactories())
			context.println("[" + f.getName() + "] " + f);
	}

	@ScriptUsage(description = "search index", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "index name", type = "string", description = "index name"),
			@ScriptArgument(name = "term", type = "string", description = "search term") })
	public void searchIndex(String[] args) {
		String tableName = args[0];
		String indexName = args[1];
		String term = args[2];
		LogIndexCursor c = null;
		try {
			long begin = System.currentTimeMillis();
			LogIndexQuery q = new LogIndexQuery();
			q.setTableName(tableName);
			q.setIndexName(indexName);
			q.setTerm(term);

			long count = 0;
			int tableId = tableRegistry.getTableId(tableName);
			c = indexer.search(q);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			while (c.hasNext()) {
				LogIndexItem item = c.next();
				Log log = storage.getLog(new LogKey(tableId, item.getDay(), (int) item.getLogId()));
				String dateString = dateFormat.format(log.getDate());
				context.println(log.getTableName() + " (" + dateString + ") #" + log.getId() + " " + log.getData());
				count++;
			}

			long elapsed = System.currentTimeMillis() - begin;
			context.println("total " + count + " logs, elapsed " + elapsed + "ms");
		} catch (IOException e) {
			context.println("search failed, " + e.getMessage());
		} finally {
			if (c != null)
				c.close();
		}
	}

	@ScriptUsage(description = "create index", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "index name", type = "string", description = "index name") })
	public void createIndex(String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		String tableName = args[0];
		String indexName = args[1];
		try {
			String tokenizerName = readLine("tokenizer? ");

			IndexTokenizerFactory factory = tokenizerRegistry.getFactory(tokenizerName);
			if (factory == null) {
				context.println("tokenizer [" + tokenizerName + "] not found");
				return;
			}

			context.println("index tokenizer configurations..");
			Map<String, String> tokenizerConfigs = new HashMap<String, String>();
			for (IndexConfigSpec spec : factory.getConfigSpecs()) {
				while (true) {
					String optional = spec.isRequired() ? "" : " (optional, enter to skip)";
					String line = readLine(spec.getName() + optional + "? ");
					if (line != null) {
						tokenizerConfigs.put(spec.getKey(), line);
						break;
					} else if (!spec.isRequired())
						break;
				}
			}

			String minDayStr = readLine("min day (yyyymmdd or enter to skip)? ");
			Date minDay = null;
			if (minDayStr != null)
				minDay = dateFormat.parse(minDayStr);

			String buildPast = readLine("build index for past log (y/n)? ");
			boolean buildPastIndex = buildPast != null && buildPast.equalsIgnoreCase("y");

			LogIndexSchema config = new LogIndexSchema();
			config.setTableName(tableName);
			config.setIndexName(indexName);
			config.setBuildPastIndex(buildPastIndex);
			config.setMinIndexDay(minDay);
			config.setTokenizerName(tokenizerName);
			config.setTokenizerConfigs(tokenizerConfigs);

			indexer.createIndex(config);
			context.println("created index " + indexName + " for table " + tableName);
		} catch (Throwable t) {
			context.println(t.getMessage());
			logger.error("kraken logstorage: cannot create index for table " + tableName, t);
		}
	}

	private String readLine(String question) throws InterruptedException {
		context.print(question);
		String line = context.readLine();
		if (line.trim().isEmpty())
			return null;

		return line;
	}

	@ScriptUsage(description = "drop index", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "index name", type = "string", description = "index name") })
	public void dropIndex(String[] args) {
		String tableName = args[0];
		String indexName = args[1];

		indexer.dropIndex(tableName, indexName);
		context.println("dropped");
	}

	@ScriptUsage(description = "migrate old properties to new confdb metadata")
	public void migrate(String[] args) {
		context.println("migrate table metadata from properties to confdb");

		FileInputStream is = null;
		try {
			ConfigDatabase db = conf.ensureDatabase("kraken-logstorage");
			is = new FileInputStream(new File(System.getProperty("kraken.data.dir"), "kraken-logstorage/tables"));

			Properties p = new Properties();
			p.load(is);

			for (Object key : p.keySet()) {
				String tableName = key.toString();
				if (!tableName.contains(".")) {
					int id = Integer.valueOf(p.getProperty(tableName));
					LogTableSchema t = new LogTableSchema(id, tableName);
					db.add(t, "kraken-logstorage", tableName + " metadata is migrated from old version");
				}
			}
		} catch (IOException e) {
			context.println(e.getMessage());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@ScriptUsage(description = "print table metadata", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "table metadata key", type = "string", description = "key", optional = true),
			@ScriptArgument(name = "table metadata value", type = "string", description = "value", optional = true) })
	public void table(String[] args) {
		String tableName = args[0];

		if (!tableRegistry.exists(tableName)) {
			context.println("table not found");
			return;
		}

		if (args.length == 1) {
			context.println("Table " + args[0]);
			context.println();
			context.println("Table Metadata");
			context.println("----------------");
			for (String key : tableRegistry.getTableMetadataKeys(tableName)) {
				String value = tableRegistry.getTableMetadata(tableName, key);
				context.println(key + "=" + value);
			}

			long total = 0;
			File dir = storage.getTableDirectory(tableName);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					total += f.length();
			}

			context.println();
			context.println("Storage Consumption");
			context.println("---------------------");
			NumberFormat nf = NumberFormat.getNumberInstance();
			context.println(nf.format(total) + " bytes");
		} else if (args.length == 2) {
			String value = tableRegistry.getTableMetadata(tableName, args[1]);
			context.println("unset " + value);
			tableRegistry.unsetTableMetadata(tableName, args[1]);
		} else if (args.length == 3) {
			tableRegistry.setTableMetadata(tableName, args[1], args[2]);
			context.printf("set %s to %s\n", args[1], args[2]);
		}
	}

	public void tables(String[] args) {
		context.println("Tables");
		context.println("--------");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		ArrayList<TableInfo> tables = new ArrayList<TableInfo>();
		for (String tableName : tableRegistry.getTableNames()) {
			int tableId = tableRegistry.getTableId(tableName);
			Iterator<Date> it = storage.getLogDates(tableName).iterator();
			Date lastDay = null;
			if (it.hasNext())
				lastDay = it.next();

			String lastRecord = lastDay != null ? dateFormat.format(lastDay) : "none";
			tables.add(new TableInfo(tableId, "[" + tableId + "] " + tableName + ": " + lastRecord));
		}

		// sort by id and print all
		Collections.sort(tables, new Comparator<TableInfo>() {
			@Override
			public int compare(TableInfo o1, TableInfo o2) {
				return o1.id - o2.id;
			}
		});

		for (TableInfo t : tables)
			context.println(t.info);
	}

	private class TableInfo {
		public int id;
		public String info;

		public TableInfo(int id, String info) {
			this.id = id;
			this.info = info;
		}
	}

	public void open(String[] args) {
		storage.start();
	}

	public void close(String[] args) {
		storage.stop();
	}

	public void reload(String[] args) {
		storage.reload();
	}

	@ScriptUsage(description = "create new table", arguments = { @ScriptArgument(name = "name", type = "string", description = "log table name") })
	public void createTable(String[] args) {
		storage.createTable(args[0]);
		context.println("table created");
	}

	@ScriptUsage(description = "rename table", arguments = {
			@ScriptArgument(name = "current table name", type = "string", description = "current log table name"),
			@ScriptArgument(name = "new table name", type = "string", description = "new table name") })
	public void renameTable(String[] args) {
		tableRegistry.renameTable(args[0], args[1]);
		context.println("ok");
	}

	@ScriptUsage(description = "drop log table", arguments = { @ScriptArgument(name = "name", type = "string", description = "log table name") })
	public void dropTable(String[] args) {
		try {
			storage.dropTable(args[0]);
			context.println("table dropped");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	@ScriptUsage(description = "get logs", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "from", type = "string", description = "yyyyMMddHH format"),
			@ScriptArgument(name = "to", type = "string", description = "yyyyMMddHH format"),
			@ScriptArgument(name = "offset", type = "int", description = "offset"),
			@ScriptArgument(name = "limit", type = "int", description = "log limit") })
	public void logs(String[] args) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
		String tableName = args[0];
		Date from = dateFormat.parse(args[1]);
		Date to = dateFormat.parse(args[2]);
		int offset = Integer.valueOf(args[3]);
		int limit = Integer.valueOf(args[4]);

		try {
			storage.search(tableName, from, to, offset, limit, new LogSearchCallback() {
				@Override
				public void onLog(Log log) {
					context.println(log.toString());
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
			context.println("interrupted");
		}
	}

	@ScriptUsage(description = "search table", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "log table name"),
			@ScriptArgument(name = "from", type = "string", description = "from"),
			@ScriptArgument(name = "to", type = "string", description = "to"),
			@ScriptArgument(name = "limit", type = "int", description = "count limit") })
	public void searchTable(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String tableName = args[0];
			Date from = dateFormat.parse(args[1]);
			Date to = dateFormat.parse(args[2]);
			int limit = Integer.parseInt(args[3]);

			long begin = new Date().getTime();

			LogSearchCallback callback = new PrintCallback();
			storage.search(tableName, from, to, limit, callback);

			long end = new Date().getTime();

			context.println("elapsed: " + (end - begin) + "ms");
		} catch (Exception e) {
			context.println(e.getMessage());
		}
	}

	private class PrintCallback implements LogSearchCallback {
		@Override
		public void interrupt() {
		}

		@Override
		public boolean isInterrupted() {
			return false;
		}

		@Override
		public void onLog(Log log) {
			if (log == null)
				return;

			Map<String, Object> m = log.getData();
			context.print(log.getId() + ": ");
			for (String key : m.keySet()) {
				context.print(key + "=" + m.get(key) + ", ");
			}
			context.println("");
		}
	}

	@ScriptUsage(description = "print all parameters")
	public void parameters(String[] args) {
		for (Constants c : Constants.values()) {
			context.println(c.getName() + ": " + ConfigUtil.get(conf, c));
		}
	}

	@ScriptUsage(description = "set parameters", arguments = {
			@ScriptArgument(name = "key", type = "string", description = "parameter key"),
			@ScriptArgument(name = "value", type = "string", description = "parameter value") })
	public void setParameter(String[] args) {
		Constants configKey = Constants.parse(args[0]);
		if (configKey == null) {
			context.println("invalid key name");
			return;
		}

		String value = null;
		if (configKey.getType().equals("string")) {
			value = args[1];
		} else if (configKey.getType().equals("int")) {
			int interval = 0;
			try {
				interval = Integer.parseInt(args[1]);
				value = Integer.toString(interval);
			} catch (NumberFormatException e) {
				context.println("invalid parameter format");
				return;
			}
		}

		ConfigUtil.set(conf, configKey, value);
		context.println("set");
	}

	@ScriptUsage(description = "import text log file", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "file path", type = "string", description = "text log file path"),
			@ScriptArgument(name = "offset", type = "int", description = "skip offset", optional = true),
			@ScriptArgument(name = "limit", type = "int", description = "load limit count", optional = true) })
	public void importTextFile(String[] args) throws IOException {
		String tableName = args[0];
		File file = new File(args[1]);
		int offset = 0;
		if (args.length > 2)
			offset = Integer.valueOf(args[2]);

		int limit = Integer.MAX_VALUE;
		if (args.length > 3)
			limit = Integer.valueOf(args[3]);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			importFromStream(tableName, fis, offset, limit);
		} catch (Exception e) {
			context.println("import failed, " + e.getMessage());
			logger.error("kraken logstorage: cannot import text file " + file.getAbsolutePath(), e);
		} finally {
			if (fis != null)
				fis.close();
		}
	}

	@ScriptUsage(description = "import zipped text log file", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "zip file path", type = "string", description = "zip file path"),
			@ScriptArgument(name = "entry path", type = "string", description = "zip entry of text log file path"),
			@ScriptArgument(name = "offset", type = "int", description = "skip offset", optional = true),
			@ScriptArgument(name = "limit", type = "int", description = "load limit count", optional = true) })
	public void importZipFile(String[] args) throws ZipException, IOException {
		String tableName = args[0];
		String filePath = args[1];
		String entryPath = args[2];
		File file = new File(args[1]);
		int offset = 0;
		if (args.length > 3)
			offset = Integer.valueOf(args[3]);

		int limit = Integer.MAX_VALUE;
		if (args.length > 4)
			limit = Integer.valueOf(args[4]);

		ZipFile zipFile = new ZipFile(file);
		ZipEntry entry = zipFile.getEntry(entryPath);
		if (entry == null) {
			context.println("entry [" + entryPath + "] not found in zip file [" + filePath + "]");
			return;
		}

		InputStream is = null;
		try {
			is = zipFile.getInputStream(entry);
			importFromStream(tableName, is, offset, limit);
		} catch (Exception e) {
			context.println("import failed, " + e.getMessage());
			logger.error("kraken logstorage: cannot import zipped text file " + file.getAbsolutePath(), e);
		} finally {
			if (is != null)
				is.close();
		}
	}

	private void importFromStream(String tableName, InputStream fis, int offset, int limit) throws IOException {
		Date begin = new Date();
		int count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(fis), 16384 * 1024); // 16MB
		String line = null;

		int i = 0;
		while (true) {
			line = br.readLine();
			if (line == null)
				break;

			if (count >= limit)
				break;

			if (i++ < offset)
				continue;

			Map<String, Object> m = new HashMap<String, Object>();
			m.put("line", line);

			Log log = new Log(tableName, new Date(), m);
			try {
				storage.write(log);
			} catch (IllegalArgumentException e) {
				context.println("skip " + line + ", " + e.getMessage());
			}

			count++;

			if (count % 10000 == 0)
				context.println("loaded " + count);
		}

		long milliseconds = new Date().getTime() - begin.getTime();
		long speed = count / (milliseconds / 1000);
		context.println("loaded " + count + " logs in " + milliseconds + " ms, " + speed + " logs/sec");
	}

	@ScriptUsage(description = "benchmark table fullscan", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "from", type = "string", description = "date from (yyyyMMdd format)"),
			@ScriptArgument(name = "to", type = "string", description = "date to (yyyyMMdd format)") })
	public void fullscan(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String tableName = args[0];
			Date from = dateFormat.parse(args[1]);
			Date to = dateFormat.parse(args[2]);

			LogCounter counter = new LogCounter();
			Date timestamp = new Date();
			storage.search(tableName, from, to, Integer.MAX_VALUE, counter);
			long elapsed = new Date().getTime() - timestamp.getTime();

			context.println("total count: " + counter.getCount() + ", elapsed: " + elapsed + "ms");
		} catch (ParseException e) {
			context.println("invalid date format");
		} catch (InterruptedException e) {
			context.println("interrupted");
		}
	}

	public void flush(String[] args) {
		storage.flush();
	}

	private static class LogCounter implements LogSearchCallback {
		private int count = 0;

		@Override
		public void onLog(Log log) {
			count++;
		}

		public int getCount() {
			return count;
		}

		@Override
		public boolean isInterrupted() {
			return false;
		}

		@Override
		public void interrupt() {
		}
	}

	@ScriptUsage(description = "print all online writer statuses")
	public void writers(String[] args) {
		context.println("Online Writers");
		context.println("-----------------");
		for (LogWriterStatus s : storage.getWriterStatuses()) {
			context.println(s);
		}
	}

	@ScriptUsage(description = "print all online indexer statuses")
	public void indexers(String[] args) {
		context.println("Online Indexers");
		context.println("------------------");

		for (LogIndexerStatus s : indexer.getIndexerStatuses()) {
			context.println(s);
		}
	}

	@ScriptUsage(description = "", arguments = {
			@ScriptArgument(name = "count", type = "integer", description = "log count", optional = true),
			@ScriptArgument(name = "repeat", type = "integer", description = "repeat count", optional = true) })
	public void benchmark(String[] args) {
		String tableName = "benchmark";
		int count = 1000000;
		int repeat = 1;
		if (args.length >= 1)
			count = Integer.parseInt(args[0]);
		if (args.length >= 2)
			repeat = Integer.parseInt(args[1]);

		Map<String, Object> text = new HashMap<String, Object>();
		text.put("_data", "2011-08-22 17:30:23 Google 111.222.33.44 GET /search q=cache:xgLxoOQBOoIJ:"
				+ "krakenapps.org/+krakenapps&cd=1&hl=en&ct=clnk&source=www.google.com 80 - 123.234.34.45 "
				+ "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 "
				+ "Safari/535.1 404 0 3");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("c-ip", "111.222.33.44");
		map.put("cs(User-Agent)",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1");
		map.put("cs-method", "GET");
		map.put("cs-uri-query", "q=cache:xgLxoOQBOoIJ:krakenapps.org/+krakenapps&cd=1&hl=en&ct=clnk&source=www.google.com");
		map.put("cs-uri-stem", "/search");
		map.put("cs-username", "-");
		map.put("date", "2011-08-22");
		map.put("s-ip", "123.234.34.45");
		map.put("s-port", "80");
		map.put("s-sitename", "Google");
		map.put("sc-status", "200");
		map.put("sc-substatus", "0");
		map.put("sc-win32-status", "0");
		map.put("time", "17:30:23");

		for (int i = 1; i <= repeat; i++) {
			context.println("=== Test #" + i + " ===");
			benchmark("text", tableName, count, text);
			benchmark("map", tableName, count, map);
			context.println("");
		}
	}

	private void benchmark(String name, String tableName, int count, Map<String, Object> data) {
		storage.createTable(tableName);

		Log log = new Log(tableName, new Date(), data);
		long begin = System.currentTimeMillis();
		for (long id = 1; id <= count; id++) {
			log.setId(id);
			storage.write(log);
		}
		long end = System.currentTimeMillis();
		long time = end - begin;

		context.println(String.format("%s(write): %d log/%d ms (%d log/s)", name, count, time, count * 1000L / time));

		begin = System.currentTimeMillis();
		try {
			storage.search(tableName, new Date(0), new Date(), count, new BenchmarkCallback());
		} catch (InterruptedException e) {
		}
		end = System.currentTimeMillis();
		time = end - begin;
		context.println(String.format("%s(read): %d log/%d ms (%d log/s)", name, count, time, count * 1000L / time));

		storage.dropTable(tableName);
	}

	private class BenchmarkCallback implements LogSearchCallback {
		private boolean interrupt = false;

		@Override
		public void onLog(Log log) {
			if (log.getData().containsKey("_data")) {
				String line = (String) log.getData().get("_data");

				// simulate same condition (compares to map write condition)
				StringTokenizer t = new StringTokenizer(line, " ");
				while (t.hasMoreTokens()) {
					t.nextToken();
				}
			}
		}

		@Override
		public void interrupt() {
			interrupt = true;
		}

		@Override
		public boolean isInterrupted() {
			return interrupt;
		}
	}
}
