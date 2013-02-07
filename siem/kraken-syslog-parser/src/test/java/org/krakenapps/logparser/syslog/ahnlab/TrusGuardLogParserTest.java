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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class TrusGuardLogParserTest {

	@Test
	public void testDnsFilter() {
		String line = "1`0`2`1`000000`11`20080109`18:04:18`Low`17`10.0.1.1`1048`210.181.4.25`53`3001``DNS 필터`Private IP Query`(ahnlab.co.kr->172.31.11.0)`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

	}

	@Test
	public void testOperationLog() {
		String line = "1`0`2`1`000000`1`20071026`12:48:08`0````3009``운영 로그`TrusGuard UTM의 정책을 적용했습니다.`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("3009", m.get("action"));
		assertEquals("운영 로그", m.get("module_name"));
		assertEquals("TrusGuard UTM의 정책을 적용했습니다.", m.get("description"));
	}

	@Test
	public void testStatLog() {
		String line = "1`0`2`1`0bf075`1`20071025`18:00:44`0````3009``Operation Log`CPU: 19.280720, Memory: 22.252111, HDD: 30, Connections: 28, IN: 130.0Kbps, OUT: 68.3Kbps, IN:128 pps, OUT:41 pps, HA: OFF`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("0bf075", m.get("utm_id"));

		// check log data
		assertEquals("3009", m.get("action"));
		assertEquals("Operation Log", m.get("module_name"));
		assertEquals(
				"CPU: 19.280720, Memory: 22.252111, HDD: 30, Connections: 28, IN: 130.0Kbps, OUT: 68.3Kbps, IN:128 pps, OUT:41 pps, HA: OFF",
				m.get("description"));
	}

	@Test
	public void testAllowAndExpireLog() {
		String line = "1`0`1`1`000000`20071025`17:46:26`Expire`6`UTM_ADMINHOST`172.16.108.152`4430`172.16.108.211`50005`eth0`unknown````1021`8`724`7`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(1, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check firewall log data // check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(1, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		assertEquals("Expire", m.get("logtype"));
		assertEquals(6, m.get("protocol"));
		assertEquals("UTM_ADMINHOST", m.get("policy_id"));
		assertEquals("172.16.108.152", m.get("src_ip"));
		assertEquals(4430, m.get("src_port"));
		assertEquals("172.16.108.211", m.get("dst_ip"));
		assertEquals(50005, m.get("dst_port"));
		assertEquals("eth0", m.get("in_nic"));
		assertEquals("unknown", m.get("out_nic"));
		assertNull(m.get("nat_ip"));
		assertNull(m.get("nat_port"));
		assertEquals(1021L, m.get("sent_data"));
		assertEquals(8L, m.get("sent_pkt"));
		assertEquals(724L, m.get("rcvd_data"));
		assertEquals(7L, m.get("rcvd_pkt"));
	}

	@Test
	public void testAllowAndExpireNatLog() {
		String line = "1`0`1`1`000000`20071025`17:46:26`Expire`6`UTM_ADMINHOST`172.16.108.152`4430`172.16.108.211`50005`eth0`unknown`SNAT`210.16.108.194`11005`1021`8`724`7`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(1, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("Expire", m.get("logtype"));
		assertEquals(6, m.get("protocol"));
		assertEquals("UTM_ADMINHOST", m.get("policy_id"));
		assertEquals("172.16.108.152", m.get("src_ip"));
		assertEquals(4430, m.get("src_port"));
		assertEquals("172.16.108.211", m.get("dst_ip"));
		assertEquals(50005, m.get("dst_port"));
		assertEquals("eth0", m.get("in_nic"));
		assertEquals("unknown", m.get("out_nic"));
		assertEquals("SNAT", m.get("nat_type"));
		assertEquals("210.16.108.194", m.get("nat_ip"));
		assertEquals(11005, m.get("nat_port"));
		assertEquals(1021L, m.get("sent_data"));
		assertEquals(8L, m.get("sent_pkt"));
		assertEquals(724L, m.get("rcvd_data"));
		assertEquals(7L, m.get("rcvd_pkt"));

	}

	@Test
	public void testDenyLog() {
		String line = "1`0`1`1`000000`20071025`17:56:38`Deny`17`UTM_DEFAULT`172.16.104.4`137`172.16.255.255`137`eth0`unknown````19968`1```";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(1, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("Deny", m.get("logtype"));
		assertEquals(17, m.get("protocol"));
		assertEquals("UTM_DEFAULT", m.get("policy_id"));
		assertEquals("172.16.104.4", m.get("src_ip"));
		assertEquals(137, m.get("src_port"));
		assertEquals("172.16.255.255", m.get("dst_ip"));
		assertEquals(137, m.get("dst_port"));
		assertEquals("eth0", m.get("in_nic"));
		assertEquals("unknown", m.get("out_nic"));
		assertNull(m.get("nat_type"));
		assertNull(m.get("nat_ip"));
		assertNull(m.get("nat_port"));
		assertEquals(19968L, m.get("sent_data"));
		assertEquals(1L, m.get("sent_pkt"));
		assertNull(m.get("rcvd_data"));
		assertNull(m.get("rcvd_pkt"));

	}

	@Test
	public void testAppFilterLog() {
		String line = "1`0`2`1`000000`6`20071023`17:46:34`0````3009``콘텐츠 필터`FTP`출발지(172.16.104.2:46235)에서 목적지(202.79.178.98:21)로 연결이 종료되었습니다.`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(6, m.get("module_flag"));
		assertEquals(0, m.get("severity"));
		assertEquals("3009", m.get("action"));
		assertEquals("콘텐츠 필터", m.get("module_name"));
		assertEquals("FTP", m.get("ap_protocol"));
		assertEquals("출발지(172.16.104.2:46235)에서 목적지(202.79.178.98:21)로 연결이 종료되었습니다.", m.get("description"));
	}

	@Test
	public void testWebFilterLog() {
		String line = "1`0`2`1`000000`4`20071026`13:05:27`Low`6`172.16.108.144`3427`61.97.65.4`80`3001``웹사이트 필터`UserURL`UserURL`[http://www.empas.com/]`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("Low", m.get("severity"));
		assertEquals(6, m.get("protocol"));
		assertEquals("172.16.108.144", m.get("src_ip"));
		assertEquals(3427, m.get("src_port"));
		assertEquals("61.97.65.4", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("웹사이트 필터", m.get("module_name"));
		assertEquals("UserURL", m.get("wf_type"));
		assertEquals("UserURL", m.get("reason"));
		assertEquals("http://www.empas.com/", m.get("url"));
	}

	@Test
	public void testSmtpPop3Log() {
		String line = "1`0`2`1`000000`2`20071031`12:33:40`HIGH`6`60.1.100.6`49566`172.16.108.152`25`3001``바이러스 차단`1`EICAR_Test_File`eicar_com.zip`circleo@gmail.com`circleo@kornet.net`FW: 광고 ..테스트 메일`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("HIGH", m.get("severity"));
		assertEquals(6, m.get("protocol"));
		assertEquals("60.1.100.6", m.get("src_ip"));
		assertEquals(49566, m.get("src_port"));
		assertEquals("172.16.108.152", m.get("dst_ip"));
		assertEquals(25, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("바이러스 차단", m.get("module_name"));
		assertEquals("1", m.get("virus_filter"));
		assertEquals("EICAR_Test_File", m.get("virus_name"));
		assertEquals("eicar_com.zip", m.get("virus_fname"));
		assertEquals("circleo@gmail.com", m.get("sender_addr"));
		assertEquals("circleo@kornet.net", m.get("recipients_addr"));
		assertEquals("FW: 광고 ..테스트 메일", m.get("subject"));
	}

	@Test
	public void testFtpLog() {
		String line = "1`0`2`1`000000`2`20071030`14:31:48`HIGH`6`60.1.100.6`49566`172.16.108.152`21`3001``바이러스 차단`1`EICAR_Test_File`eicar_com.zip`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("HIGH", m.get("severity"));
		assertEquals(6, m.get("protocol"));
		assertEquals("60.1.100.6", m.get("src_ip"));
		assertEquals(49566, m.get("src_port"));
		assertEquals("172.16.108.152", m.get("dst_ip"));
		assertEquals(21, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("바이러스 차단", m.get("module_name"));
		assertEquals("1", m.get("virus_filter"));
		assertEquals("EICAR_Test_File", m.get("virus_name"));
		assertEquals("eicar_com.zip", m.get("virus_fname"));
	}

	@Test
	public void testHttpLog() {
		String line = "1`0`2`1`000000`2`20071030`12:58:43`HIGH`6`172.16.108.152`2118`88.198.38.136`80`3001``바이러스 차단`AntiVirus(V3)`EICAR_Test_File`[http://www.eicar.org/download/eicarcom2.zip]`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("HIGH", m.get("severity"));
		assertEquals(6, m.get("protocol"));
		assertEquals("172.16.108.152", m.get("src_ip"));
		assertEquals(2118, m.get("src_port"));
		assertEquals("88.198.38.136", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("바이러스 차단", m.get("module_name"));
		assertEquals("AntiVirus(V3)", m.get("virus_filter"));
		assertEquals("EICAR_Test_File", m.get("virus_name"));
		assertEquals("http://www.eicar.org/download/eicarcom2.zip", m.get("virus_url"));
	}

	@Test
	public void testSpamLog() {
		String line = "1`0`2`1`000000`3`20071009`11:35:41`Low`6`172.16.104.1`3748`211.48.62.132`110`3003``스팸 메일 차단`2`0`circleo@gmail.com`circleo@kornet.net`FW: 광고 ..테스트 메일`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals("Low", m.get("severity"));
		assertEquals(6, m.get("protocol"));
		assertEquals("172.16.104.1", m.get("src_ip"));
		assertEquals(3748, m.get("src_port"));
		assertEquals("211.48.62.132", m.get("dst_ip"));
		assertEquals(110, m.get("dst_port"));
		assertEquals("3003", m.get("action"));
		assertEquals("스팸 메일 차단", m.get("module_name"));
		assertEquals("2", m.get("spam_filter"));
		assertEquals("0", m.get("send_spam_log"));
		assertEquals("circleo@gmail.com", m.get("sender_addr"));
		assertEquals("circleo@kornet.net", m.get("recipients_addr"));
		assertEquals("FW: 광고 ..테스트 메일", m.get("subject"));
	}

	@Test
	public void testSslVpnLog() {
		String line = "1`0`2`1`000000`8`20071030`15:40:18`0`6`192.168.0.6`3021`60.1.100.6`22`3009`user1`SSL VPN`Session closed`Disabled`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(6, m.get("protocol"));
		assertEquals("192.168.0.6", m.get("src_ip"));
		assertEquals(3021, m.get("src_port"));
		assertEquals("60.1.100.6", m.get("dst_ip"));
		assertEquals(22, m.get("dst_port"));
		assertEquals("3009", m.get("action"));
		assertEquals("user1", m.get("user"));
		assertEquals("SSL VPN", m.get("module_name"));
		assertEquals("Session closed", m.get("event"));
		assertEquals("Disabled", m.get("epsec"));
	}

	@Test
	public void testDdosLog() {
		String line = "1`0`2`1`000000`9`20070515`15:45:29`2`17`5.5.5.1`14194`4.4.4.5`31335`3001``IPS`2012`3`0800`00:03:47:B5:B0:7`10232`65535` DDOS Trin00 Daemon to Master *HELLO* message detected";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(17, m.get("protocol"));
		assertEquals("5.5.5.1", m.get("src_ip"));
		assertEquals(14194, m.get("src_port"));
		assertEquals("4.4.4.5", m.get("dst_ip"));
		assertEquals(31335, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("IPS", m.get("module_name"));
		assertEquals("2012", m.get("reason"));
		assertEquals("3", m.get("nif"));
		assertEquals("0800", m.get("eth_protocol"));
		assertEquals("00:03:47:B5:B0:7", m.get("src_mac"));
		assertEquals("10232", m.get("rule_id"));
		assertEquals("65535", m.get("vlan_id"));
		assertEquals(" DDOS Trin00 Daemon to Master *HELLO* message detected", m.get("msg"));
	}

	@Test
	public void testExploitLog() {
		String line = "1`0`2`1`000000`9`20070515`15:45:58`1`17`5.5.5.1`14508`4.4.4.5`635`3001``IPS`2012`3`0800`00:03:47:B5:B0:7`10315`65535` EXPLOIT x86 Linux mountd overflow";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(17, m.get("protocol"));
		assertEquals("5.5.5.1", m.get("src_ip"));
		assertEquals(14508, m.get("src_port"));
		assertEquals("4.4.4.5", m.get("dst_ip"));
		assertEquals(635, m.get("dst_port"));
		assertEquals("3001", m.get("action"));
		assertEquals("IPS", m.get("module_name"));
		assertEquals("2012", m.get("reason"));
		assertEquals("3", m.get("nif"));
		assertEquals("0800", m.get("eth_protocol"));
		assertEquals("00:03:47:B5:B0:7", m.get("src_mac"));
		assertEquals("10315", m.get("rule_id"));
		assertEquals("65535", m.get("vlan_id"));
		assertEquals(" EXPLOIT x86 Linux mountd overflow", m.get("msg"));
	}

	@Ignore
	@Test
	public void testPortScanLog() {
		// this log has invalid delimiter formatting
		String line = "1`0`2`1`000000`9`20071025`09:16:38`3`6`172.16.108.144`3204`121.140.211.81`9101`3003``IPS`2012`1`0800`00:0F:B5:4D:84:EB` `1331003`-1`anomaly scan`";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(6, m.get("protocol"));
		assertEquals("172.16.108.144", m.get("src_ip"));
		assertEquals(3204, m.get("src_port"));
		assertEquals("121.140.211.81", m.get("dst_ip"));
		assertEquals(9101, m.get("dst_port"));
		assertEquals("3003", m.get("action"));
		assertEquals("IPS", m.get("module_name"));
		assertEquals("2012", m.get("reason"));
		assertEquals("1", m.get("nif"));
		assertEquals("0800", m.get("eth_protocol"));
		assertEquals("00:0F:B5:4D:84:EB", m.get("src_mac"));
		assertEquals("13331003", m.get("rule_id"));
		assertEquals(" ", m.get("vlan_id"));
		assertEquals("anomaly scan", m.get("msg"));
	}

	@Test
	public void testInternetAccessControlLog() {
		String line = "1`0`2`1`000000`12`20080328`01:57:51`4`17`192.168.1.1`4993`211.41.4.33`13568`4``IAC`00:10:f3:09:2c:34";
		Map<String, Object> m = new TrusGuardLogParser().parse(line(line));

		// check log header
		assertNotNull(m);
		assertEquals(1, m.get("version"));
		assertEquals(0, m.get("encrypt"));
		assertEquals(2, m.get("type"));
		assertEquals(1, m.get("count"));
		assertEquals("000000", m.get("utm_id"));

		// check log data
		assertEquals(4, m.get("severity"));
		assertEquals(17, m.get("protocol"));
		assertEquals("192.168.1.1", m.get("src_ip"));
		assertEquals(4993, m.get("src_port"));
		assertEquals("211.41.4.33", m.get("dst_ip"));
		assertEquals(13568, m.get("dst_port"));
		assertEquals("4", m.get("action"));
		assertEquals("IAC", m.get("module_name"));
		assertEquals("00:10:f3:09:2c:34", m.get("mac"));
	}

	private Map<String, Object> line(String line) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", line);
		return m;
	}
}
