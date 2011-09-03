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
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.logstorage.Log;
import org.krakenapps.logstorage.LogQuery;
import org.krakenapps.logstorage.LogQueryCommand;
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.TableMetadata;
import org.krakenapps.logstorage.engine.Constants;
import org.krakenapps.logstorage.engine.ConfigUtil;
import org.krakenapps.logstorage.query.FileBufferList;
import org.osgi.service.prefs.PreferencesService;

public class LogStorageScript implements Script {
	private ScriptContext context;
	private LogTableRegistry tableRegistry;
	private LogStorage storage;
	private LogQueryService logQueryService;
	private PreferencesService prefsvc;

	public LogStorageScript(LogTableRegistry tableRegistry, LogStorage archive, LogQueryService logQueryService,
			PreferencesService prefsvc) {
		this.tableRegistry = tableRegistry;
		this.storage = archive;
		this.logQueryService = logQueryService;
		this.prefsvc = prefsvc;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void tables(String[] args) {
		context.println("Tables");
		context.println("---------------");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (String tableName : tableRegistry.getTableNames()) {
			Iterator<Date> it = storage.getLogDates(tableName).iterator();
			Date lastDay = null;
			if (it.hasNext())
				lastDay = it.next();

			String lastRecord = lastDay != null ? dateFormat.format(lastDay) : "none";
			context.println(tableName + ": " + lastRecord);
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

	@ScriptUsage(description = "create new table", arguments = { @ScriptArgument(name = "name", type = "string",
			description = "log table name") })
	public void createTable(String[] args) {
		storage.createTable(args[0]);
		context.println("table created");
	}

	@ScriptUsage(description = "drop log table", arguments = { @ScriptArgument(name = "name", type = "string",
			description = "log table name") })
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
			context.println(c.getName() + ": " + ConfigUtil.get(prefsvc, c));
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

		ConfigUtil.set(prefsvc, configKey, value);
		context.println("set");
	}

	@ScriptUsage(description = "", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "path", type = "string", description = "iis file path") })
	public void loadTest(String[] args) {
		Date begin = new Date();

		String tableName = args[0];
		File file = new File(args[1]);
		int loadLimit = 400000;

		if (args.length > 2) {
			loadLimit = Integer.parseInt(args[2]);
		}

		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis), 16384 * 1024); // 16MB
			String line = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			int count = 0;
			while (count < loadLimit) {
				line = br.readLine();
				if (line == null)
					break;

				if (line.startsWith("#")) {
					if (line.startsWith("#Fields: ")) {
						TableMetadata tm = tableRegistry.getTableMetadata(tableRegistry.getTableId(tableName));
						tm.put("logformat", line.replace("#Fields: ", ""));
					}
					continue;
				}

				String[] tokens = line.split(" ");

				Date date = dateFormat.parse(tokens[0] + " " + tokens[1]);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("log", line);

				Log log = new Log(tableName, date, m);
				try {
					storage.write(log);
				} catch (IllegalArgumentException e) {
					context.println("skip " + date + " , " + e.getMessage());
				}

				count++;

				if (count % 1000 == 0)
					context.println("loaded " + count);
			}

		} catch (Exception e) {
			e.printStackTrace();
			context.println(e.getMessage());
		}

		long milliseconds = new Date().getTime() - begin.getTime();
		context.println(Long.toString(milliseconds) + " ms");
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

	public void queries(String[] args) {
		Collection<LogQuery> queries = logQueryService.getQueries();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		for (LogQuery query : queries) {
			long sec = new Date().getTime() - query.getLastStarted().getTime();
			context.println(String.format("[%d] %s \t/ %s, %d seconds ago", query.getId(), query.getQueryString(),
					sdf.format(query.getLastStarted()), sec / 1000));
			for (LogQueryCommand cmd : query.getCommands()) {
				context.println(String.format("    [%s] %s \t/ %d write data(s) to next query", cmd.getStatus(),
						cmd.getQueryString(), cmd.getPushCount()));
			}
		}
	}

	public void query(String[] args) {
		long begin = System.currentTimeMillis();
		LogQuery lq = logQueryService.createQuery(args[0]);
		Thread t = new Thread(lq);
		t.start();

		do {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		} while (!lq.isEnd());

		List<Map<String, Object>> results = lq.getResult();
		for (Map<String, Object> m : results)
			printMap(m);
		((FileBufferList<Map<String, Object>>) results).close();

		logQueryService.removeQuery(lq.getId());
		context.println(String.format("%.1fs", (System.currentTimeMillis() - begin) / (double) 1000));
	}

	@SuppressWarnings("unchecked")
	private void printMap(Map<String, Object> m) {
		boolean start = true;
		context.print("{");
		List<String> keySet = new ArrayList<String>(m.keySet());
		Collections.sort(keySet);
		for (String key : keySet) {
			if (start)
				start = false;
			else
				context.print(", ");

			context.print(key + "=");
			Object value = m.get(key);
			if (value instanceof Map)
				printMap((Map<String, Object>) value);
			else if (value == null)
				context.print("null");
			else if (value.getClass().isArray())
				context.print(Arrays.toString((Object[]) value));
			else
				context.print(value.toString());
		}
		context.println("}");
	}

	public void removeQuery(String[] args) {
		logQueryService.removeQuery(Integer.parseInt(args[0]));
		context.println("removed");
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
		map.put("cs-uri-query",
				"q=cache:xgLxoOQBOoIJ:krakenapps.org/+krakenapps&cd=1&hl=en&ct=clnk&source=www.google.com");
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
				line.split(" ");
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
