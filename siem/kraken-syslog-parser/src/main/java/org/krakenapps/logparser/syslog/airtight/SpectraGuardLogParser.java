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
package org.krakenapps.logparser.syslog.airtight;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpectraGuardLogParser implements LogParser {
	private final Logger logger = LoggerFactory.getLogger(SpectraGuardLogParser.class);

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		if (line == null)
			return null;

		HashMap<String, Object> m = new HashMap<String, Object>();
		try {
			Scanner sc = new Scanner(line);
			sc.useDelimiter(": ");

			String sensor = sc.next();
			String mac = sensor.substring(1, 18);

			m.put("sensor_mac", mac);
			m.put("sensor_version", sensor.substring(19).trim());

			String token = sc.next().trim();
			String msg = null;
			if (token.equals("Start") || token.equals("Stop")) {
				m.put("state", token);
				msg = sc.next().trim();
			} else {
				msg = token;
			}

			m.put("msg", msg);

			String location = sc.next().trim();
			int p = location.indexOf("://");
			String sensorIp = location.substring(0, p);
			location = location.substring(p + 1);

			m.put("sensor_ip", sensorIp);
			m.put("location", location);
			m.put("date", sc.next().trim());
			m.put("severity", sc.next().trim());

			if (msg.startsWith("Rogue Client")) {
				m.put("type", "Rogue Client");
				m.put("client", extract(msg));
			} else if (msg.startsWith("Rogue AP")) {
				m.put("type", "Rogue AP");
				m.put("ap", extract(msg));
			} else if (msg.startsWith("Mis-configured Authorized AP")) {
				m.put("type", "Misconfigured AP");
			} else if (msg.startsWith("Authorized Client")) {
				m.put("type", "Misbehaving Client");
				String client = extract(msg);
				if (client != null)
					m.put("client", client);
			} else if (msg.startsWith("Unauthorized Client")) {
				m.put("type", "Misbehaving Client");
				m.put("client", extract(msg));
			} else if (msg.startsWith("RF signature anomaly")) {
				m.put("type", "MAC Spoofing");
				m.put("client", extract(msg));
			} else if (msg.startsWith("Deauthentication flood attack")) {
				m.put("type", "DoS");
				m.put("ap", extract(msg, "AP ["));
				m.put("client", extract(msg, "Client ["));
			} else if (msg.startsWith("An Ad hoc network")) {
				m.put("type", "Ad Hoc");
				m.put("adhoc", extract(msg));
			} else if (msg.startsWith("Use of Fake AP tool detected")) {
				m.put("type", "Rogue AP");
				m.put("sensor_name", extract(msg));
			} else if (msg.startsWith("Indeterminate AP")) {
				m.put("type", "Rogue AP");
				m.put("ap", extract(msg));
			} else if (msg.startsWith("Authorized AP") && msg.endsWith("non-allowed channel.")) {
				m.put("type", "Misconfigured AP");
				m.put("ap", extract(msg));
			} else if (msg.startsWith("Possible use of Netstumbler")) {
				m.put("type", "Scanning");
				m.put("sensor_name", extract(msg, "Sensor ["));
				m.put("client", extract(msg, "Client ["));
			} else if (msg.startsWith("AP") && msg.endsWith("quarantined.")) {
				m.put("type", "Prevention");
				m.put("ap", extract(msg));
			} else if (msg.startsWith("Client") && msg.endsWith("quarantined.")) {
				m.put("type", "Prevention");
				m.put("client", extract(msg));
			}
		} catch (Throwable t) {
			logger.trace("cannot parse spectraguard log - " + line, t);
			m.put("line", line);
		}

		return m;
	}

	private String extract(String s) {
		return extract(s, "[");
	}

	private String extract(String s, String beginMarker) {
		int begin = s.indexOf(beginMarker);
		if (begin < 0)
			return null;

		int end = s.indexOf(']', begin + beginMarker.length());
		if (end < 0)
			return null;

		return s.substring(begin + beginMarker.length(), end);
	}
}
