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
package org.krakenapps.linux.api.msgbus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.linux.api.RoutingEntry;
import org.krakenapps.linux.api.RoutingTable;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "linux-routing-table-plugin")
@MsgbusPlugin
public class RoutingTablePlugin {
	private final Logger logger = LoggerFactory.getLogger(RoutingTablePlugin.class);

	@MsgbusMethod
	public void getRoutingTable(Request req, Response resp) throws IOException {
		resp.put("routing_table", Marshaler.marshal(RoutingTable.getRoutingEntries()));
	}

	@MsgbusMethod
	public void getRoutingTableV6(Request req, Response resp) throws IOException {
		resp.put("routing_table", Marshaler.marshal(RoutingTable.getIpv6RoutingEntries()));
	}
	
	@MsgbusMethod
	public void addRoutingEntry(Request req, Response resp) {
		RoutingEntry entry = parseRoutingEntry(req);
		boolean isHost = req.getBoolean("is_host");
		String msg = RoutingTable.addRoutingEntries(entry, isHost);
		resp.put("message", msg);
	}

	@MsgbusMethod
	public void deleteRoutingEntry(Request req, Response resp) {
		RoutingEntry entry = parseRoutingEntry(req);
		boolean isHost = req.getBoolean("is_host");
		String msg = RoutingTable.deleteRoutingEntries(entry, isHost);
		resp.put("message", msg);
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void deleteRoutingEntries(Request req, Response resp) {
		List<String> msgs = new ArrayList<String>();
		List<Object> objs = (List<Object>) req.get("routing_tables");
		for (Object obj : objs) {
			Map<String, Object> m = (Map<String, Object>) obj;
			try {
				InetAddress destination = InetAddress.getByName((String) m.get("destination"));
				InetAddress gateway = InetAddress.getByName((String) m.get("gateway"));
				InetAddress genmask = InetAddress.getByName((String) m.get("genmask"));
				Integer metric = (Integer) m.get("metric");
				String iface = (String) m.get("iface");
				boolean isHost = (Boolean) m.get("is_host");
				RoutingEntry entry = new RoutingEntry(destination, gateway, genmask, null, metric, null, null, iface,
						null, null, null);

				msgs.add(RoutingTable.deleteRoutingEntries(entry, isHost));
			} catch (UnknownHostException e) {
				logger.error("kraken-linux-api: unknown host", e);
			}
		}
		resp.put("messages", msgs);
	}

	private RoutingEntry parseRoutingEntry(Request req) {
		try {
			InetAddress destination = InetAddress.getByName(req.getString("destination"));
			InetAddress gateway = req.has("gateway") ? InetAddress.getByName(req.getString("gateway")) : null;
			InetAddress genmask = req.has("genmask") ? InetAddress.getByName(req.getString("genmask")) : null;
			Integer metric = req.has("metric") ? req.getInteger("metric") : null;
			String iface = req.has("iface") ? req.getString("iface") : null;
			return new RoutingEntry(destination, gateway, genmask, null, metric, null, null, iface, null, null, null);
		} catch (UnknownHostException e) {
			logger.error("kraken-linux-api: unknown host", e);
		}
		return null;
	}
}
