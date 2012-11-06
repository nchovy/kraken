package org.krakenapps.docxcod.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.docxcod.AugmentedDirectiveProcessor;
import org.krakenapps.docxcod.ChartDirectiveParser;
import org.krakenapps.docxcod.Directive;
import org.krakenapps.docxcod.DirectiveExtractor;
import org.krakenapps.docxcod.FreeMarkerRunner;
import org.krakenapps.docxcod.JsonHelper;
import org.krakenapps.docxcod.MagicNodeUnwrapper;
import org.krakenapps.docxcod.MergeFieldParser;
import org.krakenapps.docxcod.OOXMLPackage;
import org.krakenapps.docxcod.OOXMLProcessor;
import org.krakenapps.docxcod.util.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class DocxTest {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	private TearDownHelper tearDownHelper = new TearDownHelper();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		tearDownHelper.tearDown();
	}

	// @Test
	public void fieldTest() throws IOException {
		File targetDir = new File("fieldTest");
		targetDir.mkdirs();
		// tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/fieldTest.docx"), targetDir);

		List<OOXMLProcessor> parsers = new ArrayList<OOXMLProcessor>();
		DirectiveExtractor directiveExtractor = new DirectiveExtractor();
		parsers.add(directiveExtractor);

		for (OOXMLProcessor parser : parsers) {
			parser.process(docx, null);
		}

		String[] expected = new String[] {
				"@before-row#list data as a asdfasdfasdsfd",
				"asdsfdadsfadsfsdfa",
				"aslasdfasdfasdfddkfj",
				"asdfasdfasdf",
				"@before-row #list .vars[\"stania\"] as a",
				"asdfadfasdf",
				"ahsdfl;aksjdflksdjf",
				"#list asdf as qwer"
		};

		int cnt = 0;
		for (Directive dir : directiveExtractor.getDirectives()) {
			@SuppressWarnings("unused")
			Node n = dir.getPosition();
			String dirStr = dir.getDirectiveString();
			logger.debug("extracted: " + dirStr);

			assertTrue(dirStr.equals(expected[cnt++]));
		}
	}

	@Test
	public void chartTest() throws IOException, JSONException {
		File targetDir = new File("_chartTest");
		targetDir.mkdirs();
		//tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/chartTest.docx"), targetDir);

		InputStreamReader inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
		JSONTokener tokener = new JSONTokener(inputReader);
		Map<String, Object> rootMap = JsonHelper.parse((JSONObject) tokener.nextValue());

		List<OOXMLProcessor> processors = new ArrayList<OOXMLProcessor>();
		processors.add(new MergeFieldParser());
		processors.add(new AugmentedDirectiveProcessor());
		processors.add(new ChartDirectiveParser());
		processors.add(new MagicNodeUnwrapper());
		processors.add(new FreeMarkerRunner(rootMap));

		for (OOXMLProcessor processor : processors) {
			processor.process(docx, rootMap);
		}

		File saveFile = new File("chartTest-save.docx");
		docx.save(new FileOutputStream(saveFile));
//		tearDownHelper.add(saveFile);
	}

	@Test
	public void mainTest() throws IOException, JSONException {
		File targetDir = new File("mainTest");
		targetDir.mkdirs();
		//tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/nestedList2.docx"), targetDir);

		InputStreamReader inputReader = new InputStreamReader(getClass().getResourceAsStream("/nestedListTest.in"));
		JSONTokener tokener = new JSONTokener(inputReader);
		Map<String, Object> rootMap = JsonHelper.parse((JSONObject) tokener.nextValue());

		List<OOXMLProcessor> processors = new ArrayList<OOXMLProcessor>();
		processors.add(new MergeFieldParser());
		processors.add(new AugmentedDirectiveProcessor());
		processors.add(new MagicNodeUnwrapper());
		processors.add(new FreeMarkerRunner(rootMap));

		for (OOXMLProcessor processor : processors) {
			processor.process(docx, rootMap);
		}

		File saveFile = new File("mainTest-save.docx");
		docx.save(new FileOutputStream(saveFile));
//		tearDownHelper.add(saveFile);
	}

	@Test
	public void saveTest() throws IOException {
		File targetDir = new File("saveTest");
		targetDir.mkdirs();
		tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/nestedList2.docx"), targetDir);

		File saveFile = new File("saveTest.docx");
		docx.save(new FileOutputStream(saveFile));
		tearDownHelper.add(saveFile);

		// diff word file
	}

	// @Test
	public void extractingTest() throws IOException {
		File targetDir = new File("extractingTest");
		targetDir.mkdirs();
		tearDownHelper.add(targetDir);

		OOXMLPackage docx = new OOXMLPackage();
		docx.load(getClass().getResourceAsStream("/nestedList2.docx"), targetDir);

		String[] relPaths = { "", "customXml", "customXml/item1.xml", "customXml/itemProps1.xml", "customXml/_rels",
				"customXml/_rels/item1.xml.rels", "docProps", "docProps/app.xml", "docProps/core.xml", "word",
				"word/charts", "word/charts/chart1.xml", "word/charts/_rels", "word/charts/_rels/chart1.xml.rels",
				"word/document.xml", "word/embeddings", "word/embeddings/Microsoft_Excel_____1.xlsx",
				"word/endnotes.xml", "word/fontTable.xml", "word/footnotes.xml", "word/settings.xml",
				"word/styles.xml", "word/stylesWithEffects.xml", "word/theme", "word/theme/theme1.xml",
				"word/webSettings.xml", "word/_rels", "word/_rels/document.xml.rels", "[Content_Types].xml", "_rels",
				"_rels/.rels", };
		ArrayList<File> extractedFiles = new ArrayList<File>();
		ZipHelper.getFilesRecursivelyIn(targetDir, extractedFiles);

		File[] files = new File[relPaths.length];
		for (int i = 0; i < relPaths.length; ++i) {
			files[i] = new File(targetDir, relPaths[i]);
		}
		Arrays.sort(files);
		Object[] extractedArray = extractedFiles.toArray();
		Arrays.sort(extractedArray);

		assertTrue(Arrays.equals(extractedArray, files));
	}

	@Test
	public void relsTest() {
	}
}
