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
package org.krakenapps.logparser.syslog.hp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class TippingPointSmsLogParserTest {

	@Test
	public void testParser() {
		String line = "7\t3\td2ee633d-514f-11e1-3f6d-e43760fdb01b\t00000001-0001-0001-0001-000000003886\t"
				+ "3886: HTTP: Cross Site Scripting in POST Request\t3886\ttcp\t119.205.194.173\t40635\t222.231.7.12\t80\t"
				+ "1\t3\t3\tInterpark_A\t17107965\t1335074090028\t";
		TippingPointSmsLogParser parser = new TippingPointSmsLogParser();
		Map<String, Object> m = parser.parse(line(line));

		assertEquals(7, m.get("action"));
		assertEquals(3, m.get("severity"));
		assertEquals("d2ee633d-514f-11e1-3f6d-e43760fdb01b", m.get("policy_uuid"));
		assertEquals("00000001-0001-0001-0001-000000003886", m.get("sig_uuid"));
		assertEquals("3886: HTTP: Cross Site Scripting in POST Request", m.get("sig_name"));
		assertEquals(3886, m.get("sig_no"));
		assertEquals("tcp", m.get("protocol"));
		assertEquals("119.205.194.173", m.get("src_ip"));
		assertEquals(40635, m.get("src_port"));
		assertEquals("222.231.7.12", m.get("dst_ip"));
		assertEquals(80, m.get("dst_port"));
		assertEquals(1, m.get("hit"));
		assertEquals(3, m.get("device_slot"));
		assertEquals(3, m.get("device_segment"));
		assertEquals("Interpark_A", m.get("device_name"));
		assertEquals("17107965", m.get("alarm_id"));

		Date time = (Date) m.get("datetime");
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		assertEquals(2012, c.get(Calendar.YEAR));
		assertEquals(3, c.get(Calendar.MONTH));
		assertEquals(22, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(14, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(54, c.get(Calendar.MINUTE));
		assertEquals(50, c.get(Calendar.SECOND));
	}

	@Test
	public void testParser2() {
		String line = "8\t1\tdc7fe1b9-514f-11e1-3f6d-e43760fdb01b\t00000001-0001-0001-0001-000000007112\t"
				+ "7112: IP: Fragment Expired\t7112\tip\t220.76.119.239\t0\t211.233.74.133\t0\t1\t3\t3\tInterpark_B\t100794367\t1335076462067\t";
		TippingPointSmsLogParser parser = new TippingPointSmsLogParser();
		Map<String, Object> m = parser.parse(line(line));

	}

	private Map<String, Object> line(String line) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", line);
		return m;
	}
}
