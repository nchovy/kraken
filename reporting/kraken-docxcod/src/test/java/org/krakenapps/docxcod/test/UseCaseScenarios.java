package org.krakenapps.docxcod.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.docxcod.ChartDataSource;
import org.krakenapps.docxcod.Config;
import org.krakenapps.docxcod.DataSource;
import org.krakenapps.docxcod.DocumentMerger;
import org.krakenapps.docxcod.DocumentSourceInformation;
import org.krakenapps.docxcod.FileDocumentSource;
import org.krakenapps.docxcod.RptOutput;
import org.krakenapps.docxcod.RptTemplateProcessor;
import org.krakenapps.docxcod.TableDataSource;
import org.krakenapps.docxcod.TextDataSource;
import org.krakenapps.docxcod.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
		String inputFile = "testScenario1_Input.docx";
		File outputFile = File.createTempFile("KrakenDocxcodTest_", "_Output.docx");
		outputFiles.add(outputFile);

		RptTemplateProcessor tmplProc = new RptTemplateProcessor(Config.defaultConfig);
		tmplProc.setDocumentSource(new FileDocumentSource(inputFile));
		RptOutput output = tmplProc.generateOutput();

		if (output != null) {
			Utils.saveReport(output, outputFile);
		}

		assertTrue(outputFile.exists());
	}

	//	@Test
	public void testScenario2() throws Exception {
		String fSection1Template = "testScenario2_Input.docx";

		HashMap<String, DataSource> varMap = new HashMap<String, DataSource>();
		varMap.put("key1", new TextDataSource("value1"));
		varMap.put("key2", new TextDataSource("value2"));

		RptTemplateProcessor tmplProc = new RptTemplateProcessor(Config.defaultConfig);
		tmplProc.mergeDataSource(varMap);
		tmplProc.setDocumentSource(new FileDocumentSource(fSection1Template));
		RptOutput output = tmplProc.generateOutput();

		if (output != null)
			Utils.saveReport(output, new File("testScenario2_Output.docx"));
	}

	//	@Test
	public void testScenarioOfSomeDay() throws Exception {
		String fSection1Template = "section1_template.docx";

		DocumentSourceInformation docInfo = new DocumentSourceInformation(new FileDocumentSource(fSection1Template));
		docInfo.load();
		ArrayList<TextDataSource.Reference> refs = docInfo.getTextReferences();
		ArrayList<TableDataSource.Reference> tblRefs = docInfo.getTableReferences();
		ArrayList<ChartDataSource.Reference> chartRefs = docInfo.getChartReferences();

		RptTemplateProcessor tmplProc = new RptTemplateProcessor(Config.defaultConfig);
		tmplProc.mergeDataSource(new HashMap<String, DataSource>());
		tmplProc.addDataSource("table1", new TableDataSource());
		tmplProc.addDataSource("table2", new TableDataSource());
		tmplProc.setDocumentSource(new FileDocumentSource(fSection1Template));
		RptOutput output = tmplProc.generateOutput();

		DocumentMerger merger = new DocumentMerger();
		merger.addDocumentSource(new FileDocumentSource("cover.docx"));
		merger.addDocumentSource(output.createDocumentSource());
		merger.addDocumentSource(output.createDocumentSource());
		RptOutput mergedOutput = merger.generateOutput();

		Utils.saveReport(mergedOutput, new File("test.docx"));
	}

}
