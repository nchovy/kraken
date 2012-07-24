package org.krakenapps.docxcod.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.docxcod.ChartDataReference;
import org.krakenapps.docxcod.Config;
import org.krakenapps.docxcod.DataReference;
import org.krakenapps.docxcod.DocumentMerger;
import org.krakenapps.docxcod.DocumentSourceInformation;
import org.krakenapps.docxcod.FileDocumentSource;
import org.krakenapps.docxcod.RptOutput;
import org.krakenapps.docxcod.RptTemplateProcessor;
import org.krakenapps.docxcod.TableDataReference;
import org.krakenapps.docxcod.TextDataReference;
import org.krakenapps.docxcod.Utils;

public class UseCaseScenarios {

	public ArrayList<File> outputFiles = new ArrayList<File>();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		for (File f : outputFiles) {
			try {
				f.delete();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	@Test
	public void testScenario1() throws Exception {
		File inputFile = new File("testScenario1_Input.docx");
		File outputFile = File.createTempFile("KrakenDocxcodTest_", "_Output.docx");
		outputFiles.add(outputFile);
		
		Config config = new Config();
		config.workingDir = new File("."); 


		RptTemplateProcessor tmplProc = new RptTemplateProcessor(config);
		tmplProc.setDocumentSource(new FileDocumentSource(inputFile));
		RptOutput output = tmplProc.generateOutput();

		if (output != null) {
			Utils.saveReport(output, outputFile);
		}

		assertTrue(outputFile.exists());
	}

	// @Test
	public void testScenario2() throws Exception {
		String fSection1Template = "testScenario2_Input.docx";

		HashMap<String, Object> varMap = new HashMap<String, Object>();
		varMap.put("key1", "value1");
		varMap.put("key2", "value2");

		RptTemplateProcessor tmplProc = new RptTemplateProcessor(Config.defaultConfig);
		tmplProc.setDataSource(varMap);
		tmplProc.setDocumentSource(new FileDocumentSource(new File(fSection1Template)));
		RptOutput output = tmplProc.generateOutput();

		if (output != null)
			Utils.saveReport(output, new File("testScenario2_Output.docx"));
	}

	// @Test
	public void testScenarioOfSomeDay() throws Exception {
		String fSection1Template = "section1_template.docx";

		DocumentSourceInformation docInfo = new DocumentSourceInformation(new FileDocumentSource(new File(fSection1Template)));
		docInfo.load();
		ArrayList<TextDataReference> refs = docInfo.getTextReferences();
		ArrayList<TableDataReference> tblRefs = docInfo.getTableReferences();
		ArrayList<ChartDataReference> chartRefs = docInfo.getChartReferences();

		DocumentMerger merger = new DocumentMerger();
		merger.addDocumentSource(new FileDocumentSource(new File("cover.docx")));
		merger.addDocumentSource(new FileDocumentSource(new File(fSection1Template)));
		merger.addDocumentSource(new FileDocumentSource(new File(fSection1Template)));
		RptOutput mergedTmpl = merger.generateOutput();

		RptTemplateProcessor tmplProc = new RptTemplateProcessor(Config.defaultConfig);
		HashMap<String, Object> rootObj = new HashMap<String, Object>();
		tmplProc.setDataSource(rootObj);
		tmplProc.setDocumentSource(mergedTmpl.createDocumentSource());
		RptOutput output = tmplProc.generateOutput();


		Utils.saveReport(mergedTmpl, new File("test.docx"));
	}
	
	

}
