/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.logparser.syslog.juniper.session;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.logparser.syslog.juniper.session.JuniperSessionLogParser;

public class JuniperSessionLogParserTest {

	@Test
	public void testParseIcmp() {
		String line = "nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:14:10\" duration=0 policy_id=45 service=icmp proto=1 src zone=Untrust dst zone=Trust action=Deny sent=0 rcvd=0 src=58.72.190.250 dst=210.99.53.197 icmp type=8 session_id=0";

		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		
		String[][] expectedKeyValues = {
				{"device_id", "0133012007000002"},
				{"category", "traffic"},
				{"start_time","2009-01-22 15:14:10"},
				{"duration","0"},
				{"policy_id","45"},
				{"service","icmp"},
				{"proto","1"},
				{"src zone","Untrust"},
				{"dst zone","Trust"},
				{"action","Deny"},
				{"sent","0"},
				{"rcvd","0"},
				{"src","58.72.190.250"},
				{"dst","210.99.53.197"},
				{"icmp type","8"},
				{"session_id","0"}
		};
		
		for(String[] keyValue: expectedKeyValues) {
			expected.put(keyValue[0], keyValue[1]);
		}
		
		assertEquals(expected, map);
	}

	@Test
	public void testParseIcmp2() {
		String line = "<133>nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:14:12\" duration=0 policy_id=45 service=icmp proto=1 src zone=Untrust dst zone=Trust action=Deny sent=0 rcvd=0 src=164.92.250.21 dst=210.99.49.40 icmp type=8 session_id=0";

		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		String[][] expectedKeyValues = {
				{"device_id", "0133012007000002"},
				{"category", "traffic"},
				{"start_time","2009-01-22 15:14:12"},
				{"duration","0"},
				{"policy_id","45"},
				{"service","icmp"},
				{"proto","1"},
				{"src zone","Untrust"},
				{"dst zone","Trust"},
				{"action","Deny"},
				{"sent","0"},
				{"rcvd","0"},
				{"src","164.92.250.21"},
				{"dst","210.99.49.40"},
				{"icmp type","8"},
				{"session_id","0"}
		};

		for(String[] keyValue: expectedKeyValues) {
			expected.put(keyValue[0], keyValue[1]);
		}
		
		assertEquals(expected, map);
	}
	

	@Test
	public void testParseDeny() {
		
		String line = "<133>nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:14:10\" duration=0 policy_id=80 service=http proto=6 src zone=Untrust dst zone=Trust action=Deny sent=0 rcvd=0 src=66.249.71.148 dst=210.99.49.2 src_port=56624 dst_port=80 session_id=0";

		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		String[][] expectedKeyValues = {
				{"device_id", "0133012007000002"},
				{"category", "traffic"},
				{"start_time","2009-01-22 15:14:10"},
				{"duration","0"},
				{"policy_id","80"},
				{"service","http"},
				{"proto","6"},
				{"src zone","Untrust"},
				{"dst zone","Trust"},
				{"action","Deny"},
				{"sent","0"},
				{"rcvd","0"},
				{"src","66.249.71.148"},
				{"dst","210.99.49.2"},
				{"src_port","56624"},
				{"dst_port","80"},
				{"session_id","0"}
		};

		for(String[] keyValue: expectedKeyValues) {
			expected.put(keyValue[0], keyValue[1]);
		}
		
		assertEquals(expected, map);
	}
	
	@Test
	public void testParsePermit() {
		
		String line = "<133>nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:13:46\" duration=25 policy_id=11 service=http proto=6 src zone=Untrust dst zone=Trust action=Permit sent=178 rcvd=84 src=115.89.244.226 dst=210.99.50.44 src_port=43826 dst_port=80 src-xlated ip=115.89.244.226 port=43826 dst-xlated ip=210.99.50.44 port=80 session_id=245093 reason=Close - TCP FIN";

		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		String[][] expectedKeyValues = {
				{"device_id", "0133012007000002"},
				{"category", "traffic"},
				{"start_time","2009-01-22 15:13:46"},
				{"duration","25"},
				{"policy_id","11"},
				{"service","http"},
				{"proto","6"},
				{"src zone","Untrust"},
				{"dst zone","Trust"},
				{"action","Permit"},
				{"sent","178"},
				{"rcvd","84"},
				{"src","115.89.244.226"},
				{"dst","210.99.50.44"},
				{"src_port","43826"},
				{"dst_port","80"},
				{"src-xlated ip","115.89.244.226"},
				{"src-xlated port","43826"},
				{"dst-xlated ip","210.99.50.44"},
				{"dst-xlated port","80"},
				{"session_id","245093"},
				{"reason","Close - TCP FIN"}
		};

		for(String[] keyValue: expectedKeyValues) {
			expected.put(keyValue[0], keyValue[1]);
		}
		
		assertEquals(expected, map);
	}
	
	@Test
	public void testParseUnknownService() {
		
		String line = "<133>nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:13:58\" duration=13 policy_id=10 service=tcp/port:9007 proto=6 src zone=Untrust dst zone=Trust action=Permit sent=1745 rcvd=2100 src=210.103.83.39 dst=210.99.48.136 src_port=53229 dst_port=9007 src-xlated ip=210.103.83.39 port=53229 dst-xlated ip=210.99.48.136 port=9007 session_id=253584 reason=Close - TCP FIN";
		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		Map<String, Object> expected = new HashMap<String, Object>();
		String[][] expectedKeyValues = {
				{"device_id", "0133012007000002"},
				{"category", "traffic"},
				{"start_time","2009-01-22 15:13:58"},
				{"duration","13"},
				{"policy_id","10"},
				{"service","tcp/port:9007"},
				{"proto","6"},
				{"src zone","Untrust"},
				{"dst zone","Trust"},
				{"action","Permit"},
				{"sent","1745"},
				{"rcvd","2100"},
				{"src","210.103.83.39"},
				{"dst","210.99.48.136"},
				{"src_port","53229"},
				{"dst_port","9007"},
				{"src-xlated ip","210.103.83.39"},
				{"src-xlated port","53229"},
				{"dst-xlated ip","210.99.48.136"},
				{"dst-xlated port","9007"},
				{"session_id","253584"},
				{"reason","Close - TCP FIN"}
		};

		for(String[] keyValue: expectedKeyValues) {
			expected.put(keyValue[0], keyValue[1]);
		}
		
		assertEquals(expected, map);
	}
	
	@Test
	public void testParseSpaceContainingValue() {
		String line = "<133>nsisg1000: NetScreen device_id=0133012007000002  [Root]system-notification-00257(traffic): start_time=\"2009-01-22 15:13:52\" duration=17 policy_id=32 service=smtp (tcp) proto=6 src zone=Untrust dst zone=Trust action=Permit sent=4023 rcvd=684 src=211.55.23.188 dst=210.99.48.69 src_port=2736 dst_port=25 src-xlated ip=211.55.23.188 port=2736 dst-xlated ip=210.99.48.69 port=25 session_id=248596 reason=Close - TCP FIN";
		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		assertEquals("smtp (tcp)", map.get("service"));
		
	}
	
	@Test
	public void testUnmatchedString() {
		String line = "SYN flood! From 1.1.1.1:1111 to 22.22.22.22:22222, proto TCP (zone zone #1, int test interface). Occurred 100 times.";		
		JuniperSessionLogParser parser = JuniperSessionLogParser.newInstance();
		Map<String, Object> map = parser.parse(line);
		
		assertNull(map);
	}
}
