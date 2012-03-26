package org.krakenapps.syslog.parser.internal;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class KeyValueParserTest {
	@Test
	public void test() {
		String line = "xnkey=fdf17a03cc7e7d8233c03ae442f38941 code=34553434 xnid=0501510001 "
				+ "class=이벤트 subclass=인증 level=notice subject=admin result=성공 msg=\"관리자 admin 이(가) 보안장비 로그인에 성공하였습니다. (접속IP=192.168.0.149 접속프로그램=webbrowser) (LocalAuth)\"";

		Map<String, Object> m = KeyValueParser.parse(line);
		assertEquals("fdf17a03cc7e7d8233c03ae442f38941", m.get("xnkey"));
		assertEquals("34553434", m.get("code"));
		assertEquals("0501510001", m.get("xnid"));
		assertEquals("이벤트", m.get("class"));
		assertEquals("인증", m.get("subclass"));
		assertEquals("notice", m.get("level"));
		assertEquals("admin", m.get("subject"));
		assertEquals("성공", m.get("result"));
		assertEquals("관리자 admin 이(가) 보안장비 로그인에 성공하였습니다. (접속IP=192.168.0.149 접속프로그램=webbrowser) (LocalAuth)", m.get("msg"));
	}
}
