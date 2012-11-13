package org.krakenapps.docxcod;

import static org.krakenapps.docxcod.util.CloseableHelper.safeClose;
import static org.krakenapps.docxcod.util.XMLDocHelper.evaluateXPath;
import static org.krakenapps.docxcod.util.XMLDocHelper.newDocumentBuilder;
import static org.krakenapps.docxcod.util.XMLDocHelper.newXPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.xml.xpath.XPath;

import org.krakenapps.docxcod.util.XMLDocHelper;
import org.krakenapps.docxcod.util.XMLDocHelper.NodeListWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AugmentedDirectiveProcessor implements OOXMLProcessor {
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	static String[] augmentedDirectives = {
			"@before-row",
			"@after-row",
			// "@before-paragraph",
			// "@after-paragraph"
	};

	@Override
	public void process(OOXMLPackage pkg, Map<String, Object> rootMap) {
		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), "word/document.xml"));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);
			NodeList nodeList = evaluateXPath(xpath, "//KMagicNode", doc);

			for (Node n : new NodeListWrapper(nodeList)) {
				String directive = n.getTextContent();
				if (directive.charAt(0) != '@')
					continue;

				String prefix = findPrefix(directive);
				if (prefix == null) {
					logger.warn("unsupported augmented directive({})", directive);
					continue;
				}

				Node runNode;
				Node parentOfPara = null;
				Node targetPara = runNode = n.getParentNode().getParentNode(); // maybe w:r

				if (!runNode.getNodeName().equals("w:r")) {
					logger.warn("runNode is not w:r({}, directive: {})", runNode.getNodeName(), directive);
				}
					

				// find table row element following parent nodes.
				if (prefix.contains("row"))
					do {
						targetPara = targetPara.getParentNode();
					} while (!targetPara.getNodeName().equals("w:tr"));
				else if (prefix.contains("paragraph"))
				{
					logger.debug("not supported yet");
					continue;
				}

				parentOfPara = targetPara.getParentNode();

				if (directive.startsWith("@after-row")) {
					targetPara = targetPara.getNextSibling();
				}

				// insert magic node
				parentOfPara.insertBefore(getMagicNode(doc, unwrapAugmentedDirective(directive)), targetPara);

				// remove annotated node
				runNode.getParentNode().removeChild(runNode);
			}
			
			XMLDocHelper.save(doc, new File(pkg.getDataDir(), "word/document.xml"), true);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(f);
		}
	}

	private String findPrefix(String directive) {
		for (String ad : augmentedDirectives) {
			if (directive.startsWith(ad)) {
				return ad;
			}
		}
		return null;
	}

	private String unwrapAugmentedDirective(String directive) {
		for (String ad : augmentedDirectives) {
			if (directive.startsWith(ad)) {
				return directive.substring(ad.length()).trim();
			}
		}
		return directive;
	}

	private Node getMagicNode(Document doc, String content) {
		Element magicNode = doc.createElement("KMagicNode");
		magicNode.appendChild(doc.createCDATASection(content));
		return magicNode;
	}

}
