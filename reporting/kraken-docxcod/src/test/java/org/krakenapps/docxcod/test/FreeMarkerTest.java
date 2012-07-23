package org.krakenapps.docxcod.test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;


public class FreeMarkerTest {
	@Test
	public void test1() throws Exception {
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		Template t = new Template("test", new StringReader("Hello ${user}"), cfg);
		StringWriter out = new StringWriter();
		
		HashMap<String, Object> rootMap = new HashMap<String, Object>();
		rootMap.put("user", "stania");
		t.process(rootMap, out);
		
		System.out.println(out.toString());
	}
}
