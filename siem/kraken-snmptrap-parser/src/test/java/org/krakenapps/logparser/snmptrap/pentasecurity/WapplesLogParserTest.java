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
package org.krakenapps.logparser.snmptrap.pentasecurity;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.krakenapps.logparser.snmptrap.pentasecurity.WapplesLogParser;

import static org.junit.Assert.*;

public class WapplesLogParserTest {
	@Test
	public void testDetectLog() throws UnknownHostException {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("1.3.6.1.6.3.1.1.4.1.0", "1.3.6.1.4.1.9772.1.2.0.1");
		m.put("1.3.6.1.4.1.9772.1.2.0.1.1", "12/3/27 10:40:22");
		m.put("1.3.6.1.4.1.9772.1.2.0.1.2", InetAddress.getByName("211.236.179.232"));
		m.put("1.3.6.1.4.1.9772.1.2.0.1.3", "/index.baks");
		m.put("1.3.6.1.4.1.9772.1.2.0.1.4", 27);
		m.put("1.3.6.1.4.1.9772.1.2.0.1.5",
				"GET /index.baks HTTP/1.1^M^JAccept: image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*^M^JAccept-Language: ko-KR^M^JUser-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.3; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; Tablet PC 2.0)");
		m.put("1.3.6.1.4.1.9772.1.2.0.1.6", 3);
		m.put("1.3.6.1.4.1.9772.1.2.0.1.7", "www.cyberone.kr(www.cyberone.kr)");
		m.put("1.3.6.1.4.1.9772.1.2.0.1.8", InetAddress.getByName("211.236.179.157"));

		Map<String, Object> m2 = new WapplesLogParser().parse(m);

		assertEquals(date(2012, 3, 27, 10, 40, 22), m2.get("date"));
		assertEquals("detect", m2.get("type"));
		assertEquals("211.236.179.232", m2.get("src"));
		assertEquals("211.236.179.157", m2.get("dst"));
		assertEquals("/index.baks", m2.get("uri"));
		assertEquals("extension_filtering", m2.get("rule"));
		assertEquals(
				"GET /index.baks HTTP/1.1^M^JAccept: image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*^M^JAccept-Language: ko-KR^M^JUser-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.3; Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1) ; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; Tablet PC 2.0)",
				m2.get("rawdata"));
		assertEquals("disconnect", m2.get("response"));
		assertEquals("www.cyberone.kr(www.cyberone.kr)", m2.get("hostname"));
	}

	private static Date date(int year, int mon, int day, int hour, int min, int sec) {
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
}
