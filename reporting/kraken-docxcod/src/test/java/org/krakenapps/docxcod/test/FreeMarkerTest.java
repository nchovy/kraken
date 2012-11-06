package org.krakenapps.docxcod.test;

import static org.junit.Assert.assertEquals;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;
import org.krakenapps.docxcod.JsonHelper;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class FreeMarkerTest {

	public class MakeNewChartFunction implements TemplateMethodModelEx {
		public int callCount = 0;
		
		@Override
		public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
			callCount ++;
			System.out.printf("makeNewChart called(%s, %s)\n", arguments.get(0), arguments.get(1));
			System.out.printf("%s\n", Environment.getCurrentEnvironment().getKnownVariableNames());
			System.out.printf("%s\n", Environment.getCurrentEnvironment().getVariable("u"));
			return "";
		}
	}

	@Test
	public void UserDefMethodTest() throws Exception {
		InputStreamReader templateReader = null;
		InputStreamReader inputReader = null;
		Scanner scanner = null;
		try {
			Configuration cfg = new Configuration();
			cfg.setObjectWrapper(new DefaultObjectWrapper());

			inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
			JSONTokener tokener = new JSONTokener(inputReader);
			Map<String, Object> rootMap = JsonHelper.parse((JSONObject) tokener.nextValue());
			
			MakeNewChartFunction makeNewChartFunction = new MakeNewChartFunction();
			rootMap.put("makeNewChart", makeNewChartFunction);
			
			templateReader = new InputStreamReader(getClass().getResourceAsStream("/userDefMethodTest.fpl"));
			Template t = new Template("UserDefMethodTest", templateReader, cfg);

			StringWriter out = new StringWriter();

			t.process(rootMap, out);

			scanner = new Scanner(getClass().getResourceAsStream("/userDefMethodTest.out"));
			String expectedOutput = scanner.useDelimiter("\\A").next();
			
			assertEquals(expectedOutput, out.toString());
			assertEquals(3, makeNewChartFunction.callCount);
	
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(inputReader);
			if (scanner != null)
				scanner.close();
		}
	}

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

	private void safeClose(Closeable templateReader) {
		if (templateReader == null)
			return;
		try {
			templateReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
