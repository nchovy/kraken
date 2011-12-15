package org.krakenapps.logfile;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogNormalizer;

public class ApacheWebLogNormalizer implements LogNormalizer {
	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		if (!params.containsKey("logtype"))
			return null;

		String type = (String) params.get("logtype");
		if (!type.equals("httpd"))
			return null;

		/*
		 * keys = { "client_ip", "server_ip", "resp_bytes", "resp_bytes_clf",
		 * "cookie", "duration_msec", "env", "file", "remote_host", "protocol",
		 * "req_header", "login", "method", "note", "resp_header", "pid",
		 * "server_port", "query", "request", "status", "date", "duration_sec",
		 * "user", "url", "canonical_name", "server_name", "connection", "rcvd",
		 * "sent" }
		 */

		Map<String, Object> m = new HashMap<String, Object>();
		m.put("date", params.get("date"));
		m.put("category", "web");
		m.put("method", params.get("method"));
		m.put("url", params.get("url"));
		m.put("status", params.get("status"));
		m.put("src_ip", params.get("client_ip"));
		m.put("dst_ip", params.get("server_ip"));
		m.put("dst_port", params.get("server_port"));
		m.put("rcvd", params.get("rcvd"));
		m.put("sent", params.get("sent"));

		return m;
	}
}
