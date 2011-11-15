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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.api.Script;
import org.krakenapps.api.ScriptArgument;
import org.krakenapps.api.ScriptContext;
import org.krakenapps.api.ScriptUsage;
import org.krakenapps.logdb.DataSource;
import org.krakenapps.logdb.DataSourceRegistry;
import org.krakenapps.logdb.LogQueryService;
import org.krakenapps.logdb.LogQuery;
import org.krakenapps.logdb.LogQueryCommand;
import org.krakenapps.logdb.mapreduce.MapReduceQueryStatus;
import org.krakenapps.logdb.mapreduce.MapReduceService;
import org.krakenapps.logdb.mapreduce.RemoteQuery;
import org.krakenapps.logdb.query.FileBufferList;
import org.krakenapps.logdb.query.command.RpcFrom;
import org.krakenapps.rpc.RpcConnection;
import org.krakenapps.rpc.RpcConnectionProperties;

public class LogDBScript implements Script {
	private LogQueryService qs;
	private DataSourceRegistry dsr;
	private MapReduceService mapreduce;
	private ScriptContext context;

	public LogDBScript(LogQueryService qs, DataSourceRegistry dsr, MapReduceService arbiter) {
		this.qs = qs;
		this.dsr = dsr;
		this.mapreduce = arbiter;
	}

	@Override
	public void setScriptContext(ScriptContext context) {
		this.context = context;
	}

	public void datasources(String[] args) {
		context.println("Data Sources");
		context.println("--------------");
		for (DataSource ds : dsr.getAll())
			context.println(ds);
	}

	@ScriptUsage(description = "print datasource metadata", arguments = {
			@ScriptArgument(name = "node", type = "string", description = "node guid or 'local'"),
			@ScriptArgument(name = "name", type = "string", description = "data source name") })
	public void datasource(String[] args) {
		String nodeGuid = args[0];
		String name = args[1];

		DataSource found = null;
		for (DataSource ds : dsr.getAll())
			if (ds.getNodeGuid().equals(nodeGuid) && ds.getName().equals(name))
				found = ds;

		if (found == null) {
			context.println("data source not found");
			return;
		}

		for (String key : found.getMetadata().keySet())
			context.println(key + ": " + found.getMetadata().get(key));
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
		qs.startQuery(lq.getId());

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

	public void remoteQueries(String[] args) {
		context.println("Remote Queries");
		context.println("----------------------");

		for (RemoteQuery q : mapreduce.getRemoteQueries()) {
			context.println(q);
		}
	}

	public void upstreams(String[] args) {
		context.println("Upstream Connections");
		context.println("----------------------");
		for (RpcConnection conn : mapreduce.getUpstreamConnections())
			context.println(conn);
	}

	public void downstreams(String[] args) {
		context.println("Downstream Connections");
		context.println("----------------------");
		for (RpcConnection conn : mapreduce.getDownstreamConnections())
			context.println(conn);
	}

	@ScriptUsage(description = "connect to arbiter", arguments = {
			@ScriptArgument(name = "host", type = "string", description = "host address"),
			@ScriptArgument(name = "port", type = "int", description = "port"),
			@ScriptArgument(name = "password", type = "string", description = "password") })
	public void connect(String[] args) {
		String host = args[0];
		int port = Integer.valueOf(args[1]);

		RpcConnectionProperties props = new RpcConnectionProperties(host, port);
		props.setPassword(args[2]);
		RpcConnection conn = mapreduce.connect(props);
		if (conn != null)
			context.println("connected " + conn);
		else
			context.printf("cannot connect to %s:%d\n", host, port);
	}

	@ScriptUsage(description = "disconnect from arbiter", arguments = { @ScriptArgument(name = "guid", type = "string", description = "rpc peer guid") })
	public void disconnect(String[] args) {
		mapreduce.disconnect(args[0]);
		context.println("disconnected");
	}

	/**
	 * print all mapreduce queries
	 */
	public void mrqueries(String[] args) {
		context.println("MapReduce Queries");
		context.println("-----------------");

		for (MapReduceQueryStatus q : mapreduce.getQueries())
			context.println(q);
	}

	public void mrquery(String[] args) {
		MapReduceQueryStatus q = mapreduce.createQuery(args[0]);
		if (q == null) {
			context.println("mapreduce query failed");
			return;
		}

		context.println("starting " + q);
		mapreduce.startQuery(q.getGuid());

		LogQuery lq = q.getReduceQuery().getQuery();
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
	}

	@ScriptUsage(description = "push to rpcfrom", arguments = {
			@ScriptArgument(name = "guid", type = "string", description = "dist query guid"),
			@ScriptArgument(name = "sample string", type = "string", description = "sample string") })
	public void rpcfrom(String[] args) {
		RpcFrom rpc = mapreduce.getRpcFrom(args[0]);
		if (rpc == null) {
			context.println("rpc not found");
			return;
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("data", args[1]);
		rpc.push(data);
	}

	@ScriptUsage(description = "eof to rpcfrom", arguments = { @ScriptArgument(name = "guid", type = "string", description = "dist query guid") })
	public void rpceof(String[] args) {
		RpcFrom rpc = mapreduce.getRpcFrom(args[0]);
		if (rpc == null) {
			context.println("rpc not found");
			return;
		}

		rpc.eof();
	}
}
