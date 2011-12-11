package org.krakenapps.syslog.parser.futuresystems;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.log.api.FirewallLog;
import org.krakenapps.log.api.IpsLog;
import org.krakenapps.log.api.LogNormalizer;

public class WeguardiaLogNormalizer implements LogNormalizer {

	private final Set<Integer> contentBlocks = new HashSet<Integer>();
	private final Set<Integer> contentDetects = new HashSet<Integer>();
	private final Set<Integer> contentAllows = new HashSet<Integer>();

	public WeguardiaLogNormalizer() {
		contentBlocks.add(0x26030001); // smtp match filter
		contentBlocks.add(0x27030001); // smtp transform filter
		contentBlocks.add(0x28030001); // smtp advanced filter
		contentBlocks.add(0x29040001); // http cf block
		contentBlocks.add(0x29040002); // http kiscom block
		contentBlocks.add(0x29040003); // url block game
		contentBlocks.add(0x29040004); // url block stock
		contentBlocks.add(0x29040005); // url block news
		contentBlocks.add(0x29040006); // url block iptv
		contentBlocks.add(0x29040007); // url block email
		contentBlocks.add(0x29040008); // url block webhard
		contentBlocks.add(0x29040009); // url block p2p
		contentBlocks.add(0x2904000a); // url block user

		contentDetects.add(0x26030002); // smtp match detect
		contentDetects.add(0x27030002); // smtp transform detect
		contentDetects.add(0x28030002); // smtp advanced detect
		contentDetects.add(0x2902000b); // http cf detect

		contentAllows.add(0x26020003); // smtp match allows
		contentAllows.add(0x27020003); // smtp transform allows
		contentAllows.add(0x28020003); // smtp advanced allows
	}

	@Override
	public Map<String, Object> normalize(Map<String, Object> params) {
		int logtype = Integer.valueOf((String) params.get("logtype"));

		switch (logtype) {
		case 1: // firewall
		case 9: // ddos
			return parseFirewall(params);
		case 2: // dpi
			return parseIps(params);
		}

		return null;
	}

	private Map<String, Object> parseFirewall(Map<String, Object> params) {
		String rule = (String) params.get("rule");
		String act = (String) params.get("act");
		int actNo = Integer.valueOf(act);

		FirewallLog log = new FirewallLog();
		log.setDate((Date) params.get("date"));
		log.setSubtype("session");
		log.setAction("accept");

		if (((actNo & 0x21000000) > 0) // dos
				|| ((actNo & 0x22000000) > 0) // ddos
				|| ((actNo & 0x23000000) > 0) // portscan
				|| ((actNo & 0x24000000) > 0)) // ip spoof
		{
			log.setSubtype("attack");
			log.setAction("drop");
		} else if (contentBlocks.contains(actNo)) {
			log.setSubtype("content-filter");
			log.setAction("drop");
		} else if (contentDetects.contains(actNo)) {
			log.setSubtype("content-filter");
		} else if (contentAllows.contains(actNo)) {
			log.setSubtype("content-filter");
		}

		// debug(1), info(2), normal(3), warn(4), serious(5), critical(6)
		log.setSeverity(normalizeSeverity((String) params.get("severity")));
		log.setSrc((String) params.get("sip"));
		log.setDst((String) params.get("dip"));
		log.setSrcPort((Integer) params.get("sport"));
		log.setDstPort((Integer) params.get("dport"));
		log.setProtocol((String) params.get("protocol"));
		log.setRule(rule);
		log.setDetail((String) params.get("note"));
		log.setCount((Integer) params.get("count"));

		if (actNo == 0x20040003)
			log.setAction("drop");

		return log;
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
