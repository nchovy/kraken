package org.krakenapps.docxcod;

import static org.krakenapps.docxcod.util.XMLDocHelper.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TableDirectiveParser implements OOXMLProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	public void process(OOXMLPackage pkg) {
		extractMergeField(pkg);
		unwrapMagicNode(pkg);
	}

	private static Pattern MAGICNODE_PATTERN = Pattern.compile("<KMagicNode><![CDATA[+(.*)+]]></KMagicNode>");

	private static String parseMagicNode(String in) {
		in = replaceUnicodeQuote(in.trim());
		Matcher matcher = MAGICNODE_PATTERN.matcher(in);
		System.out.println(matcher.group(1));
		if (matcher.find() && matcher.groupCount() > 0) {
			String f = matcher.group(1);
			System.out.printf("group1 : %s\n", f);
			if (f == null)
				f = matcher.group(2);
			f = f.replaceAll("\\\\(.)", "$1");
			return f;
		} else
			return null;
	}

	private static String replaceUnicodeQuote(String in) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < in.length(); ++i) {
			int type = Character.getType(in.codePointAt(i));
			switch (type) {
			case Character.FINAL_QUOTE_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
				builder.append('"');
				break;
			default:
				builder.append(in.charAt(i));
				break;
			}
		}
		return builder.toString();
	}

	public static String transformMagicNode(String nodeValue) {
		String result = nodeValue.trim();
		if (result.startsWith("<KMagicNode>")) {
			result = parseMagicNode(result);
		}
		return result;
	}

	// ///
	public static Document stringToDom(InputStream input)
			throws SAXException, ParserConfigurationException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new InputSource(new InputStreamReader(input)));
	}

	// //

	public static final String UTF8_BOM = "\uFEFF";

	private void unwrapMagicNode(OOXMLPackage pkg) {
		fileRead(pkg);
		logger.debug("aa");

	}

	private void fileRead(OOXMLPackage pkg) {

		try {
			FileInputStream fis = new FileInputStream(new File(pkg.getDataDir(), "word/document.xml"));
			String xmlString = new Scanner(fis, "UTF-8").useDelimiter("\\A").next();

			xmlString = xmlString.replaceAll("<KMagicNode><!\\[CDATA\\[", "<");
			xmlString = xmlString.replaceAll("\\]\\]></KMagicNode>", ">");
			xmlString = xmlString.replaceAll("@before-row", "");
			xmlString = xmlString.replaceAll("@after-row", "");

			InputStream in = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			FileOutputStream fos = new FileOutputStream(new File(pkg.getDataDir(), "word/document.xml"));

			BufferedInputStream bis = new BufferedInputStream(in);
			BufferedOutputStream bos = new BufferedOutputStream(fos);

			int len = 0;
			byte[] buf = new byte[1024];
			while ((len = bis.read(buf, 0, 1024)) != -1) {
				bos.write(buf, 0, len);
			}

			bos.close();
			bis.close();
			fos.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/*
	 * private void fileWrite(OOXMLPackage pkg){ try{ BufferedWriter UniOutput =
	 * new BufferedWriter(new OutputStreamWriter(new
	 * FileOutputStream("out.txt"),"UTF-8"));
	 * 
	 * UniOutput.write(65279);
	 * 
	 * UniOutput.write("abcfgdfgdf"); UniOutput.newLine();
	 * UniOutput.write("abbbbbbb");
	 * 
	 * UniOutput.close();
	 * 
	 * } catch (UnsupportedEncodingException e){ System.err.println(e);
	 * System.exit(1); } catch (IOException e){ System.err.println(e);
	 * System.exit(1); } }
	 */

	private void extractMergeField(OOXMLPackage pkg) throws TransformerFactoryConfigurationError {
		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), "word/document.xml"));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);
			NodeList nodeList = evaluateXPath(xpath,
					"//w:tbl//*[name()='w:fldChar' or name()='w:instrText' or name()='w:fldSimple']", doc);

			XPathExpression xpFldSimpleText = xpath.compile("w:r/w:t");

			List<Directive> directives = DirectiveExtractor.parseNodeList(nodeList);
			for (Directive d : directives) {

				Node n = d.getPosition();
				String directive = d.getDirectiveString();
				logger.debug("{} {}", new Object[] { n.getNodeName(), directive });
				Node targetPara = null;
				Node parentOfPara = null;

				if (directive.charAt(0) == '@') {
					targetPara = n.getParentNode().getParentNode();
					parentOfPara = targetPara;

					do {
						targetPara = targetPara.getParentNode();
					} while (!targetPara.getNodeName().equals("w:tr"));

					parentOfPara = targetPara.getParentNode();

					if (directive.startsWith("@after-row")) {
						targetPara = targetPara.getNextSibling();
					}
					parentOfPara.insertBefore(getMagicNode(doc, directive), targetPara);
				} else {
					if (n.getNodeName().equals("w:fldSimple")) {
						logger.debug("fldSimple found");
						NodeList t = evaluateXPathExpr(xpFldSimpleText, n);

						t.item(0).setTextContent(directive);
					}
				}

			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer
					.transform(new DOMSource(doc), new StreamResult(new File(pkg.getDataDir(), "word/document.xml")));
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

	private Node getMagicNode(Document doc, String content) {
		Element magicNode = doc.createElement("KMagicNode");
		magicNode.appendChild(doc.createCDATASection(content));
		return magicNode;
	}
}
