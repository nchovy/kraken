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
package org.krakenapps.logparser.snmptrap.pentasecurity;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.krakenapps.log.api.LogParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WapplesLogParser implements LogParser {
	private final Logger logger = LoggerFactory.getLogger(WapplesLogParser.class.getName());

	// detect oids
	private final static String DETECT_OID = "1.3.6.1.4.1.9772.1.2.0.1";
	private final static String DETECT_TIME_OID = DETECT_OID + ".1";
	private final static String DETECT_SRC_OID = DETECT_OID + ".2";
	private final static String DETECT_URI_OID = DETECT_OID + ".3";
	private final static String DETECT_RULE_ID_OID = DETECT_OID + ".4";
	private final static String DETECT_RAW_DATA_OID = DETECT_OID + ".5";
	private final static String DETECT_RESPONSE_VALUE_OID = DETECT_OID + ".6";
	private final static String DETECT_WEBSITE_HOSTNAME_OID = DETECT_OID + ".7";
	private final static String DETECT_DST_OID = DETECT_OID + ".8";

	// audit oids
	private final static String AUDIT_OID = "1.3.6.1.4.1.9772.1.2.0.2";
	private final static String AUDIT_SRC_OID = AUDIT_OID + ".1";
	private final static String AUDIT_TIME_OID = AUDIT_OID + ".2";
	private final static String AUDIT_INFO_OID = AUDIT_OID + ".3";
	private final static String AUDIT_TYPE_OID = AUDIT_OID + ".4";
	private final static String AUDIT_MSG_TYPE_OID = AUDIT_OID + ".5";

	// wapples status oids
	private final static String STATUS_OID = "1.3.6.1.4.1.9772.1.2.0.3";
	private final static String STATUS_TIME_OID = STATUS_OID + ".1";
	private final static String STATUS_CPU_USED_OID = STATUS_OID + ".2";
	private final static String STATUS_MEM_USED_OID = STATUS_OID + ".3";

	// web server check oids
	private final static String CHECK_OID = "1.3.6.1.4.1.9772.1.2.0.4";
	private final static String CHECK_BEGIN_TIME_OID = CHECK_OID + ".1";
	private final static String CHECK_END_TIME_OID = CHECK_OID + ".2";
	private final static String CHECK_SERVER_IP_OID = CHECK_OID + ".3";
	private final static String CHECK_SERVER_PORT_OID = CHECK_OID + ".4";
	private final static String CHECK_STATUS_CODE_OID = CHECK_OID + ".5";
	private final static String CHECK_TIMEOUT_COUNT_OID = CHECK_OID + ".6";

	@Override
	public Map<String, Object> parse(Map<String, Object> log) {
		if (logger.isDebugEnabled())
			logger.debug("kraken logparser snmptrap: wapples log [{}]", log);

		// check snmp trap oid
		String snmpTrapOid = (String) log.get("1.3.6.1.6.3.1.1.4.1.0");
		if (snmpTrapOid == null) {
			logger.warn("kraken logparser snmptrap: invalid wapples log, null snmp trap oid [{}]", log);
			return null;
		}

		// detection log
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm:ss");
			if (snmpTrapOid.equals("1.3.6.1.4.1.9772.1.2.0.1")) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("date", dateFormat.parse((String) log.get(DETECT_TIME_OID)));
				m.put("type", "detect");
				m.put("src", ((InetAddress) log.get(DETECT_SRC_OID)).getHostAddress());
				m.put("dst", ((InetAddress) log.get(DETECT_DST_OID)).getHostAddress());
				m.put("uri", log.get(DETECT_URI_OID));
				m.put("rule", getRuleName((Integer) log.get(DETECT_RULE_ID_OID)));
				m.put("rawdata", log.get(DETECT_RAW_DATA_OID));
				m.put("response", getResponseName((Integer) log.get(DETECT_RESPONSE_VALUE_OID)));
				m.put("hostname", log.get(DETECT_WEBSITE_HOSTNAME_OID));
				return m;
			}
			// audit log
			else if (snmpTrapOid.equals("1.3.6.1.4.1.9772.1.2.0.2")) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("date", dateFormat.parse((String) log.get(AUDIT_TIME_OID)));
				m.put("type", "audit");
				m.put("src", log.get(AUDIT_SRC_OID));
				m.put("info", log.get(AUDIT_INFO_OID));
				m.put("audit_type", log.get(AUDIT_TYPE_OID));
				m.put("audit_msg_type", log.get(AUDIT_MSG_TYPE_OID));
				return m;
			}
			// wapples status
			else if (snmpTrapOid.equals("1.3.6.1.4.1.9772.1.2.0.3")) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("date", dateFormat.parse((String) log.get(STATUS_TIME_OID)));
				m.put("type", "status");
				m.put("cpu_used", log.get(STATUS_CPU_USED_OID));
				m.put("mem_used", log.get(STATUS_MEM_USED_OID));
				return m;
			}
			// web server check
			else if (snmpTrapOid.equals("1.3.6.1.4.1.9772.1.2.0.4")) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("type", "server_check");
				m.put("begin_time", dateFormat.parse((String) log.get(CHECK_BEGIN_TIME_OID)));
				m.put("end_time", dateFormat.parse((String) log.get(CHECK_END_TIME_OID)));
				m.put("server_ip", ((InetAddress) log.get(CHECK_SERVER_IP_OID)).getHostAddress());
				m.put("server_port", log.get(CHECK_SERVER_PORT_OID));
				m.put("status_code", log.get(CHECK_STATUS_CODE_OID));
				m.put("timeout_count", log.get(CHECK_TIMEOUT_COUNT_OID));
				return m;
			}
		} catch (Throwable t) {
			logger.error("kraken logparser snmptrap: cannot parse wapples log => " + log, t);
			return null;
		}
		logger.warn("kraken logparser snmptrap: unrecognized snmp trap oid [{}]", snmpTrapOid);
		return null;
	}

	private static String getRuleName(int id) {
		switch (id) {
		case 1:
			return "xss";
		case 2:
			return "parameter_tampering";
		case 3:
			return "cookie_poisoning";
		case 4:
			return "buffer_overflow";
		case 5:
			return "sql_injection";
		case 6:
			return "input_invalidation";
		case 7:
			return "invalid_http_request";
		case 8:
			return "worm";
		case 9:
			return "invalid_uri";
		case 10:
			return "forceful_browsing";
		case 11:
			return "unknown_worm";
		case 12:
			return "file_upload";
		case 13:
			return "response_header_filtering";
		case 14:
			return "unicode_dir_traversal";
		case 15:
			return "known_attack";
		case 16:
			return "stealth_commanding";
		case 17:
			return "request_method_filtering";
		case 18:
			return "error_handling";
		case 19:
			return "uri_access_control";
		case 20:
			return "input_content_filtering";
		case 21:
			return "output_filtering";
		case 22:
			return "suspicious_access";
		case 23:
			return "invalid_http";
		case 24:
			return "web_site_defacement";
		case 25:
			return "output_content_filtering";
		case 26:
			return "directory_listing";
		case 27:
			return "extension_filtering";
		case 28:
			return "privacy_file_upload";
		case 29:
			return "ip_block";
		case 30:
			return "privacy_input_filtering";
		case 31:
			return "include_injection";
		case 32:
			return "ip_filtering";
		case 33:
			return "request_header_filtering";
		case 34:
			return "user_defined";
		default:
			return "rule-" + Integer.toString(id);
		}
	}

	private static String getResponseName(int value) {
		switch (value) {
		case -1:
			return "alter_content";
		case 0:
			return "no_response";
		case 1:
			return "redirect";
		case 2:
			return "error_code";
		case 3:
			return "disconnect";
		case 4:
			return "suspicious_access";
		default:
			return "response-" + value;
		}
	}
}
