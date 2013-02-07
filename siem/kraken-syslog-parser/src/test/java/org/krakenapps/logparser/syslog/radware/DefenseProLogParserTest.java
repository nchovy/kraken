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
package org.krakenapps.logparser.syslog.radware;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefenseProLogParserTest {
	@Test
	public void testAccessLog() {
		String log = "DefensePro: 04-04-2012 22:07:04 WARNING 8 Access \"black_2012_02_13\" IP 0.0.0.0 0 0.0.0.0 0 1 Regular \"Black List\" occur 6 2 N/A 0 N/A low drop";
		Map<String, Object> m = new DefenseProLogParser().parse(line(log));
		assertEquals(date(2012, 4, 4, 22, 7, 4), m.get("date"));
		assertEquals("WARNING", m.get("priority"));
		assertEquals("8", m.get("attack_id"));
		assertEquals("Access", m.get("category"));
		assertEquals("black_2012_02_13", m.get("attack_name"));
		assertEquals("IP", m.get("protocol"));
		assertEquals("0.0.0.0", m.get("src"));
		assertEquals(0, m.get("src_port"));
		assertEquals("0.0.0.0", m.get("dst"));
		assertEquals(0, m.get("dst_port"));
		assertEquals(1, m.get("phy_port"));
		assertEquals("Regular", m.get("sig_type"));
		assertEquals("Black List", m.get("policy_name"));
		assertEquals("occur", m.get("attack_status"));
		assertEquals(6, m.get("attack_count"));
		assertEquals(2, m.get("bandwidth"));
		assertEquals("N/A", m.get("vlan"));
		assertEquals("low", m.get("criticity"));
		assertEquals("drop", m.get("action"));
	}

	@Test
	public void testIntrusionLog() {
		String log = "DefensePro: 04-04-2012 22:07:04 WARNING 300039 Intrusions \"Dfweb_post_30s\" TCP 59.86.235.202 54255 175.207.24.20 80 13 Regular \"rule17\" term 0 11 N/A 0 N/A low forward";
		Map<String, Object> m = new DefenseProLogParser().parse(line(log));

		assertEquals(date(2012, 4, 4, 22, 7, 4), m.get("date"));
		assertEquals("WARNING", m.get("priority"));
		assertEquals("300039", m.get("attack_id"));
		assertEquals("Intrusions", m.get("category"));
		assertEquals("Dfweb_post_30s", m.get("attack_name"));
		assertEquals("TCP", m.get("protocol"));
		assertEquals("59.86.235.202", m.get("src"));
		assertEquals(54255, m.get("src_port"));
		assertEquals("175.207.24.20", m.get("dst"));
		assertEquals(80, m.get("dst_port"));
		assertEquals(13, m.get("phy_port"));
		assertEquals("Regular", m.get("sig_type"));
		assertEquals("rule17", m.get("policy_name"));
		assertEquals("term", m.get("attack_status"));
		assertEquals(0, m.get("attack_count"));
		assertEquals(11, m.get("bandwidth"));
		assertEquals("N/A", m.get("vlan"));
		assertEquals("low", m.get("criticity"));
		assertEquals("forward", m.get("action"));
	}

	@Test
	public void testDosLog() {
		String log = "DefensePro: 04-04-2012 22:07:04 WARNING 450008 DoS \"CyWeb_conn_1s\" TCP 115.89.124.56 1463 175.207.23.64 80 13 Regular \"rule8-6\" start 1 0 N/A 0 N/A medium drop";
		Map<String, Object> m = new DefenseProLogParser().parse(line(log));

		assertEquals(date(2012, 4, 4, 22, 7, 4), m.get("date"));
		assertEquals("WARNING", m.get("priority"));
		assertEquals("450008", m.get("attack_id"));
		assertEquals("DoS", m.get("category"));
		assertEquals("CyWeb_conn_1s", m.get("attack_name"));
		assertEquals("TCP", m.get("protocol"));
		assertEquals("115.89.124.56", m.get("src"));
		assertEquals(1463, m.get("src_port"));
		assertEquals("175.207.23.64", m.get("dst"));
		assertEquals(80, m.get("dst_port"));
		assertEquals(13, m.get("phy_port"));
		assertEquals("Regular", m.get("sig_type"));
		assertEquals("rule8-6", m.get("policy_name"));
		assertEquals("start", m.get("attack_status"));
		assertEquals(1, m.get("attack_count"));
		assertEquals(0, m.get("bandwidth"));
		assertEquals("N/A", m.get("vlan"));
		assertEquals("medium", m.get("criticity"));
		assertEquals("drop", m.get("action"));
	}

	private Date date(int year, int mon, int day, int hour, int min, int sec) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, mon - 1);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, min);
		c.set(Calendar.SECOND, sec);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	private Map<String, Object> line(String s) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", s);
		return m;
	}
}
