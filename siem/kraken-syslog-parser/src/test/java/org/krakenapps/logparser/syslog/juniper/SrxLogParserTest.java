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
package org.krakenapps.logparser.syslog.juniper;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class SrxLogParserTest {

	@Test
	public void testSessionCreateLog() {
		SrxLogParser parser = new SrxLogParser();
		Map<String, Object> m = parser
				.parse(line("May  8 02:52:01 RT_FLOW: RT_FLOW_SESSION_CREATE: session created 10.213.110.248/34613->72.14.203.188/5228 None 211.36.132.131/43335->72.14.203.188/5228 r1 None 6 TCP_1H trust untrust 80497606"));

		assertEquals("create", m.get("action"));
		assertEquals("10.213.110.248", m.get("src_ip"));
		assertEquals(34613, m.get("src_port"));
		assertEquals("72.14.203.188", m.get("dst_ip"));
		assertEquals(5228, m.get("dst_port"));
		assertEquals("211.36.132.131", m.get("nat_src_ip"));
		assertEquals(43335, m.get("nat_src_port"));
		assertEquals("72.14.203.188", m.get("nat_dst_ip"));
		assertEquals(5228, m.get("nat_dst_port"));
		assertEquals("r1", m.get("src_nat_rule"));
		assertEquals("None", m.get("dst_nat_rule"));
		assertEquals("6", m.get("protocol"));
		assertEquals("TCP_1H", m.get("policy"));
		assertEquals("trust", m.get("src_zone"));
		assertEquals("untrust", m.get("dst_zone"));
		assertEquals("80497606", m.get("session_id"));
	}

	@Test
	public void testTcpRstLog() {
		SrxLogParser parser = new SrxLogParser();
		Map<String, Object> m = parser
				.parse(line("May  8 02:52:02 RT_FLOW: RT_FLOW_SESSION_CLOSE: session closed TCP RST: 10.254.251.48/35639->72.14.203.188/5228 None 211.36.132.123/40488->72.14.203.188/5228 r1 None 6 TCP_1H trust untrust 80888035 46(4109) 42(4812) 13512"));

		assertEquals("close", m.get("action"));
		assertEquals("TCP RST", m.get("reason"));
		assertEquals("10.254.251.48", m.get("src_ip"));
		assertEquals(35639, m.get("src_port"));
		assertEquals("72.14.203.188", m.get("dst_ip"));
		assertEquals(5228, m.get("dst_port"));
		assertEquals("r1", m.get("src_nat_rule"));
		assertEquals("None", m.get("dst_nat_rule"));
		assertEquals("6", m.get("protocol"));
		assertEquals("TCP_1H", m.get("policy"));
		assertEquals("trust", m.get("src_zone"));
		assertEquals("untrust", m.get("dst_zone"));
		assertEquals("80888035", m.get("session_id"));
		assertEquals(46L, m.get("sent_pkts"));
		assertEquals(4109L, m.get("sent_bytes"));
		assertEquals(42L, m.get("rcvd_pkts"));
		assertEquals(4812L, m.get("rcvd_bytes"));
		assertEquals(13512L, m.get("elapsed_time"));
	}

	@Test
	public void testTcpFinLog() {
		SrxLogParser parser = new SrxLogParser();
		Map<String, Object> m = parser
				.parse(line("May  8 02:52:06 RT_FLOW: RT_FLOW_SESSION_CLOSE: session closed TCP FIN: 10.253.4.144/48577->74.125.71.188/5228 None 211.36.132.229/52104->74.125.71.188/5228 r1 None 6 TCP_1H trust untrust 100186027 0(0) 0(0) 1"));

		assertEquals("close", m.get("action"));
		assertEquals("TCP FIN", m.get("reason"));
		assertEquals("10.253.4.144", m.get("src_ip"));
		assertEquals(48577, m.get("src_port"));
		assertEquals("74.125.71.188", m.get("dst_ip"));
		assertEquals(5228, m.get("dst_port"));
		assertEquals("None", m.get("service"));
		assertEquals("r1", m.get("src_nat_rule"));
		assertEquals("None", m.get("dst_nat_rule"));
		assertEquals("6", m.get("protocol"));
		assertEquals("TCP_1H", m.get("policy"));
		assertEquals("trust", m.get("src_zone"));
		assertEquals("untrust", m.get("dst_zone"));
		assertEquals("100186027", m.get("session_id"));
		assertEquals(0L, m.get("sent_pkts"));
		assertEquals(0L, m.get("sent_bytes"));
		assertEquals(0L, m.get("rcvd_pkts"));
		assertEquals(0L, m.get("rcvd_bytes"));
		assertEquals(1L, m.get("elapsed_time"));
	}

	@Test
	public void testUnsetLog() {
		SrxLogParser parser = new SrxLogParser();
		Map<String, Object> m = parser
				.parse(line("May  8 02:52:02 RT_FLOW: RT_FLOW_SESSION_CLOSE: session closed unset: 10.253.92.74/58350->72.14.203.188/5228 None 211.36.132.81/53350->72.14.203.188/5228 r1 None 6 TCP_1H trust untrust 80038716 58(5362) 64(6924) 28150"));

		assertEquals("close", m.get("action"));
		assertEquals("unset", m.get("reason"));
		assertEquals("10.253.92.74", m.get("src_ip"));
		assertEquals(58350, m.get("src_port"));
		assertEquals("72.14.203.188", m.get("dst_ip"));
		assertEquals(5228, m.get("dst_port"));
		assertEquals("None", m.get("service"));
		assertEquals("r1", m.get("src_nat_rule"));
		assertEquals("None", m.get("dst_nat_rule"));
		assertEquals("6", m.get("protocol"));
		assertEquals("TCP_1H", m.get("policy"));
		assertEquals("trust", m.get("src_zone"));
		assertEquals("untrust", m.get("dst_zone"));
		assertEquals("80038716", m.get("session_id"));
		assertEquals(58L, m.get("sent_pkts"));
		assertEquals(5362L, m.get("sent_bytes"));
		assertEquals(64L, m.get("rcvd_pkts"));
		assertEquals(6924L, m.get("rcvd_bytes"));
		assertEquals(28150L, m.get("elapsed_time"));
	}

	@Test
	public void testDenyLog() {
		SrxLogParser parser = new SrxLogParser();
		Map<String, Object> m = parser
				.parse(line("Dec 16 05:02:28  RT_FLOW: RT_FLOW_SESSION_DENY: session denied 10.0.0.32/9370->63.251.254.131/370 None 17(0) default-permit trust untrust"));

		assertEquals("deny", m.get("action"));
		assertEquals("10.0.0.32", m.get("src_ip"));
		assertEquals(9370, m.get("src_port"));
		assertEquals("63.251.254.131", m.get("dst_ip"));
		assertEquals(370, m.get("dst_port"));
		assertEquals("None", m.get("service"));
		assertEquals("17", m.get("protocol"));
		assertEquals("0", m.get("icmp_type"));
		assertEquals("default-permit", m.get("policy"));
		assertEquals("trust", m.get("src_zone"));
		assertEquals("untrust", m.get("dst_zone"));
	}

	private Map<String, Object> line(String line) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", line);
		return m;
	}
}
