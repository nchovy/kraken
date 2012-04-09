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
package org.krakenapps.logparser.syslog.paloaltonetworks;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for Palo Alto log parser. I have no "config" and "system" sample
 * logs. Send me sample logs if you have.
 * 
 * @author xeraph@nchovy.com
 * 
 */
public class PaloAltoLogParserTest {
	@Test
	public void testTrafficLog() {
		String log = "Feb 22 16:27:11 1,2012/02/22 16:27:11,0002C101615,TRAFFIC,end,0,2012/02/22 16:27:10,172.16.246.56,112.76.169.110,0.0.0.0,0.0.0.0,"
				+ "VPN,,,web-browsing,vsys1,L3_VPN,L3_DMZ,vlan,vlan.2,traffic_IPS_182,2012/02/22 16:27:10,344092,1,3744,80,0,0,0x0,tcp,allow,"
				+ "1732,1732,1732,7,2012/02/22 16:24:30,130,any,0,0,0x0,172.16.0.0-172.31.255.255,Korea Republic Of,0";
		Map<String, Object> m = new PaloAltoLogParser().parse(line(log));

		assertEquals("2012/02/22 16:27:11", m.get("recv_time"));
		assertEquals("0002C101615", m.get("serial"));
		assertEquals("TRAFFIC", m.get("type"));
		assertEquals("end", m.get("subtype"));
		assertEquals("172.16.246.56", m.get("src_ip"));
		assertEquals("112.76.169.110", m.get("dst_ip"));
		assertEquals("0.0.0.0", m.get("nat_src_ip"));
		assertEquals("0.0.0.0", m.get("nat_dst_ip"));
		assertEquals("VPN", m.get("rule"));
		assertEquals("", m.get("src_user"));
		assertEquals("", m.get("dst_user"));
		assertEquals("web-browsing", m.get("application"));
		assertEquals("vsys1", m.get("virtual_system"));
		assertEquals("L3_VPN", m.get("src_zone"));
		assertEquals("L3_DMZ", m.get("dst_zone"));
		assertEquals("vlan", m.get("in_iface"));
		assertEquals("vlan.2", m.get("out_iface"));
		assertEquals("traffic_IPS_182", m.get("log_profile"));
		assertEquals("344092", m.get("session_id"));
		assertEquals(1, m.get("repeat"));
		assertEquals(3744, m.get("src_port"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("0x0", m.get("flags"));
		assertEquals("tcp", m.get("protocol"));
		assertEquals("allow", m.get("action"));
		assertEquals(1732L, m.get("bytes"));
		assertEquals(7L, m.get("packets"));
		assertEquals("2012/02/22 16:24:30", m.get("start_time"));
		assertEquals(130, m.get("elapsed_time"));
		assertEquals("any", m.get("category"));
	}

	@Test
	public void testThreatLog() {
		String log = "Feb 22 16:25:52 1,2012/02/22 16:25:52,0002C101615,THREAT,vulnerability,0,2012/02/22 16:25:47,112.76.169.97,172.16.1.99,0.0.0.0,0.0.0.0,"
				+ "VPN,ds\\a2010110,ds\\administrator,msrpc,vsys1,L3_DMZ,L3_trust,vlan.2,ethernet1/17,traffic_IPS_182,2012/02/22 16:25:51,204543,1,3204,135,0,0,0x80000000,tcp,alert,"
				+ "\"\",Microsoft RPC Endpoint Mapper(30845),any,low,client-to-server,0,0x0,Korea Republic Of,172.16.0.0-172.31.255.255,0,";
		Map<String, Object> m = new PaloAltoLogParser().parse(line(log));

		assertEquals("2012/02/22 16:25:52", m.get("recv_time"));
		assertEquals("0002C101615", m.get("serial"));
		assertEquals("THREAT", m.get("type"));
		assertEquals("vulnerability", m.get("subtype"));
		assertEquals("112.76.169.97", m.get("src_ip"));
		assertEquals("172.16.1.99", m.get("dst_ip"));
		assertEquals("0.0.0.0", m.get("nat_src_ip"));
		assertEquals("0.0.0.0", m.get("nat_dst_ip"));
		assertEquals("VPN", m.get("rule"));
		assertEquals("ds\\a2010110", m.get("src_user"));
		assertEquals("ds\\administrator", m.get("dst_user"));
		assertEquals("msrpc", m.get("application"));
		assertEquals("vsys1", m.get("virtual_system"));
		assertEquals("L3_DMZ", m.get("src_zone"));
		assertEquals("L3_trust", m.get("dst_zone"));
		assertEquals("vlan.2", m.get("in_iface"));
		assertEquals("ethernet1/17", m.get("out_iface"));
		assertEquals("traffic_IPS_182", m.get("log_profile"));
		assertEquals("204543", m.get("session_id"));
		assertEquals(1, m.get("repeat"));
		assertEquals(3204, m.get("src_port"));
		assertEquals(135, m.get("dst_port"));
		assertEquals(0, m.get("nat_src_port"));
		assertEquals(0, m.get("nat_dst_port"));
		assertEquals("0x80000000", m.get("flags"));
		assertEquals("tcp", m.get("protocol"));
		assertEquals("alert", m.get("action"));
		assertEquals("", m.get("misc"));
		assertEquals("Microsoft RPC Endpoint Mapper(30845)", m.get("threat_id"));
		assertEquals("any", m.get("category"));
		assertEquals("low", m.get("severity"));
		assertEquals("client-to-server", m.get("direction"));
	}

	private Map<String, Object> line(String line) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", line);
		return m;
	}
}
