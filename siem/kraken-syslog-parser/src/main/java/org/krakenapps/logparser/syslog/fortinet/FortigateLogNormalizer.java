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
package org.krakenapps.logparser.syslog.fortinet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogNormalizer;

@Component(name = "fortigate-log-normalizer")
@Provides
public class FortigateLogNormalizer implements LogNormalizer {
	private org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		try {
			Map<String, Object> result = new HashMap<String, Object>();

			String logId = (String) params.get("log_id");
			String type = logId.substring(0, 2);

			result.put("severity", normalizeSeverity((String) params.get("pri")));

			// types and subtypes reference:
			// http://docs.fortinet.com/fgt/handbook/html/logging_bestpractices.43.3.html
			if ("00".equals(type)) {
				return handleFirewallLog(params, result);
			} else if ("04".equals(type)) {
				return handleIntrusionLog(params, result);
			} else {
				return null;
			}
		} catch (UnknownHostException uhe) {
			slog.warn("exception in fortigate log normalizing", uhe);
			return null;
		}
	}

	/**
	 * 0 Emergency --> 1 (Fatal) 1 Alert --> 2 (High) 2 Critical --> 2 (High) 3
	 * Error ---> 3 (Medium) 4 Warning --> 3 (Medium) 5 Notice/Notification -->
	 * 4 (Low) 6 Info/Information --> 5 (Info) 7 Debug --> 5 (Info)
	 */

	private Object normalizeSeverity(String pri) {
		try {
			int severity = Integer.parseInt(pri);
			switch (severity) {
			case 0:
				return 1;
			case 1:
			case 2:
				return 2;
			case 3:
			case 4:
				return 3;
			case 5:
				return 4;
			case 6:
			case 7:
				return 5;
			default:
				throw new IllegalArgumentException("Severity is not in range [0.7] : " + severity);
			}
		} catch (NumberFormatException nfe) {
			return parseStringSeverity(pri);
		}
	}

	private static Map<String, Integer> normalizedSeverityStrings = new HashMap<String, Integer>();
	static {
		Map<String, Integer> nss = normalizedSeverityStrings;
		nss.put("emergency", 1);
		nss.put("alert", 2);
		nss.put("critical", 2);
		nss.put("error", 3);
		nss.put("warn", 3);
		nss.put("warning", 3);
		nss.put("notification", 4);
		nss.put("notice", 4);
		nss.put("information", 5);
		nss.put("info", 5);
		nss.put("debug", 5);
	}

	private Object parseStringSeverity(String pri) {
		if (normalizedSeverityStrings.containsKey(pri.toLowerCase()))
			return normalizedSeverityStrings.get(pri);
		else
			throw new IllegalArgumentException("Severity cannot be recognized : " + pri);
	}

	private Map<String, Object> handleIntrusionLog(Map<String, Object> params, Map<String, Object> m) {
		m.put("src_ip", params.get("src"));
		m.put("dst_ip", params.get("dst"));
		m.put("src_port", params.get("src_port"));
		m.put("dst_port", params.get("dst_port"));
		m.put("rule", params.get("msg"));
		m.put("count", params.get("count"));
		m.put("severity", params.get("severity"));

		m.put("type", "intrusion");
		m.put("category", "unknown");
		return m;
	}

	private Map<String, Object> handleFirewallLog(Map<String, Object> params, Map<String, Object> m) throws UnknownHostException {
		String action = (String) params.get("status");

		m.put("type", "firewall");
		m.put("category", "session");

		m.put("src_ip", params.containsKey("src") ? InetAddress.getByName((String) params.get("src")) : null);
		m.put("src_port", params.get("src_port"));
		m.put("dst_ip", params.containsKey("dst") ? InetAddress.getByName((String) params.get("dst")) : null);
		m.put("dst_port", params.get("dst_port"));
		m.put("tx_bytes", Long.valueOf((String) params.get("sent")));
		m.put("rx_bytes", Long.valueOf((String) params.get("rcvd")));
		m.put("service", params.get("service"));
		m.put("policy", params.get("policyid"));
		m.put("action", action.toLowerCase());

		return m;
	}
}
