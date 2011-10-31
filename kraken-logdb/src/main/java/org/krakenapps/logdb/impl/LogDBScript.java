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
package org.krakenapps.logdb.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.arbiter.ArbiterQueryStatus;
import org.krakenapps.logdb.arbiter.ArbiterService;
import org.krakenapps.logdb.query.FileBufferList;

public class LogDBScript implements Script {
	private LogQueryService qs;
	private ArbiterService arbiter;
	private ScriptContext context;

	public LogDBScript(LogQueryService qs, ArbiterService arbiter) {
		this.qs = qs;
		this.arbiter = arbiter;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void queries(String[] args) {
		context.println("Log Queries");
		context.println("-------------");
		Collection<LogQuery> queries = qs.getQueries();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		for (LogQuery query : queries) {
			String when = " \t/ not started yet";
			if (query.getLastStarted() != null) {
				long sec = new Date().getTime() - query.getLastStarted().getTime();
				when = String.format(" \t/ %s, %d seconds ago", sdf.format(query.getLastStarted()), sec / 1000);
			}

			context.println(String.format("[%d] %s%s", query.getId(), query.getQueryString(), when));

			if (query.getCommands() != null) {
				for (LogQueryCommand cmd : query.getCommands()) {
					context.println(String.format("    [%s] %s \t/ passed %d data to next query", cmd.getStatus(),
							cmd.getQueryString(), cmd.getPushCount()));
				}
			} else
				context.println("    null");
		}
	}

	public void query(String[] args) {
		long begin = System.currentTimeMillis();
		LogQuery lq = qs.createQuery(args[0]);
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

		qs.removeQuery(lq.getId());
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
		qs.removeQuery(Integer.parseInt(args[0]));
		context.println("removed");
	}

	/**
	 * print all connected nodes
	 */
	public void nodes(String[] args) {
		context.println("Log DB Nodes");
		context.println("--------------");

	}

	@ScriptUsage(description = "connect to arbiter")
	public void connect(String[] args) {
		String host = args[0];
		int port = Integer.valueOf(args[1]);
	}

	public void disconnect(String[] args) {

	}

	/**
	 * print all distributed queries
	 */
	public void distQueries(String[] args) {
		context.println("Arbiter Queries");
		context.println("-----------------");

		for (ArbiterQueryStatus q : arbiter.getQueries())
			context.println(q);
	}

	public void distQuery(String[] args) {
		ArbiterQueryStatus q = arbiter.createQuery(args[0]);
		context.println(q);
	}
}
