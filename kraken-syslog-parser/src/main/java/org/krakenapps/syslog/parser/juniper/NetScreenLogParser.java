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
package org.krakenapps.syslog.parser.juniper;

import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.krakenapps.log.api.LogParser;
import org.krakenapps.syslog.parser.juniper.attack.JuniperAttackLogParser;
import org.krakenapps.syslog.parser.juniper.session.JuniperSessionLogParser;

@Component(name = "netscreen-log-parser")
@Provides
public class NetScreenLogParser implements LogParser {
	private org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());

	private JuniperSessionLogParser sessionPattern = JuniperSessionLogParser.newInstance();
	private JuniperAttackLogParser attackPattern = JuniperAttackLogParser.newInstance();

	@Override
	public String getName() {
		return "netscreen-isg";
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		Integer severity = (Integer) params.get("severity");
		//		Integer facility = (Integer) params.get("facility");

		String line = (String) params.get("msg");
		try {
			Map<String, Object> map = sessionPattern.parse(line);
			if (map == null && attackPattern != null) {
				map = attackPattern.parse(line);
			}
			map.put("severity", severity);
			map.put("facility", (Integer) params.get("facility"));

			return map;
		} catch (Exception e) {
			slog.debug("parse error for: " + line);
		}
		return null;
	}
}
