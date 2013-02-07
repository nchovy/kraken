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
package org.krakenapps.logparser.syslog.airtight;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SpectraGuardLogParserTest {

	@Test
	public void testRogueClientLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: Rogue Client [48:60:BC:42:D5:AC] is active. : 21.78.122.241://Locations/QooNet/BonoTower_2F : 2012-08-05T23:56:50+00:00 : High : 1987198 : 5 : 66 : 780";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Rogue Client", m.get("type"));
		assertEquals("48:60:BC:42:D5:AC", m.get("client"));
		assertEquals("//Locations/QooNet/BonoTower_2F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testRogueApLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Stop: Rogue AP [EFM_28:B3:B2] is active. : 21.78.122.241://Locations/Foo/10F : 2012-08-05T23:57:06+00:00 : High : 1987123 : 5 : 59 : 779";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Stop", m.get("state"));
		assertEquals("Rogue AP", m.get("type"));
		assertEquals("EFM_28:B3:B2", m.get("ap"));
		assertEquals("//Locations/Foo/10F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testRfSigAnomalyLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: RF signature anomaly detected for Client [Kim_GalaxyS2] : 21.78.122.241://Locations/Foo/12F : 2012-08-08T04:45:25+00:00 : High : 2005417 : 5 : 65 : 502";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("MAC Spoofing", m.get("type"));
		assertEquals("Kim_GalaxyS2", m.get("client"));
		assertEquals("//Locations/Foo/12F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testDeauthFloodLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Stop: Deauthentication flood attack is in progress against Authorized AP [00:08:9F:09:39:C0] and Client [Nam_iPhone 3GS]. : 21.78.122.241://Locations/Foo/12F : 2012-08-06T00:16:35+00:00 : High : 1987420 : 5 : 52 : 255";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Stop", m.get("state"));
		assertEquals("DoS", m.get("type"));
		assertEquals("00:08:9F:09:39:C0", m.get("ap"));
		assertEquals("Nam_iPhone 3GS", m.get("client"));
		assertEquals("//Locations/Foo/12F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testAdhocNetworkLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: An Ad hoc network [hpsetup] involving one or more Authorized Clients is active. : 21.78.122.241://Locations/QooNet/BarTower_3F : 2012-08-08T04:39:47+00:00 : High : 2005377 : 5 : 61 : 791";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Ad Hoc", m.get("type"));
		assertEquals("hpsetup", m.get("adhoc"));
		assertEquals("//Locations/QooNet/BarTower_3F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testFakeApLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Use of Fake AP tool detected near Sensor [A3-QooNet-02[BonoTower2F]] : 21.78.122.241://Locations/QooNet/BonoTower_2F : 2012-08-05T23:56:50+00:00 : High : 1987200 : 5 : 52 : 299";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Rogue AP", m.get("type"));
		assertEquals("A3-QooNet-02[BonoTower2F", m.get("sensor_name"));
		assertEquals("//Locations/QooNet/BonoTower_2F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testIndeterminateApLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: Indeterminate AP [EFM_86:21:88] is active. : 21.78.122.241://Locations/QooNet/BarTower_3F : 2012-08-05T23:56:57+00:00 : Medium : 1987201 : 5 : 59 : 281";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Rogue AP", m.get("type"));
		assertEquals("EFM_86:21:88", m.get("ap"));
		assertEquals("//Locations/QooNet/BarTower_3F", m.get("location"));
		assertEquals("Medium", m.get("severity"));
	}

	@Test
	public void testUnauthorizedClientLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: Unauthorized Client [78:47:1D:C9:83:0F] is connected to Authorized AP. : 21.78.122.241://Locations/HQ : 2012-08-05T23:57:04+00:00 : High : 1987203 : 5 : 66 : 796";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Misbehaving Client", m.get("type"));
		assertEquals("78:47:1D:C9:83:0F", m.get("client"));
		assertEquals("//Locations/HQ", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testAuthorizedApLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Stop: Authorized AP [QooNet1(5G)] is operating on non-allowed channel. : 21.78.122.241://Locations/QooNet/BarTower_3F : 2012-08-05T23:57:21+00:00 : Low : 1987173 : 5 : 51 : 515";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Stop", m.get("state"));
		assertEquals("Misconfigured AP", m.get("type"));
		assertEquals("QooNet1(5G)", m.get("ap"));
		assertEquals("//Locations/QooNet/BarTower_3F", m.get("location"));
		assertEquals("Low", m.get("severity"));
	}

	@Test
	public void testAuthorizedClientLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: Authorized Client [LimSoYeon] is connected to a non-authorized AP. : 21.78.122.241://Locations/HQ : 2012-08-06T00:22:10+00:00 : High : 1987468 : 5 : 66 : 799";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Misbehaving Client", m.get("type"));
		assertEquals("LimSoYeon", m.get("client"));
		assertEquals("//Locations/HQ", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testNetstumblerLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Possible use of Netstumbler detected near Sensor [A3-QooNet-01[BonoTower6F]] from Client [10:0B:A9:7A:6B:F0] : 21.78.122.241://Locations/QooNet/BonoTower_6F : 2012-08-05T23:58:26+00:00 : Medium : 1987228 : 5 : 53 : 268";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Scanning", m.get("type"));
		assertEquals("A3-QooNet-01[BonoTower6F", m.get("sensor_name"));
		assertEquals("10:0B:A9:7A:6B:F0", m.get("client"));
		assertEquals("//Locations/QooNet/BonoTower_6F", m.get("location"));
		assertEquals("Medium", m.get("severity"));
	}

	@Test
	public void testApQuarantineLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Start: AP [EFM_AC:34:42] needs to be quarantined. : 21.78.122.241://Locations/Foo/12F : 2012-08-06T00:02:03+00:00 : High : 1987269 : 5 : 69 : 831";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Start", m.get("state"));
		assertEquals("Prevention", m.get("type"));
		assertEquals("EFM_AC:34:42", m.get("ap"));
		assertEquals("//Locations/Foo/12F", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	@Test
	public void testClientQuarantineLog() {
		String log = "<00:25:90:0A:0C:FC>SpectraGuard Enterprise v6.2 : Stop: Client [Lee_iPhone4S] needs to be quarantined. : 21.78.122.241://Locations/Foo : 2012-08-09T07:58:51+00:00 : High : 2014177 : 5 : 69 : 834";
		SpectraGuardLogParser p = new SpectraGuardLogParser();
		Map<String, Object> m = p.parse(line(log));

		assertEquals("00:25:90:0A:0C:FC", m.get("sensor_mac"));
		assertEquals("SpectraGuard Enterprise v6.2", m.get("sensor_version"));
		assertEquals("21.78.122.241", m.get("sensor_ip"));
		assertEquals("Stop", m.get("state"));
		assertEquals("Prevention", m.get("type"));
		assertEquals("Lee_iPhone4S", m.get("client"));
		assertEquals("//Locations/Foo", m.get("location"));
		assertEquals("High", m.get("severity"));
	}

	private Map<String, Object> line(String log) {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("line", log);
		return m;
	}
}
