/*
 * Copyright 2012 Future Systems, Inc
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
package org.krakenapps.logparser.syslog.oullim;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.krakenapps.log.api.LogParser;

public class SecureWorksLogParser implements LogParser {

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		Scanner s = new Scanner(line);
		s.useDelimiter(" ");

		HashMap<String, Object> m = new HashMap<String, Object>();
		s.next();

		String protocol = s.next();
		m.put("protocol", protocol);

		String logger = null;
		if (protocol.equals("ICMP")) {
			logger = s.next();
			s.next();
		} else {
			s.next();
			logger = s.next();
			s.next();
		}

		String src = s.next();
		int p = src.indexOf(':');
		m.put("src_ip", src.substring(0, p));
		m.put("src_port", Integer.valueOf(src.substring(p + 1)));

		// skip arrow
		s.next();

		String dst = s.next();
		p = dst.indexOf(':');
		m.put("dst_ip", dst.substring(0, p));
		m.put("dst_port", Integer.valueOf(dst.substring(p + 1)));

		s.useDelimiter("\n");
		String remain = s.next().trim();
		remain = remain.substring(1, remain.length() - 1);

		String[] tokens = remain.split(" ");

		if (logger.equals("PACKET")) {
			if (tokens[0].equals("ALLOW") || tokens[0].equals("DENY")) {
				m.put("logger", "PACKET");
				m.put("action", tokens[0]);

				for (int i = 1; i < tokens.length; i++) {
					String t = tokens[i];
					if (t.startsWith("RULE")) {
						m.put("rule", t.substring(5));
					} else if (t.startsWith("IFN")) {
						m.put("ifn", t.substring(4));
					} else if (t.startsWith("NAT(NR)")) {
						m.put("nat_src_ip", t.substring(17));
						m.put("nat_type", "NR");
					} else if (t.startsWith("NAT(RV")) {
						m.put("nat_dst_ip", t.substring(17));
						m.put("nat_type", "RV");
					} else if (t.startsWith("SRCPORT")) {
						m.put("nat_src_port", Integer.valueOf(t.substring(8, t.length() - 1)));
					} else if (t.startsWith("DSTPORT")) {
						m.put("nat_dst_port", Integer.valueOf(t.substring(8, t.length() - 1)));
					} else if (t.startsWith("TYPE")) {
						m.put("icmp_type", t.substring(5));
					}
				}
			} else if (tokens[0].startsWith("PKTS")) {
				m.put("pkts", Long.valueOf(tokens[0].substring(5)));
				m.put("data", Long.valueOf(tokens[1].substring(5)));
				m.put("time", Long.valueOf(tokens[2].substring(5)));
			}

		} else if (logger.equals("swmaind")) {
			if (tokens[0].equals("CPU")) {
				m.put("cpu_usage", Integer.valueOf(tokens[3]));
			} else if (tokens[0].equals("MEM")) {
				m.put("mem_total", tokens[2].substring(6, tokens[2].length() - 1));
				m.put("mem_free", tokens[3].substring(5, tokens[3].length() - 1));
				m.put("mem_cached", tokens[4].substring(7, tokens[4].length()));
			}
		}

		return m;
	}
}
