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
package org.krakenapps.logparser.syslog.xnsystems;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.logparser.syslog.xnsystems.NeoboxLogParser;

import static org.junit.Assert.*;

public class NeoboxParserTest {

	@Test
	public void testWafLog() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0101510001 class=웹필터링 subclass=허용 level=notice subject=192.168.0.17 result=성공 "
				+ "msg=\"policy_type=웹필터링 policy_action=허용 src=192.168.0.17 src_port=60067 -> dst=74.125.155.113 dst_port=80 url=http://safebrowsingcache.google.com/safebrowsing/rd/ChNnb29nLW1hbHdhcmUtc2hhdmFyEAEYhoIDIIaCAzIFBsEAAAE\"";
		Map<String, Object> m = new NeoboxLogParser().parse(map(line));
		assertEquals("웹필터링", m.get("policy_type"));
		assertEquals("허용", m.get("policy_action"));
		assertEquals("192.168.0.17", m.get("src"));
		assertEquals(60067, m.get("src_port"));
		assertEquals("74.125.155.113", m.get("dst"));
		assertEquals(80, m.get("dst_port"));
		assertEquals("http://safebrowsingcache.google.com/safebrowsing/rd/ChNnb29nLW1hbHdhcmUtc2hhdmFyEAEYhoIDIIaCAzIFBsEAAAE",
				m.get("url"));
	}

	@Test
	public void testFirewallLog() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0201510003 class=방화벽 subclass=허용 level=notice subject=192.168.0.195 result=성공 "
				+ "msg=\"policy_type=FORWARD policy_action=LOG in=brg0 src=192.168.0.195 src_port=0 -> out=brg0 dst=192.168.0.1 dst_port=0 proto=ICMP(ECHO_REQUEST)\"";
		Map<String, Object> m = new NeoboxLogParser().parse(map(line));
		assertEquals("FORWARD", m.get("policy_type"));
		assertEquals("LOG", m.get("policy_action"));
		assertEquals("brg0", m.get("in"));
		assertEquals("192.168.0.195", m.get("src"));
		assertEquals(0, m.get("src_port"));
		assertEquals("192.168.0.1", m.get("dst"));
		assertEquals(0, m.get("dst_port"));
		assertEquals("ICMP(ECHO_REQUEST)", m.get("proto"));
	}

	@Test
	public void testIpsLog() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0301510001 class=IPS subclass=탐지 level=notice subject=112.222.225.13 result=성공 "
				+ "msg=\"policy_type=IPS policy_action=signature-detect in=eth0 src=112.222.225.13 src_port=5060 -> out= dst=192.168.0.127 dst_port=5060 "
				+ "proto=udp sid=11969 smsg=VOIP-SIP inbound 401 unauthorized message\"";
		Map<String, Object> m = new NeoboxLogParser().parse(map(line));
		assertEquals("IPS", m.get("policy_type"));
		assertEquals("signature-detect", m.get("policy_action"));
		assertEquals("eth0", m.get("in"));
		assertEquals("112.222.225.13", m.get("src"));
		assertEquals(5060, m.get("src_port"));
		assertNull(m.get("out"));
		assertEquals("192.168.0.127", m.get("dst"));
		assertEquals(5060, m.get("dst_port"));
		assertEquals("udp", m.get("proto"));
		assertEquals("11969", m.get("sid"));
		assertEquals("VOIP-SIP inbound 401 unauthorized message", m.get("smsg"));
	}

	@Test
	public void testDosLog() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0402510001 class=DoS subclass=패킷차단 level=notice subject=192.168.0.86 result=성공 "
				+ "msg=\"policy_type=DoS policy_action=패킷차단 in=eth0 src=192.168.0.86 src_port=3148 -> dst=192.168.0.142 dst_port=0 proto=TCP reason=비정상 플래그의 TCP 패킷 차단\"";
		Map<String, Object> m = new NeoboxLogParser().parse(map(line));
		assertEquals("DoS", m.get("policy_type"));
		assertEquals("패킷차단", m.get("policy_action"));
		assertEquals("eth0", m.get("in"));
		assertEquals("192.168.0.86", m.get("src"));
		assertEquals(3148, m.get("src_port"));
		assertEquals("192.168.0.142", m.get("dst"));
		assertEquals(0, m.get("dst_port"));
		assertEquals("TCP", m.get("proto"));
		assertEquals("비정상 플래그의 TCP 패킷 차단", m.get("reason"));
	}

	@Test
	public void testEventLog() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0501510001 class=이벤트 subclass=인증 level=notice subject=admin result=성공 "
				+ "msg=\"관리자 admin 이(가) 보안장비 로그인에 성공하였습니다. (접속 IP=192.168.0.149 접속프로그램=web-browser) (LocalAuth)\"";
		Map<String, Object> m = new NeoboxLogParser().parse(map(line));
		assertEquals("관리자 admin 이(가) 보안장비 로그인에 성공하였습니다. (접속 IP=192.168.0.149 접속프로그램=web-browser) (LocalAuth)", m.get("msg"));
	}

	private Map<String, Object> map(String line) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", line);
		return m;
	}
}
