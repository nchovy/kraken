package org.krakenapps.docxcod.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.docxcod.ChartDirectiveParser;
import org.krakenapps.docxcod.FreeMarkerRunner;
import org.krakenapps.docxcod.JsonHelper;
import org.krakenapps.docxcod.OOXMLPackage;
import org.krakenapps.docxcod.OOXMLProcessor;
import org.krakenapps.docxcod.MergeFieldParser;

public class UseCaseScenarios {

	private TearDownHelper tearDownHelper = new TearDownHelper();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		tearDownHelper.tearDown();
	}

	@Test
	public void testScenario1() throws Exception {
		File targetDir = new File("mainTest");
		targetDir.mkdirs();
//		tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/nestedList2.docx"), targetDir);

		InputStreamReader inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
		JSONTokener tokener = new JSONTokener(inputReader);
		Map<String, Object> rootMap = JsonHelper.parse((JSONObject) tokener.nextValue());

		List<OOXMLProcessor> processors = new ArrayList<OOXMLProcessor>();
		processors.add(new MergeFieldParser());
		processors.add(new ChartDirectiveParser());
		processors.add(new FreeMarkerRunner(rootMap));

		for (OOXMLProcessor processor : processors) {
			processor.process(docx, rootMap);
		}

		File saveFile = new File("mainTest-save.docx");
		docx.save(new FileOutputStream(saveFile));
		//tearDownHelper.add(saveFile);
		assertTrue(saveFile.exists());
		
	}
}
