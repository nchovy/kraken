package org.krakenapps.docxcod;

import static org.krakenapps.docxcod.util.XMLDocHelper.evaluateXPath;
import static org.krakenapps.docxcod.util.XMLDocHelper.newDocumentBuilder;
import static org.krakenapps.docxcod.util.XMLDocHelper.newXPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.xpath.XPath;

import org.apache.commons.io.FilenameUtils;
import org.krakenapps.docxcod.util.CloseableHelper;
import org.krakenapps.docxcod.util.XMLDocHelper;
import org.krakenapps.docxcod.util.XMLDocHelper.NodeListWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import freemarker.core.Environment;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class ChartDirectiveParser implements OOXMLProcessor {
	public class ChartUidFunction implements TemplateMethodModelEx {
		public static final String functionName = "chartUid";
		private String rid = null;
		private int count = 0;

		@Override
		public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
			String origRid = arguments.get(0).toString();
			if (!origRid.equals(rid)) {
				rid = origRid;
				count = 0;
			}
			return Integer.toString(count++);
		}
	}

	private static final String CHART_XML_CONTENTTYPE = "application/vnd.openxmlformats-officedocument.drawingml.chart+xml";
	private static final String DOCXCOD_CHART_XML_EXT = "docxcod_chart_xml";
	private static final String CONTENT_TYPES_XML = "[Content_Types].xml";

	public class ChartResFunction implements TemplateMethodModelEx {
		public static final String functionName = "ridHelper";
		public int count = 0;
		private final OOXMLPackage pkg;

		public ChartResFunction(OOXMLPackage pkg) {
			this.pkg = pkg;
		}

		@Override
		public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
			String originalRid = arguments.get(0).toString();
			String chartUid = arguments.get(1).toString();
			String rid = originalRid + "_" + chartUid;

			Environment ce = Environment.getCurrentEnvironment();
			@SuppressWarnings("unchecked")
			Set<String> knownVariable = ce.getKnownVariableNames();
			HashMap<String, Object> localRoot = new HashMap<String, Object>();
			for (String k : knownVariable) {
				localRoot.put(k, ce.getVariable(k));
			}

			appendContentType(pkg); // add ".kraken_chart_xml" to
									// [Content_Types].xml
			createCopiedChart(pkg, originalRid, chartUid, localRoot); // create
																		// copied
																		// chart(xml,
																		// xlsx)

			// must return text content of attr "r:id".
			return rid;
		}

		public String getName() {
			return functionName;
		}
	}

	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	private void createCopiedChart(OOXMLPackage pkg, String originalRid, String chartUid,
			HashMap<String, Object> localRoot) {
		String chartXmlPath = appendToRels(pkg, "word/_rels/document.xml.rels", originalRid, chartUid);
		chartXmlPath = FilenameUtils.concat("word", chartXmlPath);
		if (chartXmlPath != null) {
			logger.info("new chart xml filename: {}", chartXmlPath);
			String embeddedXlsxPath = createChartFromExisting(pkg, chartXmlPath, chartUid);
			createNewEmbeddedXlsx(pkg, embeddedXlsxPath, chartUid, localRoot);
			// modify chart from xlsx
		}
	}


	private String makeRelsPath(String chartXmlPath) {
		String name = FilenameUtils.getName(chartXmlPath);
		String path = FilenameUtils.getPath(chartXmlPath);

		return FilenameUtils.concat(FilenameUtils.concat(path, "_rels"), name + ".rels");
	}

	private String createChartFromExisting(OOXMLPackage pkg, String chartXmlPath, String chartUid) {
		InputStream f = null;
		try {
			String embeddedXlsxRid = null;
			String embeddedXlsxFile = null;

			f = new FileInputStream(new File(pkg.getDataDir(), chartXmlPath));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);
			NodeList nodeList = evaluateXPath(xpath, "//c:externalData", doc);
			if (nodeList.getLength() != 0) {
				Node n = nodeList.item(0);
				Node attrRid = n.getAttributes().getNamedItem("r:id");
				if (attrRid == null)
					throw new IllegalStateException(String.format("no Target externalData in %s", chartXmlPath));
				embeddedXlsxRid = attrRid.getTextContent();
			}

			XMLDocHelper.save(doc, new File(pkg.getDataDir(), makeNewChartFilename(chartXmlPath, chartUid)), true);

			f.close();

			f = new FileInputStream(new File(pkg.getDataDir(), makeRelsPath(chartXmlPath)));
			doc = newDocumentBuilder().parse(f);
			xpath = newXPath(doc);
			if (embeddedXlsxRid != null) {
				NodeList rsNodes = evaluateXPath(xpath, "//:Relationship[@Id='" + embeddedXlsxRid + "']", doc);
				if (rsNodes.getLength() != 0) {
					Node n = rsNodes.item(0);
					embeddedXlsxFile = FilenameUtils.concat("word/charts", n.getAttributes().getNamedItem("Target").getTextContent());
					String relTarget = makeXlsxRelTarget(chartXmlPath, makeNewXlsxFilename(embeddedXlsxFile, chartUid));
					n.getAttributes().getNamedItem("Target").setTextContent(FilenameUtils.separatorsToUnix(relTarget));
				}
			}
			XMLDocHelper.save(doc,
					new File(pkg.getDataDir(), makeRelsPath(makeNewChartFilename(chartXmlPath, chartUid))), true);

			return embeddedXlsxFile;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableHelper.safeClose(f);
		}

		return null;
	}

	private String makeXlsxRelTarget(String chartXmlPath, String xlsxFilename) {
		// TODO: use reliable relative path logic
		return "../embeddings/" + FilenameUtils.getName(xlsxFilename);
	}


	private String makeNewChartFilename(String chartXmlPath, String chartUid) {
		// trim right ".xml"
		String s = chartXmlPath.substring(0, chartXmlPath.length() - 4);
		s += "_" + chartUid + "." + DOCXCOD_CHART_XML_EXT;
		return s;
	}

	private String appendToRels(OOXMLPackage pkg, String relPath, String originalRid, String chartUid) {
		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), relPath));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);
			NodeList nodeList = evaluateXPath(xpath, "//:Relationship[@Id='" + originalRid + "']", doc);
			String chartXmlPath = null;
			if (nodeList.getLength() != 0) {
				Node n = nodeList.item(0);

				Node attrTarget = n.getAttributes().getNamedItem("Target");
				if (attrTarget == null)
					throw new IllegalStateException(String.format("no Target attribute in %s with rid %s", relPath,
							originalRid));
				chartXmlPath = attrTarget.getTextContent();

				Node attrType = n.getAttributes().getNamedItem("Type");
				if (attrType == null)
					throw new IllegalStateException(String.format("no Type attribute in %s with rid %s", relPath,
							originalRid));
				String type = attrType.getTextContent();

				Element newChild = doc.createElement("Relationship");

				newChild.setAttribute("Id", originalRid + "_" + chartUid);
				newChild.setAttribute("Target",
						FilenameUtils.separatorsToUnix(makeNewChartFilename(chartXmlPath, chartUid)));
				newChild.setAttribute("Type", type);
				doc.getFirstChild().appendChild(newChild);
			} else {
				return null;
			}

			XMLDocHelper.save(doc, new File(pkg.getDataDir(), relPath), true);

			return chartXmlPath;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableHelper.safeClose(f);
		}

		return null;
	}

	private String makeNewXlsxFilename(String embeddedXlsxFile, String chartUid) {
		String path = FilenameUtils.getPath(embeddedXlsxFile);
		String basename = FilenameUtils.getBaseName(embeddedXlsxFile);
		String ext = FilenameUtils.getExtension(embeddedXlsxFile);
		
		return path + basename + "_" + chartUid + "." + ext;
	}

	private void createNewEmbeddedXlsx(OOXMLPackage pkg, String embeddedXlsxFile, String chartUid, Map<String, Object> localRoot) {
		OOXMLPackage xlsx = new OOXMLPackage();
		FileInputStream is = null;
		FileOutputStream os = null;
		try {
			is = new FileInputStream(new File(pkg.getDataDir(), embeddedXlsxFile));
			xlsx.load(is);

			os = new FileOutputStream(new File(pkg.getDataDir(), makeNewXlsxFilename(embeddedXlsxFile, chartUid)));
			xlsx.save(os);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			CloseableHelper.safeClose(is);
			CloseableHelper.safeClose(os);
		}

	}

	private void appendContentType(OOXMLPackage pkg) {
		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), CONTENT_TYPES_XML));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);
			NodeList nodeList = evaluateXPath(xpath, "//:Default[@Extension='" + DOCXCOD_CHART_XML_EXT + "']", doc);
			if (nodeList.getLength() == 0) {
				Element newChild = doc.createElement("Default");
				newChild.setAttribute("ContentType", CHART_XML_CONTENTTYPE);
				newChild.setAttribute("Extension", DOCXCOD_CHART_XML_EXT);
				doc.getFirstChild().appendChild(newChild);
			}

			XMLDocHelper.save(doc, new File(pkg.getDataDir(), CONTENT_TYPES_XML), true);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableHelper.safeClose(f);
		}
	}

	@Override
	public void process(OOXMLPackage pkg, Map<String, Object> rootMap) {
		if (rootMap != null) {
			rootMap.put(ChartResFunction.functionName, new ChartResFunction(pkg));
			rootMap.put(ChartUidFunction.functionName, new ChartUidFunction());
		}

		InputStream f = null;
		try {
			f = new FileInputStream(new File(pkg.getDataDir(), "word/document.xml"));
			Document doc = newDocumentBuilder().parse(f);

			XPath xpath = newXPath(doc);

			NodeList nodeList = evaluateXPath(xpath, "//c:chart", doc);

			for (Node n : new NodeListWrapper(nodeList)) {
				InsertChartHelperMagicNode(doc, n);
			}

			XMLDocHelper.save(doc, new File(pkg.getDataDir(), "word/document.xml"), true);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseableHelper.safeClose(f);
		}
	}

	private enum AsttpPos {
		BEFORE,
		AFTER
	};

	private void InsertChartHelperMagicNode(Document doc, Node chartNode) {
		Node attrRid = chartNode.getAttributes().getNamedItem("r:id");
		String originalRid = attrRid.getTextContent();
		logger.info("chart rid: {}", originalRid);
		appendSiblingToTheParent(chartNode, "w:drawing", AsttpPos.BEFORE,
				getMagicNode(doc, String.format("#assign curChartUid=chartUid(\'%s\')", attrRid)));
		attrRid.setTextContent(String.format("${%s(\'%s\', curChartUid)}", ChartResFunction.functionName, originalRid));
	}

	private boolean appendSiblingToTheParent(Node currentNode, String parentNodeName, AsttpPos posOfSibling,
			Node magicNode) {
		Node p = currentNode;
		for (;;) {
			Node t = p.getParentNode();
			if (t == null)
				return false;
			p = t;
			if (p.getNodeName().equals(parentNodeName))
				break;
		}
		if (p.getParentNode() == null)
			return false;

		switch (posOfSibling) {
		case BEFORE:
			p.getParentNode().insertBefore(magicNode, p);
			break;
		case AFTER:
			p.getParentNode().insertBefore(magicNode, p.getNextSibling());
			break;
		}
		return true;
	}

	private Node getMagicNode(Document doc, String content) {
		Element magicNode = doc.createElement("KMagicNode");
		magicNode.appendChild(doc.createCDATASection(content));
		return magicNode;
	}

}
