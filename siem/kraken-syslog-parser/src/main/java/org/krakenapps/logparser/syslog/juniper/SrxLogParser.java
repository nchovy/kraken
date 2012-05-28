package org.krakenapps.logparser.syslog.juniper;

import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SrxLogParser implements LogParser {
	private final Logger logger = LoggerFactory.getLogger(SrxLogParser.class.getName());

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		Map<String, Object> m = new HashMap<String, Object>();
		String line = (String) params.get("line");
		if (line == null)
			return null;

		try {
			
			
			
		} catch (Throwable t) {
			logger.warn("kraken syslog parser: cannot parse log [" + line + "]", t);
			return params;
		}

		return m;
	}

}
