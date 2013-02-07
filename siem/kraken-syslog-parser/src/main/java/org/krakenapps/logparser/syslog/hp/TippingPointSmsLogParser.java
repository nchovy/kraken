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
package org.krakenapps.logparser.syslog.hp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.krakenapps.log.api.LogParser;

/**
 * 
 * @author xeraph
 * @since 1.4.0
 */
public class TippingPointSmsLogParser implements LogParser {
	private enum FieldType {
		String, Integer, Date
	};

	private static final String[] Keys = new String[] { "action", "severity", "policy_uuid", "sig_uuid", "sig_name",
			"sig_no", "protocol", "src_ip", "src_port", "dst_ip", "dst_port", "hit", "device_slot", "device_segment",
			"device_name", "alarm_id", "datetime" };
	private static final FieldType[] Types = new FieldType[] { FieldType.Integer, FieldType.Integer, FieldType.String,
			FieldType.String, FieldType.String, FieldType.Integer, FieldType.String, FieldType.String,
			FieldType.Integer, FieldType.String, FieldType.Integer, FieldType.Integer, FieldType.Integer,
			FieldType.Integer, FieldType.String, FieldType.String, FieldType.Date };

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		StringTokenizer tok = new StringTokenizer(line, "\t");

		Map<String, Object> m = new HashMap<String, Object>();

		int i = 0;
		while (tok.hasMoreTokens()) {
			if (i >= 17)
				break;

			String key = Keys[i];
			FieldType type = Types[i++];
			String token = tok.nextToken();

			if (type == FieldType.Integer)
				m.put(key, Integer.valueOf(token));
			else if (type == FieldType.Date)
				m.put(key, new Date(Long.valueOf(token)));
			else
				m.put(key, token);
		}

		return m;
	}

}
