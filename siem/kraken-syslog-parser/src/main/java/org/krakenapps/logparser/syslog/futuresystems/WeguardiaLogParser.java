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
package org.krakenapps.logparser.syslog.futuresystems;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.krakenapps.log.api.DelimiterParser;
import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeguardiaLogParser implements LogParser {
	private static final String[] columnHeaders = new String[] { "type", "date", "oip", "sip", "nat_sip", "sport", "nat_sport",
			"dip", "nat_dip", "dport", "nat_dport", "protocol", "logtype", "act", "severity", "product", "note", "count",
			"category", "rule", "group_id", "usage", "user", "iface" };

	private final Logger logger = LoggerFactory.getLogger(WeguardiaLogParser.class.getName());

	private DelimiterParser parser;
	private ThreadLocal<Calendar> dateFormatters;

	public WeguardiaLogParser() {
		parser = new DelimiterParser(";", columnHeaders);
		dateFormatters = new ThreadLocal<Calendar>() {
			@Override
			protected Calendar initialValue() {
				return Calendar.getInstance();
			}
		};
	}

	private Date parse(Calendar c, String s) {
		c.set(Calendar.YEAR, Integer.valueOf(s.substring(0, 4)));
		c.set(Calendar.MONTH, Integer.valueOf(s.substring(4, 6)) - 1);
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(s.substring(6, 8)));

		c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(s.substring(9, 11)));
		c.set(Calendar.MINUTE, Integer.valueOf(s.substring(11, 13)));
		c.set(Calendar.SECOND, Integer.valueOf(s.substring(13, 15)));
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		try {
			Map<String, Object> m = parser.parse(params);
			Calendar c = dateFormatters.get();

			// parse date
			Date d = parse(c, (String) m.get("date"));
			m.put("date", d);

			// parse src port
			String sport = (String) m.get("sport");
			if (sport != null) {
				if (!sport.isEmpty())
					m.put("sport", Integer.valueOf(sport));
				else
					m.put("sport", null);
			}

			// parse dst port
			String dport = (String) m.get("dport");
			if (dport != null) {
				if (!dport.isEmpty())
					m.put("dport", Integer.valueOf(dport));
				else
					m.put("dport", null);
			}

			// parse count
			String count = (String) m.get("count");
			if (count != null) {
				if (!count.isEmpty())
					m.put("count", Integer.valueOf(count));
				else
					m.put("count", 1);
			}

			return m;
		} catch (Exception e) {
			logger.warn("kraken syslog parser: cannot parse weguardia log [{}]", params);
		}
		return null;
	}
}
