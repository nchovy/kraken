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
package org.krakenapps.logparser.syslog.oullim;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class SecureWorksLogParserTest {
	@Test
	public void testTcpAllowLog() {
		String log = "SECUREWORKS: TCP  PACKET unknown 192.100.10.77:3095 -> 192.100.10.24:80 (ALLOW RULE=14 IFN=eth7)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("TCP", m.get("protocol"));
		assertEquals("192.100.10.77", m.get("src_ip"));
		assertEquals(3095, m.get("src_port"));
		assertEquals("192.100.10.24", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("ALLOW", m.get("action"));
		assertEquals("14", m.get("rule"));
		assertEquals("eth7", m.get("ifn"));
	}

	@Test
	public void testTcpDenyLog() {
		String log = "SECUREWORKS: TCP  PACKET unknown 192.100.10.21:2249 -> 192.100.10.116:2186 (DENY RULE=Default IFN=eth5)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("TCP", m.get("protocol"));
		assertEquals("192.100.10.21", m.get("src_ip"));
		assertEquals(2249, m.get("src_port"));
		assertEquals("192.100.10.116", m.get("dst_ip"));
		assertEquals(2186, m.get("dst_port"));
		assertEquals("DENY", m.get("action"));
		assertEquals("Default", m.get("rule"));
		assertEquals("eth5", m.get("ifn"));
	}

	@Test
	public void testTcpNatLog() {
		String log = "SECUREWORKS: TCP  PACKET unknown 192.100.10.74:3147 -> 20.50.129.11:80 (ALLOW RULE=48 IFN=eth7 NAT(NR)=(SRCADDR=11.118.22.13 SRCPORT=52085))";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("TCP", m.get("protocol"));
		assertEquals("192.100.10.74", m.get("src_ip"));
		assertEquals(3147, m.get("src_port"));
		assertEquals("20.50.129.11", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("ALLOW", m.get("action"));
		assertEquals("48", m.get("rule"));
		assertEquals("eth7", m.get("ifn"));
		assertEquals("NR", m.get("nat_type"));
		assertEquals("11.118.22.13", m.get("nat_src_ip"));
		assertEquals(52085, m.get("nat_src_port"));
	}

	@Test
	public void testTcpNatLog2() {
		String log = "SECUREWORKS: TCP  PACKET unknown 11.34.99.48:57710 -> 11.118.20.20:80 (ALLOW RULE=10 IFN=eth4 NAT(RV)=(DSTADDR=192.168.3.20 DSTPORT=80))";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("TCP", m.get("protocol"));
		assertEquals("11.34.99.48", m.get("src_ip"));
		assertEquals(57710, m.get("src_port"));
		assertEquals("11.118.20.20", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("ALLOW", m.get("action"));
		assertEquals("10", m.get("rule"));
		assertEquals("eth4", m.get("ifn"));
		assertEquals("RV", m.get("nat_type"));
		assertEquals("192.168.3.20", m.get("nat_dst_ip"));
		assertEquals(80, m.get("nat_dst_port"));
	}

	@Test
	public void testIcmpDenyLog() {
		String log = "SECUREWORKS: ICMP PACKET unknown 192.100.10.76:0 -> 13.199.15.22:0 (DENY RULE=Default TYPE=8 IFN=eth7)";

		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("ICMP", m.get("protocol"));
		assertEquals("192.100.10.76", m.get("src_ip"));
		assertEquals(0, m.get("src_port"));
		assertEquals("13.199.15.22", m.get("dst_ip"));
		assertEquals(0, m.get("dst_port"));
		assertEquals("DENY", m.get("action"));
		assertEquals("Default", m.get("rule"));
		assertEquals("8", m.get("icmp_type"));
		assertEquals("eth7", m.get("ifn"));
	}

	@Test
	public void testUdpAllowLog() {
		String log = "SECUREWORKS: UDP  PACKET unknown 21.78.122.49:2483 -> 11.118.20.13:161 (ALLOW RULE=60 IFN=eth4)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("UDP", m.get("protocol"));
		assertEquals("21.78.122.49", m.get("src_ip"));
		assertEquals(2483, m.get("src_port"));
		assertEquals("11.118.20.13", m.get("dst_ip"));
		assertEquals(161, m.get("dst_port"));
		assertEquals("ALLOW", m.get("action"));
		assertEquals("60", m.get("rule"));
		assertEquals("eth4", m.get("ifn"));
	}

	@Test
	public void testUdpDenyLog() {
		String log = "SECUREWORKS: UDP  PACKET unknown 192.100.10.96:137 -> 192.100.10.127:137 (DENY RULE=Default IFN=eth7)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("UDP", m.get("protocol"));
		assertEquals("192.100.10.96", m.get("src_ip"));
		assertEquals(137, m.get("src_port"));
		assertEquals("192.100.10.127", m.get("dst_ip"));
		assertEquals(137, m.get("dst_port"));
		assertEquals("DENY", m.get("action"));
		assertEquals("Default", m.get("rule"));
		assertEquals("eth7", m.get("ifn"));
	}

	@Test
	public void testUdpNatLog() {
		String log = "SECUREWORKS: UDP  PACKET unknown 192.100.10.125:53992 -> 23.248.22.2:53 (ALLOW RULE=48 IFN=eth7 NAT(NR)=(SRCADDR=11.118.22.13 SRCPORT=30606))";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("UDP", m.get("protocol"));
		assertEquals("192.100.10.125", m.get("src_ip"));
		assertEquals(53992, m.get("src_port"));
		assertEquals("23.248.22.2", m.get("dst_ip"));
		assertEquals(53, m.get("dst_port"));
		assertEquals("ALLOW", m.get("action"));
		assertEquals("48", m.get("rule"));
		assertEquals("eth7", m.get("ifn"));
		assertEquals("NR", m.get("nat_type"));
		assertEquals("11.118.22.13", m.get("nat_src_ip"));
		assertEquals(30606, m.get("nat_src_port"));
	}

	@Test
	public void testUdpStatLog() {
		String log = "SECUREWORKS: UDP  PACKET unknown 192.100.10.89:59119 -> 14.124.11.2:53 (PKTS=2 DATA=164 TIME=180)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals(2L, m.get("pkts"));
		assertEquals(164L, m.get("data"));
		assertEquals(180L, m.get("time"));
	}

	@Test
	public void testCpuLog() {
		String log = "SECUREWORKS: TCP  swmaind unknown 255.255.255.255:0 -> 255.255.255.255:0 (CPU Usage : 4)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals(4, m.get("cpu_usage"));
	}

	@Test
	public void testMemUsageLog() {
		String log = "SECUREWORKS: TCP  swmaind unknown 255.255.255.255:0 -> 255.255.255.255:0 (MEM : Total=1007.52M, Free=635.06M, Cached=513.68M)";
		SecureWorksLogParser p = new SecureWorksLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("1007.52M", m.get("mem_total"));
		assertEquals("635.06M", m.get("mem_free"));
		assertEquals("513.68M", m.get("mem_cached"));
	}

	@Ignore
	@Test
	public void testSwmaindLog() {
		@SuppressWarnings("unused")
		String log = "SECUREWORKS: TCP  swmaind unknown 255.255.255.255:0 -> 255.255.255.255:0 (0 112 7 9 9 27 381959 381200 759 277830342 0 0 1865 0 1477)";
	}

	private Map<String, Object> line(String log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", log);
		return m;
	}
}
