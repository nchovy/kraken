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
import java.util.Collection;
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
import org.krakenapps.logstorage.LogQueryService;
import org.krakenapps.logstorage.LogSearchCallback;
import org.krakenapps.logstorage.LogStorage;
import org.krakenapps.logstorage.LogTableRegistry;
import org.krakenapps.logstorage.TableMetadata;
import org.krakenapps.logstorage.criterion.EqExpression;
import org.krakenapps.logstorage.engine.Constants;
import org.krakenapps.logstorage.engine.ConfigUtil;
import org.krakenapps.logstorage.query.LogQueryCommand;
import org.krakenapps.logstorage.query.LogQueryImpl;
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

	@ScriptUsage(description = "create new table", arguments = { @ScriptArgument(name = "name", type = "string", description = "log table name") })
	public void createTable(String[] args) {
		storage.createTable(args[0]);
		context.println("table created");
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
			storage.search(tableName, from, to, offset, limit, null, new LogSearchCallback() {
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
			@ScriptArgument(name = "limit", type = "int", description = "count limit"),
			@ScriptArgument(name = "field", type = "string", description = "field"),
			@ScriptArgument(name = "term", type = "string", description = "term") })
	public void searchTable(String[] args) {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String tableName = args[0];
			Date from = dateFormat.parse(args[1]);
			Date to = dateFormat.parse(args[2]);
			int limit = Integer.parseInt(args[3]);
			String field = args[4];
			String term = args[5];

			long begin = new Date().getTime();

			LogSearchCallback callback = new PrintCallback();
			storage.search(tableName, from, to, limit, new EqExpression(field, term), callback);

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

	@ScriptUsage(description = "set parameters")
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
			storage.search(tableName, from, to, Integer.MAX_VALUE, null, counter);
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

	public void createLogQuery(String[] args) {
		LogQuery lq = logQueryService.createQuery(args[0]);
		new Thread(lq).start();

		while (!lq.isEnd())
			;

		List<Map<String, Object>> results = lq.getResult();
		for (Map<String, Object> m : results)
			context.println(m.toString());

		logQueryService.removeQuery(lq.getId());
	}

	public void removeLogQuery(String[] args) {
		logQueryService.removeQuery(Integer.parseInt(args[0]));
		context.println("removed");
	}

	public void queries(String[] args) {
		Collection<LogQuery> queries = logQueryService.getQueries();

		for (LogQuery query : queries) {
			context.println(String.format("[%d] %s", query.getId(), query.getQueryString()));
			for (LogQueryCommand cmd : ((LogQueryImpl) query).getCommands()) {
				context.println(String.format("    [%s] %s", cmd.getStatus(), cmd.getQueryString()));
			}
		}
	}
}
