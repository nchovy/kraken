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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.*;
import org.krakenapps.logparser.syslog.juniper.attack.JuniperAttackLogParser;
import org.krakenapps.logparser.syslog.juniper.attack.JuniperAttackLogPattern;

public class JuniperAttackLogParserTest {

	JuniperAttackLogParser parser;
	
	@Before
	public void setUp() throws FileNotFoundException, IOException {
		File formatFile = new File("src/main/resources/org/krakenapps/logparser/syslog/juniper/attack/attack_log_format.txt");
		parser = JuniperAttackLogParser.newInstance(new FileReader(formatFile));
	}
	
//	@Ignore
	@Test
	public void testParse() {
		
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
		
		
		Map<String, Object> actual = parser.parse(line);
		
//		for(String key: expected.keySet()) System.out.println(expected.get(key)+"\t"+actual.get(key));
		
		assertEquals(expected, actual);
	}

	@Test
	public void testParse2() {

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
		
		Map<String, Object> actual = parser.parse(line);
		assertNotNull(actual);
//		for(String key: actual.keySet()) System.out.println(expected.get(key)+"\t"+actual.get(key));
		
		assertEquals(expected, actual);
		
	}

	@Test
	public void testGetPatternMap() {
		
		Set<String> expected = new HashSet<String>(Arrays.asList(
				"ActiveX control blocked! From ",
				"Address sweep! From ",
				"Bad IP option! From ", 
				"Dst IP session limit! From ", 
				"EXE file blocked! From ",
				"FIN but no ACK bit! From ",
				"Fragmented traffic! From ",
				"ICMP flood! From ",
				"ICMP fragment! From ",
				"IP spoofing! From ",
				"Java applet blocked! From ",
				"Land attack! From ",
				"Large ICMP packet! From ",
				"Malicious URL! From ",
				"No TCP flag! From ",
				"Ping of Death! From ",
				"Port scan! From ",
				"SYN and FIN bits! From ",
				"SYN flood! From ", 
				"SYN fragment! From ", 
				"SYN-ACK-ACK Proxy DoS! From ", 
				"Source Route IP option! From ",
				"Src IP session limit! From ", 
				"Teardrop attack! From ", 
				"UDP flood! From ", 
				"Unknown protocol! From ", 
				"WinNuke attack! From ", 
				"ZIP file blocked! From "
		));
		
//		for(String s: parser.getPatternKeySet()) System.out.println(s);
		
		assertEquals(expected, parser.getPatternKeySet());
	}
}
