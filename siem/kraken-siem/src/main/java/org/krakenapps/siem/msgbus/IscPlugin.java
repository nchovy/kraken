/*
 * Copyright 2011 NCHOVY
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
package org.krakenapps.siem.msgbus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.isc.api.IscClient;
import org.krakenapps.msgbus.MsgbusException;
import org.krakenapps.msgbus.Request;
import org.krakenapps.msgbus.Response;
import org.krakenapps.msgbus.handler.MsgbusMethod;
import org.krakenapps.msgbus.handler.MsgbusPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "siem-isc-plugin")
@MsgbusPlugin
public class IscPlugin {
	private final Logger logger = LoggerFactory.getLogger(IscPlugin.class.getName());

	@Requires
	private IscClient client;

	@SuppressWarnings("unchecked")
	@MsgbusMethod
	public void call(Request req, Response resp) {
		try {
			String method = req.getString("method");
			List<Object> args = (List<Object>) req.get("args");
			resp.putAll(extract(client.call(method, args.toArray())));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getThreatcon(Request req, Response resp) {
		try {
			resp.putAll(extract(client.call("threatcon.get")));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getSansTopSourceSnapshots(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("sans.getTopSourceSnapshots", page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}

	}

	@MsgbusMethod
	public void getSansRisingPortSnapshots(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("sans.getRisingPortSnapshots", page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getSansPortReportSnapshots(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("sans.getPortReportSnapshots", page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getSansTopSources(Request req, Response resp) {
		try {
			String date = convertDate(req.getString("date"));
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String order = optional(req, "order", "attacks");
			resp.putAll(extract(client.call("sans.getTopSources", date, page, pageSize, order)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getSansRisingPorts(Request req, Response resp) {
		try {
			String date = convertDate(req.getString("date"));
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("sans.getRisingPorts", date, page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getSansPortReports(Request req, Response resp) {
		try {
			String date = convertDate(req.getString("date"));
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String order = optional(req, "order", "port");
			resp.putAll(extract(client.call("sans.getPortReports", date, page, pageSize, order)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getKrCertHackingPatternSnapshots(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("krcert.getHackingPatternSnapshots", page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getKrCertHackingPatterns(Request req, Response resp) {
		try {

			String date = convertDate(req.getString("date"));
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String order = optional(req, "order", "share");
			resp.putAll(extract(client.call("krcert.getHackingPatterns", date, page, pageSize, order)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getServiceport(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String query = optional(req, "query");
			String order = optional(req, "order", "port");
			resp.putAll(extract(client.call("serviceport.get", page, pageSize, query, order)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getCve(Request req, Response resp) {
		try {
			String name = req.getString("name");
			resp.putAll(extract(client.call("cve.get", name)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getCveList(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String query = optional(req, "order");
			resp.putAll(extract(client.call("cve.getList", page, pageSize, query)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getDeface(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			resp.putAll(extract(client.call("deface.get", page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void getRssEntries(Request req, Response resp) {
		try {
			String type = req.getString("type");
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String query = optional(req, "query");
			resp.putAll(extract(client.call("rss.entries", type, page, pageSize, query)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@MsgbusMethod
	public void searchIsc(Request req, Response resp) {
		try {
			int page = req.getInteger("page");
			int pageSize = req.getInteger("page_size");
			String query = req.getString("query");
			resp.putAll(extract(client.call("isc.search", query, page, pageSize)));
		} catch (Exception e) {
			logger.error("kraken siem: isc rpc error", e);
			throw new MsgbusException("siem", "rpc-error");
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> extract(Object obj) {
		if (obj instanceof ArrayList) {
			ArrayList<?> list = (ArrayList<?>) obj;
			if (list.isEmpty())
				return null;
			else
				return (Map<String, Object>) list.get(0);
		}

		return (Map<String, Object>) obj;
	}

	private String optional(Request req, String key) {
		return optional(req, key, "");
	}

	private String optional(Request req, String key, String def) {
		if (req.has(key))
			return req.getString(key);
		else
			return def;
	}

	private String convertDate(String date) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat to = new SimpleDateFormat("yyyyMMddHHmmss");
		try {
			return to.format(from.parse(date));
		} catch (Exception e) {
			return "";
		}
	}
}
