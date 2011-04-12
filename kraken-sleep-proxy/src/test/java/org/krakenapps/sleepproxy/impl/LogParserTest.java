package org.krakenapps.sleepproxy.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

public class LogParserTest {
	@Test
	public void test() throws UnknownHostException {
		String log = "1,1,3f292340-afa3-43e3-9f6d-49a3b219aa5a,xeraph,MAYA,OFFICE,1,00:16:EA:B3:A8:0E,192.168.0.5,\"Intel(R) WiFi Link 5300 AGN\"";

		LogMessage m = LogParser.parse(log);
		assertEquals(1, m.getVersion());
		assertEquals(1, m.getMsgType());
		assertEquals("3f292340-afa3-43e3-9f6d-49a3b219aa5a", m.getGuid());
		assertEquals("xeraph", m.getUserName());
		assertEquals("MAYA", m.getHostName());
		assertEquals("OFFICE", m.getDomain());

		List<NicInfo> adapters = m.getNetworkAdapters();
		assertEquals(1, adapters.size());

		NicInfo nic = adapters.get(0);
		assertEquals("00:16:EA:B3:A8:0E", nic.getMac());
		assertEquals(InetAddress.getByName("192.168.0.5"), nic.getIp());
		assertEquals("Intel(R) WiFi Link 5300 AGN", nic.getDescription());
	}
}
