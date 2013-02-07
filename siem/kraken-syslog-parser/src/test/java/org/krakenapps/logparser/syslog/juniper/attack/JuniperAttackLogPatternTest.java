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
package org.krakenapps.logparser.syslog.juniper.attack;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;
import org.krakenapps.logparser.syslog.juniper.attack.JuniperAttackLogPattern;

public class JuniperAttackLogPatternTest {

//	@Ignore
	@Test
	public void testParse() {
		String category = "Emergency (00005)";
		String patternString = "SYN flood! From <src-ip>:<src-port> to <dst-ip>:<dst-port>, proto TCP (zone <zone-name>, int <interface-name>). Occurred <none> times.";
		
		JuniperAttackLogPattern pattern = JuniperAttackLogPattern.from(category, patternString);
		
		String line = "SYN flood! From 1.1.1.1:1111 to 22.22.22.22:22222, proto TCP (zone zone #1, int test interface). Occurred 100 times.";
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(JuniperAttackLogPattern.SEVERITY_KEY, "Emergency");
		expected.put(JuniperAttackLogPattern.ID_KEY, "00005");
		expected.put(JuniperAttackLogPattern.RULE_KEY, "SYN flood");
		try {
			expected.put("src-ip", InetAddress.getByName("1.1.1.1"));
			expected.put("src-port", 1111);
			expected.put("dst-ip", InetAddress.getByName("22.22.22.22"));
			expected.put("dst-port", 22222);
			expected.put("zone-name", "zone #1");
			expected.put("interface-name", "test interface");
			expected.put("count", 100);
			expected.put("category", "intrusion");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		
		Map<String, Object> actual = pattern.parse(line);
		
//		for(String key: expected.keySet()) System.out.println(expected.get(key)+"\t"+actual.get(key));
		
		assertEquals(expected, actual);
	}

//	@Ignore
	@Test
	public void testGetConstElements() {

		String category = "Emergency (00005)";
		String patternString = "SYN flood! From <src-ip>:<src-port> to <dst-ip>:<dst-port>, proto TCP (zone <zone-name>, int <interface-name>). Occurred <none> times.";
		
		JuniperAttackLogPattern parser = JuniperAttackLogPattern.from(category, patternString);
		
		List<String> expected = Arrays.asList(
				"SYN flood! From ",
				":",
				" to ",
				":",
				", proto TCP (zone ",
				", int ",
				"). Occurred ",
				" times."
		);
		
		List<String> consts = parser.getConstElements();
		
		
		assertEquals(expected, consts);
	}

//	@Ignore
	@Test
	public void testFindBraket() {

		String patternString = "SYN flood! From <src-ip>:<src-port> to <dst-ip>:<dst-port>, proto TCP (zone <zone-name>, int <interface-name>). Occurred <none> times.";
		
		int[] expected = {"SYN flood! From ".length(), "SYN flood! From <src-ip>".length()};
		int[] actual = JuniperAttackLogPattern.findBraket(patternString, 0);
//		System.out.println(Arrays.toString(actual));
		assertArrayEquals(expected, actual);
		
	}

	@Test
	public void testParseWithNoPortNumber() {
		String category = "Emergency (00006)";
		String patternString = "Teardrop attack! From <src-ip>:<src-port> to <dst-ip>:<dst-port>, proto { TCP | UDP | <protocol> } (zone <zone-name>, int <interface-name>). Occurred <none> times.";

		JuniperAttackLogPattern pattern = JuniperAttackLogPattern.from(category, patternString);
		
//		System.out.println(pattern.getConstElements());
//		System.out.println(pattern.getVariables());
		
		String line = "Teardrop attack! From 11.11.11.11 to 222.222.222.222, proto Unknown (zone Untrust, int #1004). Occurred 100 times.";
		
		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put(JuniperAttackLogPattern.SEVERITY_KEY, "Emergency");
		expected.put(JuniperAttackLogPattern.ID_KEY, "00006");
		expected.put(JuniperAttackLogPattern.RULE_KEY, "Teardrop attack");
		try {
			expected.put("src-ip", InetAddress.getByName("11.11.11.11"));
			expected.put("dst-ip", InetAddress.getByName("222.222.222.222"));
			expected.put("protocol", "Unknown");
			expected.put("zone-name", "Untrust");
			expected.put("interface-name", "#1004");
			expected.put("count", 100);
			expected.put("category", "intrusion");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		
		
		Map<String, Object> actual = pattern.parse(line);
		assertNotNull(actual);
//		for(String key: actual.keySet()) System.out.println(expected.get(key)+"\t"+actual.get(key));
		
		assertEquals(expected, actual);
		
	}
}
