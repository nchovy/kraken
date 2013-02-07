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

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krakenapps.log.api.FirewallLog;
import org.krakenapps.log.api.IpsLog;
import org.krakenapps.log.api.LogNormalizer;

public class WeguardiaLogNormalizer implements LogNormalizer {

	private final Set<Long> contentBlocks = new HashSet<Long>();
	private final Set<Long> contentDetects = new HashSet<Long>();
	private final Set<Long> contentAllows = new HashSet<Long>();

	public WeguardiaLogNormalizer() {
		contentBlocks.add(0x26030001L); // smtp match filter
		contentBlocks.add(0x27030001L); // smtp transform filter
		contentBlocks.add(0x28030001L); // smtp advanced filter
		contentBlocks.add(0x29040001L); // http cf block
		contentBlocks.add(0x29040002L); // http kiscom block
		contentBlocks.add(0x29040003L); // url block game
		contentBlocks.add(0x29040004L); // url block stock
		contentBlocks.add(0x29040005L); // url block news
		contentBlocks.add(0x29040006L); // url block iptv
		contentBlocks.add(0x29040007L); // url block email
		contentBlocks.add(0x29040008L); // url block webhard
		contentBlocks.add(0x29040009L); // url block p2p
		contentBlocks.add(0x2904000aL); // url block user

		contentDetects.add(0x26030002L); // smtp match detect
		contentDetects.add(0x27030002L); // smtp transform detect
		contentDetects.add(0x28030002L); // smtp advanced detect
		contentDetects.add(0x2902000bL); // http cf detect

		contentAllows.add(0x26020003L); // smtp match allows
		contentAllows.add(0x27020003L); // smtp transform allows
		contentAllows.add(0x28020003L); // smtp advanced allows
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
		int logtype = Integer.valueOf((String) params.get("logtype"));
		String rule = (String) params.get("rule");
		String act = (String) params.get("act");
		long actNo = Long.valueOf(act);

		FirewallLog log = new FirewallLog();
		log.setDate((Date) params.get("date"));
		log.setSubtype("session");
		log.setAction("accept");

		if (logtype == 9) {
			log.setSubtype("attack");
			log.setAction("drop");
		} else if (((actNo & 0x21000000) > 0) // dos
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

		if (actNo == 0x20040003L) // packet filter drop
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
