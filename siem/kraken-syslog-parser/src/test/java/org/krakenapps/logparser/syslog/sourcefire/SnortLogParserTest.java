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
package org.krakenapps.logparser.syslog.sourcefire;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.logparser.syslog.sourcefire.SnortLogParser;

import static org.junit.Assert.*;

public class SnortLogParserTest {
	private SnortLogParser parser = new SnortLogParser();

	@Test
	public void testCase1() {
		String log = "snort[20586]: [1:1000000:0] what the hell {TCP} 10.10.0.10:5432 -> 10.10.0.8:1837";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", log);
		Map<String, Object> m = parser.parse(params);

		assertEquals(20586, m.get("pid"));
		assertEquals(1, m.get("gid"));
		assertEquals(1000000, m.get("sid"));
		assertEquals(0, m.get("rev"));
		assertEquals("what the hell", m.get("msg"));
		assertEquals("TCP", m.get("proto"));
		assertEquals("/10.10.0.10", m.get("src_ip").toString());
		assertEquals(5432, m.get("src_port"));
		assertEquals("/10.10.0.8", m.get("dst_ip").toString());
		assertEquals(1837, m.get("dst_port"));
	}

	@Test
	public void testCase2() {
		String log = "<33>last message repeated 7 times";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", log);

		Map<String, Object> m = parser.parse(params);
		assertNull(m);
	}

	@Test
	public void testCase3() {
		String log = "snort[24858]: [122:17:0] (portscan) UDP Portscan[Priority: 3]: {PROTO:255} 220.45.142.139 -> 10.10.0.10";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", log);
		Map<String, Object> m = parser.parse(params);

		assertEquals(24858, m.get("pid"));
		assertEquals(122, m.get("gid"));
		assertEquals(17, m.get("sid"));
		assertEquals(0, m.get("rev"));
		assertEquals("(portscan) UDP Portscan", m.get("msg"));
		assertEquals("255", m.get("proto"));
		assertEquals("/220.45.142.139", m.get("src_ip").toString());
		assertNull(m.get("src_port"));
		assertEquals("/10.10.0.10", m.get("dst_ip").toString());
		assertNull(m.get("dst_port"));
	}

	@Test
	public void testCase4() {
		String log = "snort[24858]: [1:486:4] ICMP Destination Unreachable Communication with Destination Host is Administratively Prohibited [Classification: Misc activity] [Priority: 3]: {ICMP} 220.45.142.139 -> 10.10.0.2";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("message", log);
		Map<String, Object> m = parser.parse(params);

		assertEquals(24858, m.get("pid"));
		assertEquals(1, m.get("gid"));
		assertEquals(486, m.get("sid"));
		assertEquals(4, m.get("rev"));
		assertEquals("ICMP Destination Unreachable Communication with Destination Host is Administratively Prohibited",
				m.get("msg"));
		assertEquals("ICMP", m.get("proto"));
		assertEquals("/220.45.142.139", m.get("src_ip").toString());
		assertNull(m.get("src_port"));
		assertEquals("/10.10.0.2", m.get("dst_ip").toString());
		assertNull(m.get("dst_port"));
		assertEquals("Misc activity", m.get("class"));
		assertEquals(3, m.get("priority"));
	}
}
