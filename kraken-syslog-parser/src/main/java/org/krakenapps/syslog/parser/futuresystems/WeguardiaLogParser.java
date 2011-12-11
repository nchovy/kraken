package org.krakenapps.syslog.parser.futuresystems;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		try {
			Map<String, Object> m = new DelimiterParser(";", columnHeaders).parse(params);

			// parse date
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
			Date d = dateFormat.parse((String) m.get("date"));
			m.put("date", d);

			// parse src port
			String sport = (String) m.get("sport");
			if (sport != null) {
				sport = sport.trim();
				if (!sport.isEmpty())
					m.put("sport", Integer.valueOf(sport));
				else
					m.put("sport", null);
			}

			// parse dst port
			String dport = (String) m.get("dport");
			if (dport != null) {
				dport = dport.trim();
				if (!dport.isEmpty())
					m.put("dport", Integer.valueOf(dport));
				else
					m.put("dport", null);
			}

			// parse count
			String count = (String) m.get("count");
			if (count != null) {
				count = count.trim();
				if (!count.isEmpty())
					m.put("count", Integer.valueOf(count));
				else
					m.put("count", 1);
			}

			return m;
		} catch (ParseException e) {
			logger.warn("kraken syslog parser: cannot parse weguardia log [{}]", params);
		}
		return null;
	}
}
