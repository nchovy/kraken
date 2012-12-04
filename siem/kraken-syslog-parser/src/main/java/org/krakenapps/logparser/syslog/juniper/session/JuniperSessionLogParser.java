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
package org.krakenapps.logparser.syslog.juniper.session;

import java.util.HashMap;
import java.util.Map;

public class JuniperSessionLogParser {
	private static final String DEVICE_ID = "device_id";
	private static final String CategoryHint = "[Root]system-notification";

	private JuniperSessionLogParser() {
	}

	public static JuniperSessionLogParser newInstance() {
		return new JuniperSessionLogParser();
	}

	public Map<String, Object> parse(String line) {
		// hashtable threshold will be 30 (max 24~25 tokens)
		Map<String, Object> map = new HashMap<String, Object>(40);
		int pos = 0;
		int limit = 0;
		pos = parseDeviceId(line, map, pos);
		limit = parseCategory(line, map, pos);

		if (pos == -1 || limit == -1)
			return null;

		String oldKey = null;

		while (limit < line.length()) {
			pos = line.indexOf(' ', limit) + 1;
			limit = line.indexOf('=', pos);
			if (pos == 0 || limit == -1)
				break;
			String key = line.substring(pos, limit);
			pos = limit + 1;
			if (line.charAt(pos) == '"') {
				pos++;
				limit = line.indexOf('"', pos);
			} else {
				limit = line.indexOf(' ', pos);
			}
			if (limit == -1 || "reason".equals(key))
				limit = line.length();

			if ("port".equals(key)) {
				if (oldKey.equals("src-xlated ip"))
					key = "src-xlated port";
				else
					key = "dst-xlated port";
			} else if ("service".equals(key)) {
				limit = line.indexOf("proto", limit) - 1;
			}

			String value = line.substring(pos, limit);
			// System.out.println(key+"="+value);
			oldKey = key;
			map.put(key, value);
		}

		return map;
	}

	private static int parseDeviceId(String line, Map<String, Object> map, int pos) {
		int offset = line.indexOf(DEVICE_ID, pos);
		if (offset == -1)
			return -1;
		offset += DEVICE_ID.length() + 1;
		int limit = line.indexOf(' ', offset);

		map.put(DEVICE_ID, line.substring(offset, limit));

		return limit;

	}

	private static int parseCategory(String line, Map<String, Object> map, int pos) {
		int offset = line.indexOf(CategoryHint, pos);
		if (offset == -1)
			return -1;
		offset += CategoryHint.length();
		offset = line.indexOf('(', offset) + 1;
		int limit = line.indexOf(')', offset);

		map.put("category", line.substring(offset, limit));

		return limit;
	}
}
