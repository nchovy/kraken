/*
 * Copyright 2012 Future Systems
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
package org.krakenapps.eventstorage.script;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.krakenapps.api.DateFormat;
import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.confdb.ConfigService;
import org.krakenapps.eventstorage.Event;
import org.krakenapps.eventstorage.EventRecord;
import org.krakenapps.eventstorage.EventStorage;
import org.krakenapps.eventstorage.EventTableRegistry;
import org.krakenapps.eventstorage.engine.GlobalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventStorageScript implements Script {
	private Logger logger = LoggerFactory.getLogger(EventStorageScript.class);

	private ScriptContext context;
	private ConfigService confsvc;
	private EventTableRegistry tableRegistry;
	private EventStorage storage;

	public EventStorageScript(ConfigService confsvc, EventTableRegistry tableRegistry, EventStorage storage) {
		this.confsvc = confsvc;
		this.tableRegistry = tableRegistry;
		this.storage = storage;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void tables(String[] args) {
		context.println("Tables");
		context.println("---------------");
		for (String name : tableRegistry.getTableNames())
			context.println(name);
	}

	@ScriptUsage(description = "create new table", arguments = { @ScriptArgument(name = "name", type = "string", description = "event table name") })
	public void createTable(String[] args) {
		try {
			storage.createTable(args[0]);
			context.println("table created");
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "rename table", arguments = {
			@ScriptArgument(name = "current table name", type = "string", description = "current event table name"),
			@ScriptArgument(name = "new table name", type = "string", description = "new event table name") })
	public void renameTable(String[] args) {
		try {
			tableRegistry.renameTable(args[0], args[1]);
			context.println("table renamed");
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "drop event table", arguments = { @ScriptArgument(name = "name", type = "string", description = "event table name") })
	public void dropTable(String[] args) {
		try {
			storage.dropTable(args[0]);
			context.println("table dropped");
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "get events", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "id", type = "int", description = "event id") })
	public void event(String[] args) {
		try {
			String tableName = args[0];
			int id = Integer.valueOf(args[1]);

			Event event = storage.getEvent(tableName, id);
			context.println((event == null) ? "not found" : event);
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "get events", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "offset", type = "int", description = "offset"),
			@ScriptArgument(name = "limit", type = "int", description = "limit") })
	public void events(String[] args) {
		try {
			String tableName = args[0];
			int offset = Integer.valueOf(args[1]);
			int limit = Integer.valueOf(args[2]);

			Collection<Event> events = storage.getEvents(tableName, offset, limit);
			for (Event event : events)
				context.println(event);
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "get events", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "id", type = "int", description = "event id"),
			@ScriptArgument(name = "count", type = "int", description = "new event count"),
			@ScriptArgument(name = "data", type = "string", description = "event data", optional = true) })
	public void write(String[] args) {
		try {
			String tableName = args[0];
			long id = Long.valueOf(args[1]);
			Date date = new Date();
			int count = Integer.valueOf(args[2]);
			byte[] data = (args.length > 3) ? args[3].getBytes() : null;
			EventRecord record = new EventRecord(id, date, count, data);

			storage.write(tableName, record);
			context.println("write");
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "get events", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "id", type = "int", description = "event id") })
	public void remove(String[] args) {
		try {
			String tableName = args[0];
			long id = Long.valueOf(args[1]);

			storage.remove(tableName, id);
			context.println("removed");
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	@ScriptUsage(description = "get events", arguments = {
			@ScriptArgument(name = "table name", type = "string", description = "table name"),
			@ScriptArgument(name = "date", type = "string", description = "event date") })
	public void removes(String[] args) {
		try {
			String tableName = args[0];
			Date day = DateFormat.parse("yyyyMMdd", args[1]);
			storage.remove(tableName, day);
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	public void flush(String[] args) {
		try {
			storage.flush();
		} catch (Throwable e) {
			context.println(e);
			logger.debug("kraken eventstorage: " + e, e);
		}
	}

	public void parameters(String[] args) {
		for (GlobalConfig.Key key : GlobalConfig.Key.values()) {
			if (key == GlobalConfig.Key.NextEventId)
				continue;
			Object value = GlobalConfig.get(confsvc, key);
			context.println(key.getName() + " = " + value);
		}
	}

	@ScriptUsage(description = "start benchmark", arguments = { @ScriptArgument(name = "count", type = "int", description = "event count", optional = true) })
	public void benchmark(String[] args) {
		long count = (args.length > 0) ? Long.parseLong(args[0]) : 1000000;
		String tableName = "benchmark";
		storage.createTable(tableName);
		try {
			long time = System.currentTimeMillis() - count * 200L;
			{ // sequential id write (new event)
				byte[] data = new byte[300];
				Arrays.fill(data, (byte) 0xCC);

				long begin = System.currentTimeMillis();
				for (long id = 1; id <= count; id++) {
					Date date = new Date(time);
					time += 100L;
					storage.write(tableName, new EventRecord(id, date, 1, data));
				}
				long end = System.currentTimeMillis();
				context.printf("new event write (incl. data): %d ms (%d events/s)\n", end - begin, count * 1000L / (end - begin));
			}

			{ // random id write (exist event)
				Random r = new Random();
				long begin = System.currentTimeMillis();
				for (long i = 1; i <= count; i++) {
					Date date = new Date(time);
					time += 100L;
					storage.write(tableName, new EventRecord((long) r.nextInt((int) (count & 0x7FFFFFFF)) + 1, date, 2));
				}
				long end = System.currentTimeMillis();
				context.printf("random event write (excl. data): %d ms (%d events/s)\n", end - begin, count * 1000L
						/ (end - begin));
			}

			{
				long begin = System.currentTimeMillis();
				Collection<Event> events = storage.getEvents(tableName, (int) (count) - 4, 5);
				boolean valid = (events.size() == 4);
				long end = System.currentTimeMillis();
				context.printf("event read: %d ms (%d events/s) %s\n", end - begin, count * 1000L / (end - begin), valid ? ""
						: ("(error " + events.size() + ")"));
			}
		} finally {
			storage.dropTable(tableName);
		}
	}
}
