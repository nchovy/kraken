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
package org.krakenapps.logparser.syslog.ahnlab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrusGuardLogParser implements LogParser {
	private final Logger logger = LoggerFactory.getLogger(TrusGuardLogParser.class.getName());

	@Override
	public Map<String, Object> parse(Map<String, Object> params) {
		String line = (String) params.get("line");
		if (line == null)
			return null;

		try {
			Map<String, Object> m = new HashMap<String, Object>();
			Scanner scanner = new Scanner(line);
			scanner.useDelimiter("`");

			// log header
			m.put("version", Integer.valueOf(scanner.next()));
			m.put("encrypt", Integer.valueOf(scanner.next()));

			int type = Integer.valueOf(scanner.next());
			m.put("type", type);
			m.put("count", Integer.valueOf(scanner.next()));
			m.put("utm_id", scanner.next());

			if (type == 1) { // kernel log (packet filter)
				parseFirewallLog(scanner, m);
			} else if (type == 2) { // application log
				parseApplicationLog(scanner, m);
			}

			return m;
		} catch (Throwable t) {
			logger.warn("kraken syslog parser: cannot parse trusguard log => " + line, t);
			return null;
		}
	}

	private void parseFirewallLog(Scanner scanner, Map<String, Object> m) {
		// log data
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String dateToken = scanner.next();
		String timeToken = scanner.next();
		try {
			m.put("date", dateFormat.parse(dateToken + " " + timeToken));
		} catch (ParseException e) {
		}

		String logType = scanner.next();
		m.put("logtype", logType);

		m.put("protocol", Integer.valueOf(scanner.next()));
		m.put("policy_id", scanner.next());
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.nextInt()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.nextInt()));
		m.put("in_nic", scanner.next());
		m.put("out_nic", scanner.next());

		String natTypeToken = scanner.next();
		m.put("nat_type", natTypeToken.isEmpty() ? null : natTypeToken);

		String natIp = scanner.next();
		m.put("nat_ip", natIp.isEmpty() ? null : natIp);

		String natPortToken = scanner.next();
		m.put("nat_port", natPortToken.isEmpty() ? null : Integer.valueOf(natPortToken));

		String sentDataToken = scanner.next();
		String sentPktToken = scanner.next();
		String rcvdDataToken = scanner.next();
		String rcvdPktToken = scanner.next();

		m.put("sent_data", sentDataToken.isEmpty() ? null : Long.valueOf(sentDataToken));
		m.put("sent_pkt", sentPktToken.isEmpty() ? null : Long.valueOf(sentPktToken));
		m.put("rcvd_data", rcvdDataToken.isEmpty() ? null : Long.valueOf(rcvdDataToken));
		m.put("rcvd_pkt", rcvdPktToken.isEmpty() ? null : Long.valueOf(rcvdPktToken));
	}

	private void parseApplicationLog(Scanner scanner, Map<String, Object> m) {
		int moduleFlag = Integer.valueOf(scanner.next());
		m.put("module_flag", moduleFlag);

		// log data
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String dateToken = scanner.next();
		String timeToken = scanner.next();
		try {
			m.put("date", dateFormat.parse(dateToken + " " + timeToken));
		} catch (ParseException e) {
		}

		if (moduleFlag == 1)
			parseOperationLog(scanner, m);
		else if (moduleFlag == 2)
			parseVirusLog(scanner, m);
		else if (moduleFlag == 3)
			parseSpamLog(scanner, m);
		else if (moduleFlag == 4)
			parseWebFilterLog(scanner, m);
		else if (moduleFlag == 6)
			parseAppFilterLog(scanner, m);
		else if (moduleFlag == 8)
			parseSslVpnLog(scanner, m);
		else if (moduleFlag == 9)
			parseIpsLog(scanner, m);
		else if (moduleFlag == 12)
			parseInternetAccessControlLog(scanner, m);
	}

	private void parseOperationLog(Scanner scanner, Map<String, Object> m) {
		String severityToken = scanner.next();
		scanner.next();

		m.put("severity", severityToken);
		scanner.next();
		scanner.next();
		m.put("action", scanner.next());
		scanner.next();
		m.put("module_name", scanner.next());
		m.put("description", scanner.next());
	}

	private void parseVirusLog(Scanner scanner, Map<String, Object> m) {
		String severityToken = scanner.next();
		String protocolToken = scanner.next();

		m.put("severity", severityToken);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("virus_filter", scanner.next());
		m.put("virus_name", scanner.next());

		String path = scanner.next();
		if (path.startsWith("[") && path.endsWith("]"))
			m.put("virus_url", path.substring(1, path.length() - 1));
		else
			m.put("virus_fname", path);

		if (scanner.hasNext()) {
			m.put("sender_addr", scanner.next());
			m.put("recipients_addr", scanner.next());
			m.put("subject", scanner.next());
		}
	}

	private void parseSpamLog(Scanner scanner, Map<String, Object> m) {
		String severityToken = scanner.next();
		String protocolToken = scanner.next();

		m.put("severity", severityToken);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());

		m.put("spam_filter", scanner.next());
		m.put("send_spam_log", scanner.next());
		m.put("sender_addr", scanner.next());
		m.put("recipients_addr", scanner.next());
		m.put("subject", scanner.next());
	}

	private void parseWebFilterLog(Scanner scanner, Map<String, Object> m) {
		String severityToken = scanner.next();
		String protocolToken = scanner.next();

		m.put("severity", severityToken);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("wf_type", scanner.next());
		m.put("reason", scanner.next());

		String url = scanner.next();
		m.put("url", url.substring(1, url.length() - 1));
	}

	private void parseAppFilterLog(Scanner scanner, Map<String, Object> m) {
		int severity = Integer.valueOf(scanner.next());
		String protocolToken = scanner.next();

		m.put("severity", severity);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src", scanner.next());
		m.put("dst", scanner.next());
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("ap_protocol", scanner.next());
		m.put("description", scanner.next());
	}

	private void parseSslVpnLog(Scanner scanner, Map<String, Object> m) {
		int severity = Integer.valueOf(scanner.next());
		String protocolToken = scanner.next();

		m.put("severity", severity);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("event", scanner.next());
		m.put("epsec", scanner.next());
	}

	private void parseIpsLog(Scanner scanner, Map<String, Object> m) {
		int severity = Integer.valueOf(scanner.next());
		String protocolToken = scanner.next();

		m.put("severity", severity);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("reason", scanner.next());
		m.put("nif", scanner.next());
		m.put("eth_protocol", scanner.next());
		m.put("src_mac", scanner.next());
		m.put("rule_id", scanner.next());
		m.put("vlan_id", scanner.next());
		m.put("msg", scanner.next());
	}

	private void parseInternetAccessControlLog(Scanner scanner, Map<String, Object> m) {
		int severity = Integer.valueOf(scanner.next());
		String protocolToken = scanner.next();

		m.put("severity", severity);
		m.put("protocol", protocolToken.isEmpty() ? null : Integer.valueOf(protocolToken));
		m.put("src_ip", scanner.next());
		m.put("src_port", Integer.valueOf(scanner.next()));
		m.put("dst_ip", scanner.next());
		m.put("dst_port", Integer.valueOf(scanner.next()));
		m.put("action", scanner.next());
		m.put("user", scanner.next());
		m.put("module_name", scanner.next());
		m.put("mac", scanner.next());
	}
}
