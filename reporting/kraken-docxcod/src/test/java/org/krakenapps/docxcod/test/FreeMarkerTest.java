package org.krakenapps.docxcod.test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.krakenapps.docxcod.JsonHelper;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreeMarkerTest {

	public void nestedListTest() throws Exception {
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		InputStreamReader templateReader = null;
		InputStreamReader inputReader = null;
		try {
			inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
			JSONTokener tokener = new JSONTokener(inputReader);
			Map<String, Object> rootMap = JsonHelper.parse((JSONObject) tokener.nextValue());

			templateReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.fpl"));
			Template t = new Template("test", templateReader, cfg);
			StringWriter out = new StringWriter();

			t.process(rootMap, out);

			System.out.println(out.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(templateReader);
			safeClose(inputReader);
		}
	}

	public void test1() throws Exception {
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		InputStreamReader templateReader = new InputStreamReader(getClass().getResourceAsStream("/test1.fpl"));
		// XXX: close templateReader using try..finally
		try {
			Template t = new Template("test", templateReader, cfg);
			StringWriter out = new StringWriter();

			Map<String, Object> rootMap = new HashMap<String, Object>();
			ArrayList<Object> beings = new ArrayList<Object>();
			rootMap.put("title", "Developers");
			rootMap.put("data", beings);

			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "junsang");
				being.put("tel", "010-1111-2222");
			}
			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "jun");
				being.put("tel", "010-3333-2222");
			}
			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "sang");
				being.put("tel", "010-4444-2222");
			}

			t.process(rootMap, out);
			System.out.print(out.toString());
		} finally {
			templateReader.close();
		}

	}
	
	private void safeClose(InputStreamReader templateReader) {
		if (templateReader == null)
			return;
		try {
			templateReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
