package org.krakenapps.logfile;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApacheWebLogParserTest {
	private Date parseDate(String s) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
		try {
			return dateFormat.parse(s);
		} catch (ParseException e) {
			return null;
		}
	}

	private InetAddress parseAddress(String s) {
		try {
			return InetAddress.getByName(s);
		} catch (UnknownHostException e) {
			return null;
		}
	}

	@Test
	public void testTimeAndMethod() {
		String line = "[29/Dec/2010:16:26:20 +0900] \"GET\"";
		ApacheWebLogParser l = new ApacheWebLogParser("%t \"%m\"");
		Map<String, Object> m = l.parse(line);

		assertEquals("GET", m.get("method"));
		assertEquals(parseDate("29/Dec/2010:16:26:20 +0900"), m.get("date"));
	}

	@Test
	public void testTextAndDescriptors() {
		String line = "ab  10.0.1.17 -";
		ApacheWebLogParser l = new ApacheWebLogParser("ab  %h %l");
		Map<String, Object> m = l.parse(line);

		assertEquals(parseAddress("10.0.1.17"), m.get("remote_host"));
		assertNull(m.get("login"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidTextAndDescriptors() {
		String line = "cb 10.0.1.17 -";
		ApacheWebLogParser l = new ApacheWebLogParser("ab %h %l");
		l.parse(line);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDescriptor() {
		// There is no %Z descriptor in specification
		new ApacheWebLogParser("%Z %h %l \"%m\"");
	}

	@Test
	public void testPrintableDelimiter() {
		String line = "	10.0.1.17|\"GET\" [29/Dec/2010:16:26:20 +0900]-10.0.1.17";
		ApacheWebLogParser l = new ApacheWebLogParser("	%h|\"%m\" %t-%a");
		Map<String, Object> m = l.parse(line);

		assertEquals(parseAddress("10.0.1.17"), m.get("remote_host"));
		assertEquals("GET", m.get("method"));
		assertEquals(parseDate("29/Dec/2010:16:26:20 +0900"), m.get("date"));
		assertEquals(parseAddress("10.0.1.17"), m.get("client_ip"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoDelimiter() {
		new ApacheWebLogParser("%h% %l");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidLogFormat() {
		new ApacheWebLogParser("%h%l");
	}

	@Test
	public void testAllDescriptors() {
		String logFormat = "%h %l %u %t \"%r\"" +
				" %s %b %a %A %B %C %D \"%f\"" +
				" %H \"%{SSL_CLIENT_DN}e\"" +
				" \"%{Accept-Encoding}i\"" +
				" %m \"%{error-notes}n\" \"%{User-Agent}o\" %P %p %q %T %u %U %v %V %X %I %O";
		String line = "127.0.0.1 - - [03/Jan/2011:18:59:04 +0900] \"GET /favicon.ico HTTP/1.1\"" +
				" 404 209 127.0.0.1 127.0.0.1 209 - 1000 \"C:/Program Files/Apache Software Foundation/Apache2.2/htdocs/favicon.ico\"" +
				" HTTP/1.1 \"-\" \"gzip,deflate,sdch\"" +
				" GET \"-\" \"-\" 2980 80  0 - /favicon.ico ryusei-PC.office.nchovy.net localhost + 355 424";

		ApacheWebLogParser l = new ApacheWebLogParser(logFormat);
		Map<String, Object> m = new HashMap<String, Object>();
		m = l.parse(line);

		assertEquals(parseAddress("127.0.0.1"), m.get("remote_host"));
		assertNull(m.get("login"));
		assertNull(m.get("user"));
		assertEquals(parseDate("03/Jan/2011:18:59:04 +0900"), m.get("date"));
		assertEquals("GET /favicon.ico HTTP/1.1", m.get("request"));
		assertEquals(404, m.get("status"));
		assertEquals(209, m.get("resp_bytes_clf"));
		assertEquals(parseAddress("127.0.0.1"), m.get("client_ip"));
		assertEquals(parseAddress("127.0.0.1"), m.get("server_ip"));
		assertEquals(209, m.get("resp_bytes"));
		assertNull(m.get("cookie"));
		assertEquals(1000, m.get("duration_msec"));
		assertEquals("C:/Program Files/Apache Software Foundation/Apache2.2/htdocs/favicon.ico", m.get("file"));
		assertEquals("HTTP/1.1", m.get("protocol"));
		assertNull(m.get("ssl_client_dn"));
		assertEquals("gzip,deflate,sdch", m.get("accept_encoding"));
		assertNull(m.get("user_agent"));
		assertEquals("GET", m.get("method"));
		assertNull(m.get("note"));
		assertNull(m.get("resp_header"));
		assertEquals(2980, m.get("pid"));
		assertEquals(80, m.get("server_port"));
		assertEquals(0, m.get("duration_sec"));
		assertNull(m.get("user"));
		assertEquals("/favicon.ico", m.get("url"));
		assertEquals("ryusei-PC.office.nchovy.net", m.get("canonical_name"));
		assertEquals("localhost", m.get("server_name"));
		assertEquals("+", m.get("connection"));
		assertEquals(355, m.get("rcvd"));
		assertEquals(424, m.get("sent"));
	}
}
