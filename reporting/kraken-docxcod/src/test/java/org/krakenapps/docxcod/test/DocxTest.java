package org.krakenapps.docxcod.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.krakenapps.docxcod.Docx;
import org.krakenapps.docxcod.util.ZipHelper;

public class DocxTest {
	private TearDownHelper tearDownHelper = new TearDownHelper();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		tearDownHelper.tearDown();
	}

	@Test
	public void saveTest() throws IOException {
		File targetDir = new File("saveTest");
		targetDir.mkdirs();
//		tearDownHelper.add(targetDir);

		Docx docx = new Docx();
		docx.load(getClass().getResourceAsStream("/nestedList2.docx"), targetDir);

		File saveFile = new File("saveTest.docx");
		docx.save(new FileOutputStream(saveFile));
		tearDownHelper.add(saveFile);
		
		// diff word file
	}

	@Test
	public void extractingTest() throws IOException {
		File targetDir = new File("extractingTest");
		targetDir.mkdirs();
		tearDownHelper.add(targetDir);

		Docx docx = new Docx();
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
