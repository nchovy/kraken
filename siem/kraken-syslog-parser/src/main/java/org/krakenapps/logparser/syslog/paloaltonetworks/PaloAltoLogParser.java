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
package org.krakenapps.logparser.syslog.paloaltonetworks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.krakenapps.log.api.LogParser;

/**
 * Log Parser for Palo Alto Networks PA Series
 * 
 * @author xeraph
 * 
 */
public class PaloAltoLogParser implements LogParser {

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");

		List<String> tokens = tokenize(line);
		String type = tokens.get(3);
		if (type.equals("TRAFFIC"))
			return decodeTrafficLog(tokens);
		else if (type.equals("THREAT"))
			return decodeThreatLog(tokens);
		else if (type.equals("CONFIG"))
			return decodeConfigLog(tokens);
		else if (type.equals("SYSTEM"))
			return decodeSystemLog(tokens);

		return null;
	}

	private List<String> tokenize(String line) {
		List<String> l = new ArrayList<String>();

		int offset = 0;

		while (true) {
			int pos = line.indexOf(',', offset);
			if (pos < 0) {
				l.add(line.substring(offset));
				break;
			}

			l.add(line.substring(offset, pos));
			offset = pos + 1;
		}

		return l;
	}

	private Map<String, Object> decodeTrafficLog(List<String> tokens) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("recv_time", tokens.get(1));
		m.put("serial", tokens.get(2));
		m.put("type", tokens.get(3));
		m.put("subtype", tokens.get(4));
		m.put("src_ip", tokens.get(7));
		m.put("dst_ip", tokens.get(8));
		m.put("nat_src_ip", tokens.get(9));
		m.put("nat_dst_ip", tokens.get(10));
		m.put("rule", tokens.get(11));
		m.put("src_user", tokens.get(12));
		m.put("dst_user", tokens.get(13));
		m.put("application", tokens.get(14));
		m.put("virtual_system", tokens.get(15));
		m.put("src_zone", tokens.get(16));
		m.put("dst_zone", tokens.get(17));
		m.put("in_iface", tokens.get(18));
		m.put("out_iface", tokens.get(19));
		m.put("log_profile", tokens.get(20));
		m.put("session_id", tokens.get(22));
		m.put("repeat", Integer.valueOf(tokens.get(23)));
		m.put("src_port", Integer.valueOf(tokens.get(24)));
		m.put("dst_port", Integer.valueOf(tokens.get(25)));
		m.put("nat_src_port", Integer.valueOf(tokens.get(26)));
		m.put("nat_dst_port", Integer.valueOf(tokens.get(27)));
		m.put("flags", tokens.get(28));
		m.put("protocol", tokens.get(29));
		m.put("action", tokens.get(30));
		m.put("bytes", Long.valueOf(tokens.get(31)));
		m.put("packets", Long.valueOf(tokens.get(34)));
		m.put("start_time", tokens.get(35));
		m.put("elapsed_time", Integer.valueOf(tokens.get(36)));
		m.put("category", tokens.get(37));

		return m;
	}

	private Map<String, Object> decodeThreatLog(List<String> tokens) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("recv_time", tokens.get(1));
		m.put("serial", tokens.get(2));
		m.put("type", tokens.get(3));
		m.put("subtype", tokens.get(4));
		m.put("src_ip", tokens.get(7));
		m.put("dst_ip", tokens.get(8));
		m.put("nat_src_ip", tokens.get(9));
		m.put("nat_dst_ip", tokens.get(10));
		m.put("rule", tokens.get(11));
		m.put("src_user", tokens.get(12));
		m.put("dst_user", tokens.get(13));
		m.put("application", tokens.get(14));
		m.put("virtual_system", tokens.get(15));
		m.put("src_zone", tokens.get(16));
		m.put("dst_zone", tokens.get(17));
		m.put("in_iface", tokens.get(18));
		m.put("out_iface", tokens.get(19));
		m.put("log_profile", tokens.get(20));
		m.put("session_id", tokens.get(22));
		m.put("repeat", Integer.valueOf(tokens.get(23)));
		m.put("src_port", Integer.valueOf(tokens.get(24)));
		m.put("dst_port", Integer.valueOf(tokens.get(25)));
		m.put("nat_src_port", Integer.valueOf(tokens.get(26)));
		m.put("nat_dst_port", Integer.valueOf(tokens.get(27)));
		m.put("flags", tokens.get(28));
		m.put("protocol", tokens.get(29));
		m.put("action", tokens.get(30));
		String misc = tokens.get(31);
		m.put("misc", misc.substring(1, misc.length() - 1));
		m.put("threat_id", tokens.get(32));
		m.put("category", tokens.get(33));
		m.put("severity", tokens.get(34));
		m.put("direction", tokens.get(35));
		return m;
	}

	private Map<String, Object> decodeConfigLog(List<String> tokens) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("recv_time", tokens.get(1));
		m.put("serial", tokens.get(2));
		m.put("type", tokens.get(3));
		m.put("subtype", tokens.get(4));
		m.put("host", tokens.get(7));
		m.put("virtual_system", tokens.get(8));
		m.put("command", tokens.get(9));
		m.put("admin", tokens.get(10));
		m.put("client", tokens.get(11));
		m.put("result", tokens.get(12));
		m.put("config_path", tokens.get(13));
		return m;
	}

	private Map<String, Object> decodeSystemLog(List<String> tokens) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("recv_time", tokens.get(1));
		m.put("serial", tokens.get(2));
		m.put("type", tokens.get(3));
		m.put("subtype", tokens.get(4));
		m.put("virtual_system", tokens.get(7));
		m.put("event_id", tokens.get(8));
		m.put("object", tokens.get(9));
		m.put("module", tokens.get(12));
		m.put("severity", tokens.get(13));
		m.put("description", tokens.get(14));
		return m;
	}

}
