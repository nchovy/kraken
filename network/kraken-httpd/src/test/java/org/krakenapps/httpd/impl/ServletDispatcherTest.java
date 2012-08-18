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
package org.krakenapps.httpd.impl;

import javax.servlet.ServletRegistration;

import org.junit.Test;
import static org.junit.Assert.*;

public class ServletDispatcherTest {
	@Test
	public void testSubstringPath() {
		ServletDispatcher d = new ServletDispatcher();
		MockServlet s1 = new MockServlet(1);
		MockServlet s2 = new MockServlet(2);

		ServletRegistration reg1 = d.addServlet("s1", s1);
		reg1.addMapping("/log/*");

		ServletRegistration reg2 = d.addServlet("s2", s2);
		reg2.addMapping("//*");

		ServletMatchResult r = d.matches("/page/login_start.html");
		assertEquals(2, id(r));

		ServletMatchResult r2 = d.matches("/logger.html");
		assertEquals(2, id(r2));
	}

	@Test
	public void test() {
		ServletDispatcher d = new ServletDispatcher();

		MockServlet s1 = new MockServlet(1);
		MockServlet s2 = new MockServlet(2);
		MockServlet s3 = new MockServlet(3);
		MockServlet s4 = new MockServlet(4);

		ServletRegistration reg1 = d.addServlet("s1", s1);
		reg1.addMapping("/foo/bar/*");

		ServletRegistration reg2 = d.addServlet("s2", s2);
		reg2.addMapping("/baz/*");

		ServletRegistration reg3 = d.addServlet("s3", s3);
		reg3.addMapping("/catalog");

		ServletRegistration reg4 = d.addServlet("s4", s4);
		reg4.addMapping("*.bop");

		ServletMatchResult r = d.matches("/foo/bar/index.html");
		assertEquals(1, id(r));

		r = d.matches("/foo/bar/index.bop");
		assertEquals(1, id(r));

		r = d.matches("/baz");
		assertEquals(2, id(r));

		r = d.matches("/baz/index.html");
		assertEquals(2, id(r));

		r = d.matches("/catalog");
		assertEquals(3, id(r));

		r = d.matches("/catalog/index.html");
		assertNull(r);

		r = d.matches("/catalog/racecar.bop");
		assertEquals(4, id(r));

		r = d.matches("/index.bop");
		assertEquals(4, id(r));
	}

	@Test
	public void testPathInfo() {
		ServletDispatcher d = new ServletDispatcher();
		MockServlet s1 = new MockServlet(1);
		ServletRegistration reg1 = d.addServlet("s1", s1);
		reg1.addMapping("/foo/*");

		ServletMatchResult r = d.matches("/foo/bar?login_name=test");
		assertNotNull(r);
		assertEquals("/foo", r.getServletPath());
		assertEquals("/bar", r.getPathInfo());

		r = d.matches("/foo/bar");
		assertNotNull(r);
		assertEquals("/foo", r.getServletPath());
		assertEquals("/bar", r.getPathInfo());
	}

	private int id(ServletMatchResult r) {
		return ((MockServlet) r.getServlet()).getId();
	}
}
