package org.krakenapps.xmlrpc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class PrettyXmlRpcCallParserTest {
	@Test
	public void parseTest() throws IOException {
		File f = new File("src/test/resources/pretty-xmlrpc-call.xml");

		FileInputStream is = new FileInputStream(f);
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;

			sb.append(line + "\n");
		}

		String call = sb.toString();

		XmlRpcMessage req = XmlRpcMethodCallParser.parse(XmlUtil.parse(call));
		assertEquals("device.generateTokens", req.getMethodName());
		assertEquals(1, req.getParameters().length);

		@SuppressWarnings("unchecked")
		Map<String, Object> m = (Map<String, Object>) req.getParameters()[0];
		assertTrue(m.containsKey("apikey"));
		assertTrue(m.containsKey("login_name"));
		assertTrue(m.containsKey("count"));
		assertTrue(m.containsKey("expiry"));
	}
}
