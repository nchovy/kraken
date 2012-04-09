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
package org.krakenapps.logparser.syslog.radware;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefenseProLogParser implements LogParser {
	private final Logger logger = LoggerFactory.getLogger(DefenseProLogParser.class.getName());

	@Override
	public Map<String, Object> parse(Map<String, Object> props) {
		try {
			Map<String, Object> m = new HashMap<String, Object>();
			String line = (String) props.get("line");
			int offset = 0;

			// mark after DefensePro:
			offset = line.indexOf(':', offset);
			int pos = line.indexOf(' ', offset + 2);

			// go to end of time string
			pos = line.indexOf(' ', pos + 1);
			String date = line.substring(offset + 2, pos);

			// parse priority
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String priority = line.substring(offset, pos);

			// parse attack id
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String attackId = line.substring(offset, pos);

			// parse category
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String category = line.substring(offset, pos);

			// parse attack name
			offset = pos + 2; // skip opening quote
			pos = line.indexOf('"', offset);
			String attackName = line.substring(offset, pos);

			// parse protocol
			offset = pos + 2; // skip closing quote
			pos = line.indexOf(' ', offset);
			String protocol = line.substring(offset, pos);

			// parse src
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String src = line.substring(offset, pos);

			// parse src port
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			int srcPort = Integer.valueOf(line.substring(offset, pos));

			// parse dst
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String dst = line.substring(offset, pos);

			// parse dst port
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			int dstPort = Integer.valueOf(line.substring(offset, pos));

			// parse physical port
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			int phyPort = Integer.valueOf(line.substring(offset, pos));

			// parse signature type
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String sigType = line.substring(offset, pos);

			// parse policy name
			offset = pos + 2; // skip opening quote
			pos = line.indexOf('"', offset);
			String policyName = line.substring(offset, pos);

			// parse attack status
			offset = pos + 2; // skip closing quote
			pos = line.indexOf(' ', offset);
			String attackStatus = line.substring(offset, pos);

			// parse attack count
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			int attackCount = Integer.valueOf(line.substring(offset, pos));

			// parse bandwidth
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			int bandwidth = Integer.valueOf(line.substring(offset, pos));

			// parse vlan
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String vlan = line.substring(offset, pos);

			// skip unknown1
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			@SuppressWarnings("unused")
			int unknown1 = Integer.valueOf(line.substring(offset, pos));

			// skip unknown2
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			@SuppressWarnings("unused")
			String unknown2 = line.substring(offset, pos);

			// parse criticity
			offset = pos + 1;
			pos = line.indexOf(' ', offset);
			String criticity = line.substring(offset, pos);

			// parse action
			offset = pos + 1;
			String action = line.substring(offset);

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			try {
				m.put("date", dateFormat.parse(date));
			} catch (ParseException e) {
			}

			m.put("priority", priority);
			m.put("attack_id", attackId);
			m.put("category", category);
			m.put("attack_name", attackName);
			m.put("protocol", protocol);
			m.put("src", src);
			m.put("src_port", srcPort);
			m.put("dst", dst);
			m.put("dst_port", dstPort);
			m.put("phy_port", phyPort);
			m.put("sig_type", sigType);
			m.put("policy_name", policyName);
			m.put("attack_status", attackStatus);
			m.put("attack_count", attackCount);
			m.put("bandwidth", bandwidth);
			m.put("vlan", vlan);
			m.put("criticity", criticity);
			m.put("action", action);
			return m;
		} catch (Throwable t) {
			// error sample 1: "[DefensePro: 04-04-2012 22:19:36 WARNING Fan
			// failure was detected. 1 fan is not operational"
			// error sample 2: "[DefensePro: 04-04-2012 22:13:17 WARNING Failed
			// to allocate an entry from the Server Cracking Counters table (all
			// containers are used). Tuning of the table size is required."
			logger.error("kraken syslog parser: cannot parse defense pro log [{}]", props.get("line"));
			return props;
		}
	}
}
