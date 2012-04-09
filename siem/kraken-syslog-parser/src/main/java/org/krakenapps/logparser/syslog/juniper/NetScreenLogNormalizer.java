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
package org.krakenapps.logparser.syslog.juniper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetScreenLogNormalizer implements LogNormalizer {
	private final Logger logger = LoggerFactory.getLogger(NetScreenLogNormalizer.class.getName());

	/** @formatter:off
	 * 0 Emergency  --> 1 (Fatal)
	 * 1 Alert     --> 2 (High)
	 * 2 Critical  --> 2 (High)
	 * 3 Error  ---> 3 (Medium)
	 * 4 Warn   --> 3 (Medium)
	 * 5 Notice --> 4 (Low)
	 * 6 Info   --> 5 (Info)
	 * 7 Debug  --> 5 (Info)
	 */ // @formatter:on
	private int normalizeSeverity(int originalSeverity) {
		switch (originalSeverity) {
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
			throw new IllegalArgumentException("Severity is not in range [0.7] : " + originalSeverity);
		}
	}

	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		try {
			Map<String, Object> m = new HashMap<String, Object>();

			String category = (String) params.get("category");
			if (category == null) {
				logger.debug("kraken syslog parser: category is null, bug check");
				return null;
			}

			m.put("severity", normalizeSeverity((Integer) params.get("severity")));

			if (category.equals("traffic"))
				return handleFirewallLog(params, m);
			else if (category.equals("intrusion"))
				return handleIntrusionLog(params, m);

			return null;
		} catch (UnknownHostException e) {
			logger.warn("");
			return null;
		}
	}

	private Map<String, Object> handleIntrusionLog(Map<String, Object> params, Map<String, Object> m) {
		m.put("src_ip", params.get("src-ip"));
		m.put("dst_ip", params.get("dst-ip"));
		m.put("src_port", params.get("src-port"));
		m.put("dst_port", params.get("dst-port"));
		m.put("rule", params.get("rule"));
		m.put("count", params.get("count"));
		m.put("severity", params.get("severity"));

		m.put("type", "intrusion");
		m.put("category", "unknown");
		return m;
	}

	private Map<String, Object> handleFirewallLog(Map<String, Object> params, Map<String, Object> m) throws UnknownHostException {
		String action = (String) params.get("action");

		m.put("type", "firewall");
		m.put("category", "session");

		m.put("src_ip", params.containsKey("src") ? InetAddress.getByName((String) params.get("src")) : null);
		m.put("src_port", params.get("src_port"));
		m.put("dst_ip", params.containsKey("dst") ? InetAddress.getByName((String) params.get("dst")) : null);
		m.put("dst_port", params.get("dst_port"));
		m.put("tx_bytes", Long.valueOf((String) params.get("sent")));
		m.put("rx_bytes", Long.valueOf((String) params.get("rcvd")));
		m.put("service", params.get("service"));
		m.put("policy", params.get("policy_id"));
		m.put("action", action.toLowerCase());

		return m;
	}
}
