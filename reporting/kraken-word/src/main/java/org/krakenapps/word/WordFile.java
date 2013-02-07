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
package org.krakenapps.word;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.krakenapps.word.model.Document;

public class WordFile {
	private File f;
	private Document doc;

	public WordFile(File f) {
		this.f = f;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public void write() throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(fos);
			add(zos, "/template/[Content_Types].xml");
			add(zos, "/template/_rels/.rels");

			addDir(zos, "docProps/");
			add(zos, "/template/docProps/app.xml");
			add(zos, "/template/docProps/core.xml");

			addDir(zos, "word/");
			String s = generateDocument();
			add(zos, "word/document.xml", new ByteArrayInputStream(s.getBytes("utf-8")));

			// add(zos, "/template/word/document.xml");
			add(zos, "/template/word/fontTable.xml");
			add(zos, "/template/word/settings.xml");
			add(zos, "/template/word/styles.xml");
			add(zos, "/template/word/stylesWithEffects.xml");
			add(zos, "/template/word/webSettings.xml");

			addDir(zos, "word/_rels/");
			add(zos, "/template/word/_rels/document.xml.rels");

			addDir(zos, "word/theme/");
			add(zos, "/template/word/theme/theme1.xml");
		} finally {
			if (zos != null) {
				zos.close();
			}

			if (fos != null)
				fos.close();
		}
	}

	private String generateDocument() {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = documentBuilderFactory.newDocumentBuilder();
			org.w3c.dom.Document d = builder.newDocument();
			d.appendChild(doc.toXml(d));
			return toXmlString(d);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void addDir(ZipOutputStream zos, String name) throws IOException {
		zos.putNextEntry(new ZipEntry(name));
	}

	private void add(ZipOutputStream os, String path) throws IOException {
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream(path);
			path = path.replaceFirst("/template/", "");
			add(os, path, is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				is.close();
		}
	}

	private void add(ZipOutputStream os, String name, InputStream is) throws IOException {
		byte[] b = new byte[8096];

		ZipEntry e = new ZipEntry(name);
		e.setTime(new Date().getTime());
		os.putNextEntry(e);

		while (true) {
			int bytes = is.read(b);
			if (bytes < 0)
				break;

			os.write(b, 0, bytes);
		}

		os.closeEntry();
	}

	private static String toXmlString(org.w3c.dom.Document document) {
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document.getLastChild()), result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return sw.toString();
	}
}
