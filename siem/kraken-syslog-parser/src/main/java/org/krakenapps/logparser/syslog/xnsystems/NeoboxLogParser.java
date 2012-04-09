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
package org.krakenapps.logparser.syslog.xnsystems;

import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.krakenapps.logparser.syslog.internal.KeyValueParser;

public class NeoboxLogParser implements LogParser {
	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		Map<String, Object> m = KeyValueParser.parse((String) params.get("line"));
		if (m == null)
			return null;

		String xnid = (String) m.get("xnid");
		String msg = (String) m.get("msg");
		char c1 = xnid.charAt(0);
		char c2 = xnid.charAt(1);

		if (c1 == '0') {
			// web filtering
			if (c2 == '1') {
				m.put("policy_type", extract(msg, "policy_type"));
				m.put("policy_action", extract(msg, "policy_action"));
				m.put("src", extract(msg, "src"));
				m.put("src_port", Integer.valueOf(extract(msg, "src_port")));
				m.put("dst", extract(msg, "dst"));
				m.put("dst_port", Integer.valueOf(extract(msg, "dst_port")));
				m.put("url", extract(msg, "url"));
				m.remove("msg");
			}
			// firewall
			else if (c2 == '2') {
				m.put("policy_type", extract(msg, "policy_type"));
				m.put("policy_action", extract(msg, "policy_action"));
				m.put("in", extract(msg, "in"));
				m.put("src", extract(msg, "src"));
				m.put("src_port", Integer.valueOf(extract(msg, "src_port")));
				m.put("dst", extract(msg, "dst"));
				m.put("dst_port", Integer.valueOf(extract(msg, "dst_port")));
				m.put("proto", extract(msg, "proto"));
				m.remove("msg");
			}
			// ips
			else if (c2 == '3') {
				m.put("policy_type", extract(msg, "policy_type"));
				m.put("policy_action", extract(msg, "policy_action"));
				m.put("in", extract(msg, "in"));
				m.put("src", extract(msg, "src"));
				m.put("src_port", Integer.valueOf(extract(msg, "src_port")));
				m.put("dst", extract(msg, "dst"));
				m.put("dst_port", Integer.valueOf(extract(msg, "dst_port")));
				m.put("proto", extract(msg, "proto"));
				m.put("sid", extract(msg, "sid"));
				m.put("smsg", msg.substring(msg.indexOf("smsg=") + "smsg=".length()));
				m.remove("msg");
			}
			// dos
			else if (c2 == '4') {
				m.put("policy_type", extract(msg, "policy_type"));
				m.put("policy_action", extract(msg, "policy_action"));
				m.put("in", extract(msg, "in"));
				m.put("src", extract(msg, "src"));
				m.put("src_port", Integer.valueOf(extract(msg, "src_port")));
				m.put("dst", extract(msg, "dst"));
				m.put("dst_port", Integer.valueOf(extract(msg, "dst_port")));
				m.put("proto", extract(msg, "proto"));
				m.put("reason", msg.substring(msg.indexOf("reason=") + "reason=".length()));
				m.remove("msg");
			}
			// event
			else if (c2 == '5') {
				// do nothing
			}

		}

		return m;
	}

	private String extract(String msg, String key) {
		int begin = msg.indexOf(key);
		if (begin < 0)
			return null;

		int end = msg.indexOf(' ', begin + key.length());
		if (end < 0)
			return msg.substring(begin + key.length() + 1);

		return msg.substring(begin + key.length() + 1, end);
	}
}
