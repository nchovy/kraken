package org.krakenapps.syslog.parser.futuresystems;

import java.util.Date;
import java.util.Map;

import org.krakenapps.log.api.IpsLog;
import org.krakenapps.log.api.LogNormalizer;

public class WeguardiaLogNormalizer implements LogNormalizer {

	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		int logtype = Integer.valueOf((String) params.get("logtype"));

		switch (logtype) {
		// case 1: // firewall
		// return parseFirewall(params);
		case 2: // dpi
			return parseIps(params);
		}

		return null;
	}

	private Map<String, Object> parseIps(Map<String, Object> params) {
		IpsLog log = new IpsLog();
		log.setDate((Date) params.get("date"));

		// debug(1), info(2), normal(3), warn(4), serious(5), critical(6)
		log.setSeverity(normalizeSeverity((String) params.get("severity")));
		log.setSrc((String) params.get("sip"));
		log.setDst((String) params.get("dip"));
		log.setSrcPort((Integer) params.get("sport"));
		log.setDstPort((Integer) params.get("dport"));
		log.setProtocol((String) params.get("protocol"));
		log.setRule((String) params.get("rule"));
		log.setDetail((String) params.get("note"));
		log.setCount((Integer) params.get("count"));

		return log;
	}

	private int normalizeSeverity(String value) {
		int v = Integer.valueOf(value);
		switch (v) {
		case 1:
			return 5;
		case 2:
			return 5;
		case 3:
			return 4;
		case 4:
			return 3;
		case 5:
			return 2;
		case 6:
			return 1;
		default:
			return 0;
		}
	}
}
