package org.krakenapps.docxcod.test;

import java.io.FilterReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreeMarkerTest {

	private static Map<String, Object> parse(JSONObject obj) {
		Map<String, Object> m = new HashMap<String, Object>();
		String[] names = JSONObject.getNames(obj);
		if (names == null)
			return m;

		for (String key : names) {
			try {
				Object value = obj.get(key);
				if (value == JSONObject.NULL)
					value = null;
				else if (value instanceof JSONArray)
					value = parse((JSONArray) value);
				else if (value instanceof JSONObject)
					value = parse((JSONObject) value);

				m.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return m;
	}

	private static List<Object> parse(JSONArray arr) {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < arr.length(); i++) {
			try {
				Object o = arr.get(i);
				if (o == JSONObject.NULL)
					list.add(null);
				else if (o instanceof JSONArray)
					list.add(parse((JSONArray) o));
				else if (o instanceof JSONObject)
					list.add(parse((JSONObject) o));
				else
					list.add(o);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	@Test
	public void nestedListTest() throws Exception {
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(new DefaultObjectWrapper());

		InputStreamReader templateReader = null;
		InputStreamReader inputReader = null;
		try {
			inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
			JSONTokener tokener = new JSONTokener(inputReader);
			Map<String, Object> rootMap = parse((JSONObject) tokener.nextValue());

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

	@Test
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
			rootMap.put("title", "개발자들");
			rootMap.put("data", beings);

			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "준상");
				being.put("tel", "010-1111-2222");
			}
			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "상");
				being.put("tel", "010-3333-2222");
			}
			{
				HashMap<String, Object> being = new HashMap<String, Object>();
				beings.add(being);
				being.put("name", "준");
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
