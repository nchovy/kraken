package org.krakenapps.logfile;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class OpenSshLogParserTest {

	@Test
	public void testResultAccepted() {
		OpenSshLogParser o = new OpenSshLogParser();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("line", "Jan  3 22:09:09 navi sshd[8016]"
				+ ": Accepted password for xeraph from 112.153.155.76 port 20766 ssh2");

		Map<String, Object> m = new HashMap<String, Object>();
		m = o.parse(args);

		assertEquals("navi", m.get("host"));
		assertEquals("sshd[8016]", m.get("logger"));
		assertEquals("login", m.get("type"));
		assertEquals("success", m.get("result"));
		assertEquals("xeraph", m.get("account"));
		assertEquals("112.153.155.76", m.get("src_ip"));
		assertEquals("20766", m.get("src_port"));
		assertEquals("ssh2", m.get("protocol"));
	}

	@Test
	public void testResultFailed() {
		OpenSshLogParser o = new OpenSshLogParser();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("line", "Jan  2 06:59:08 navi sshd[26125]"
				+ ": Failed password for root from 122.227.22.52 port 52627 ssh2");

		Map<String, Object> m = new HashMap<String, Object>();
		m = o.parse(args);

		assertEquals("navi", m.get("host"));
		assertEquals("sshd[26125]", m.get("logger"));
		assertEquals("login", m.get("type"));
		assertEquals("failure", m.get("result"));
		assertEquals("root", m.get("account"));
		assertEquals("122.227.22.52", m.get("src_ip"));
		assertEquals("52627", m.get("src_port"));
		assertEquals("ssh2", m.get("protocol"));

	}

	@Test
	public void testResultClosed() {
		OpenSshLogParser o = new OpenSshLogParser();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("line", "Jan  3 22:09:10 navi sshd[8016]"
				+ ": pam_unix(sshd:session): session closed for user xeraph");

		Map<String, Object> m = new HashMap<String, Object>();
		m = o.parse(args);

		assertEquals("navi", m.get("host"));
		assertEquals("sshd[8016]", m.get("logger"));
		assertEquals("login", m.get("type"));
		assertEquals("session", m.get("category"));
		assertEquals("closed", m.get("result"));
		assertEquals("xeraph", m.get("account"));

		assertNull(m.get("src_ip"));
		assertNull(m.get("src_port"));
		assertNull(m.get("protocol"));
		assertNull(m.get("uid"));

	}

	@Test
	public void testResultOpened() {
		OpenSshLogParser o = new OpenSshLogParser();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("line", "Jan  3 22:09:10 navi sshd[8016]"
				+ ": pam_unix(sshd:session): session opened for user xeraph by (uid=0)");

		Map<String, Object> m = new HashMap<String, Object>();
		m = o.parse(args);

		assertEquals("navi", m.get("host"));
		assertEquals("sshd[8016]", m.get("logger"));
		assertEquals("login", m.get("type"));
		assertEquals("session", m.get("category"));
		assertEquals("opened", m.get("result"));
		assertEquals("xeraph", m.get("account"));
		assertEquals(0, m.get("uid"));

		assertNull(m.get("src_ip"));
		assertNull(m.get("src_port"));
		assertNull(m.get("protocol"));

	}

	@Test
	public void testTypeUnknown() {
		OpenSshLogParser o = new OpenSshLogParser();

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("line", "Jan  2 07:01:16 navi sshd[26322]" + ": Received disconnect from 122.227.22.52: 11: Bye Bye");

		o.parse(args); // return null
	}
}
