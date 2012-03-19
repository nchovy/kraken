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
package org.krakenapps.linux.api.msgbus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.krakenapps.linux.api.DnsConfig;
import org.krakenapps.linux.api.DnsConfig.Sortlist;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "linux-dns-config-plugin")
@MsgbusPlugin
public class DnsConfigPlugin {
	private final Logger logger = LoggerFactory.getLogger(DnsConfigPlugin.class);

	@MsgbusMethod
	public void getDnsConfig(Request req, Response resp) throws IOException {
		resp.put("config", Marshaler.marshal(DnsConfig.getConfig()));
	}

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void setDnsConfig(Request req, Response resp) throws IOException {
		try {
			List<InetAddress> nameserver = new ArrayList<InetAddress>();
			List<String> nServer = (List<String>) req.get("nameserver");
			for (String name : nServer)
				nameserver.add(InetAddress.getByName(name));

			String domain = req.getString("domain");
			List<String> search = (List<String>) req.get("search");
			List<Sortlist> sortlist = new ArrayList<Sortlist>();
			List<Map<String, String>> sList = (List<Map<String, String>>) req.get("sortlist");
			for (Map<String, String> sort : sList) {
				InetAddress addr = InetAddress.getByName(sort.get("address"));
				if (sort.containsKey("netmask")) {
					sortlist.add(new Sortlist(addr, InetAddress.getByName(sort.get("netmask"))));
				} else {
					sortlist.add(new Sortlist(addr));
				}
			}

			DnsConfig config = DnsConfig.getConfig();
			config.removeAllNameserver();
			for (InetAddress addr : nameserver)
				config.addNameserver(addr);
			config.setDomain(domain);
			config.removeAllSearch();
			for (String str : search)
				config.addSearch(str);
			config.removeAllSortlist();
			for (Sortlist list : sortlist)
				config.addSortlist(list);
			config.save();
		} catch (UnknownHostException e) {
			logger.error("kraken-linux-api: unknown host", e);
		}
	}

}
