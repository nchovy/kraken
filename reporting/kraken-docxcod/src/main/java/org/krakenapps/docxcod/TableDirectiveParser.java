package org.krakenapps.docxcod;

import static org.junit.Assert.assertTrue;
import static org.krakenapps.docxcod.util.XMLDocHelper.evaluateXPath;
import static org.krakenapps.docxcod.util.XMLDocHelper.newDocumentBuilder;
import static org.krakenapps.docxcod.util.XMLDocHelper.newXPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;

import org.krakenapps.docxcod.util.XMLDocHelper.NodeListWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TableDirectiveParser implements OOXMLProcessor {
	public void process(OOXMLPackage pkg) {
		extractMergeField(pkg);
		unwrapMagicNode(pkg);
	}
	
	private void unwrapMagicNode(OOXMLPackage pkg) {
		// unwrap KMagicNode
	}

	private void extractMergeField(OOXMLPackage pkg) throws TransformerFactoryConfigurationError {
		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), "word/document.xml"));
			Document doc = newDocumentBuilder().parse(f);
			assertTrue(doc != null);

			XPath xpath = newXPath(doc);
			{
				NodeList nodeList = evaluateXPath(xpath, "//w:tbl//*[name()='w:fldChar' or name()='w:instrText' or name()='w:fldSimple']", doc);
				System.out.printf("instrText cnt: %d\n", nodeList.getLength());
				for (Node n : new NodeListWrapper(nodeList)) {
					if (n.getNodeName().equals("w:fldChar"))
						System.out.printf("%s %s\n", n.getNodeName(), n.getAttributes().getNamedItem("w:fldCharType").getNodeValue());
					else if (n.getNodeName().equals("w:instrText")) {
						System.out.printf("%s [%s]\n", n.getNodeName(), n.getTextContent());
					} else if (n.getNodeName().equals("w:fldSimple")) {
						System.out.printf("%s [%s]\n", n.getNodeName(), n.getAttributes().getNamedItem("w:instr").getNodeValue());
					} else {
						System.out.printf("%s\n", n.getNodeName());
					}
				}
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), new StreamResult(new File(pkg.getDataDir(), "word/document.xml")));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(f);
		}
	}

	private void safeClose(InputStream f) {
		if (f == null)
			return;
		try {
			f.close();
		} catch (Exception e) {
			// ignore
		}
	}



}
