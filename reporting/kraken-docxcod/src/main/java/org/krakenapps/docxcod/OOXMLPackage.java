package org.krakenapps.docxcod;

import static org.krakenapps.docxcod.util.XMLDocHelper.evaluateXPath;
import static org.krakenapps.docxcod.util.XMLDocHelper.newDocumentBuilder;
import static org.krakenapps.docxcod.util.XMLDocHelper.newXPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.krakenapps.docxcod.util.XMLDocHelper.NodeListWrapper;
import org.krakenapps.docxcod.util.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OOXMLPackage {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	private File dataDir = null;

	private Relationship rootRel = null;

	public OOXMLPackage() {

	}

	public File getDataDir() {
		return dataDir;
	}

	public void attach(File targetDir) {
		if (this.dataDir != null)
			throw new AlreadyAttachedException(targetDir.getAbsolutePath());
		this.dataDir = targetDir;
		parseRels();
	}

	public void load(InputStream is, File targetDir) throws IOException {
		if (this.dataDir != null)
			throw new AlreadyAttachedException(targetDir.getAbsolutePath());
		this.dataDir = targetDir;
		try {
			ZipHelper.extract(is, dataDir);
			tidyXMLs();
			parseRels();
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
				// ignore
			}
		}
	}

	private void parseRels() {
		File rootRelDir = new File(dataDir, "_rels");
		File rootRelFile = new File(rootRelDir, ".rels");
		Stack<SimpleEntry<File, Relationship>> remaining = new Stack<SimpleEntry<File, Relationship>>();
		Set<Relationship> relationships = new HashSet<Relationship>();

		rootRel = new Relationship();
		remaining.push(new SimpleEntry<File, Relationship>(rootRelFile, rootRel));
		try {
			while (!remaining.empty()) {
				SimpleEntry<File, Relationship> cur = remaining.pop();
				File curFile = cur.getKey();
				if (!curFile.exists())
					continue;
				Document doc = newDocumentBuilder().parse(curFile);
				logger.trace("Parsing: " + curFile);
				Relationship parent = cur.getValue();

				try {
					XPath xpath = newXPath(doc);
					NodeList nodeList = evaluateXPath(xpath, "//DEF:Relationship", doc);
					for (Node n : new NodeListWrapper(nodeList)) {
						NamedNodeMap attrs = n.getAttributes();
						Relationship rel = new Relationship(parent, attrs);
						parent.children.add(rel);
						relationships.add(rel);
						remaining.push(
								new SimpleEntry<File, Relationship>(
										makeRelFile(curFile, rel.target),
										rel));
					}

				} catch (XPathExpressionException e) {
					logger.warn("invalid rels document: " + curFile);
				} finally {
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
			// printRelationship(rootRel, new PrintWriter(out));
			logger.debug(rootRel.toSummaryString());

		} catch (SAXException e) {
			logger.error("invalid XML doc", e);
		} catch (IOException e) {
			logger.error("exception while parsing docx rels", e);
		} catch (ParserConfigurationException e) {
			logger.error("exception while parsing docx rels", e);
		}

	}

	private void printRelationship(Relationship rootRel, PrintWriter writer) {
		Stack<SimpleEntry<Integer, Relationship>> r = new Stack<SimpleEntry<Integer, Relationship>>();
		r.push(new SimpleEntry<Integer, Relationship>(0, rootRel));
		while (!r.empty()) {
			SimpleEntry<Integer, Relationship> entry = r.pop();
			int depth = entry.getKey();
			Relationship rel = entry.getValue();

			String prefix = "";
			while (depth-- > 0)
				prefix += "\t";
			writer.println(prefix + rel.toString());

			for (Relationship c : rel.children) {
				r.push(new SimpleEntry<Integer, Relationship>(entry.getKey() + 1, c));
			}
		}
		writer.flush();
	}

	private File makeRelFile(File curFile, String path) {
		File parent = curFile.getParentFile().getParentFile();
		File target = new File(parent, path);
		File relsParent = target.getParentFile();
		File rels = new File(relsParent, "_rels");

		return new File(rels, target.getName() + ".rels");
	}

	public void load(InputStream is) throws IOException {
		try {
			File tempDir = File.createTempFile("KrakenDocxcodData_", "");
			tempDir.delete();
			tempDir.mkdirs();
			load(is, tempDir);
		} catch (IOException e) {
			throw e;
		}
	}

	private void tidyXMLs() {
		ArrayList<File> files = new ArrayList<File>();
		ZipHelper.getFilesRecursivelyIn(dataDir, files);

		files = filterXMLfiles(files);

		for (File f : files) {
			tidyXML(f);
		}
	}

	private void tidyXML(File f) {
		Document doc;
		try {
			doc = newDocumentBuilder().parse(f);
			if (doc == null)
				return;
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(doc), new StreamResult(f));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<File> filterXMLfiles(ArrayList<File> files) {
		ArrayList<File> result = new ArrayList<File>();
		for (File f : files) {
			String n = f.getName();
			n = n.toUpperCase();
			if (n.endsWith(".XML") || n.endsWith(".RELS"))
				result.add(f);
		}

		return result;
	}

	public void save(OutputStream os) {
		ZipOutputStream zipOs = null;
		try {
			parseRels();
			
			zipOs = new ZipOutputStream(os);
			List<File> files = new ArrayList<File>();

			files.add(new File(dataDir, "[Content_Types].xml"));

			String[] listParts = listParts("");

			final Set<String> setOfParts = new HashSet<String>(Arrays.asList(listParts));

			for (String part : listParts) {
				files.add(new File(dataDir, part));
			}

			ZipHelper.getFilesRecursivelyIn(dataDir, files, new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().equals(".rels") || isRelsForParts(pathname))
						return true;
					else
						return false;
				}

				private boolean isRelsForParts(File pathname) {
					String path = pathname.getPath();
					if (path.endsWith(".rels")) {
						String relParent = path.substring(0, path.length() - ".rels".length());
						relParent = relParent.replace("_rels" + File.separator, "");
						return setOfParts.contains(ZipHelper.extractSubPath(new File(relParent), dataDir));
					}
					return false;
				}
			});

			ZipHelper.archive(zipOs, files, dataDir);
			zipOs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (zipOs != null) {
				try {
					zipOs.close();
				} catch (IOException e) {
					e.printStackTrace();
					// ignore
				}
			}
		}
	}

	public String addPart(String string, String string2) {
		return "";
	}

	public String getRelationId(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public String findPart(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}

	public Relationship getRootRelationship() {
		return rootRel;
	}

	public String[] listParts(String prefix) {
		if (rootRel == null)
			return new String[0];

		ArrayList<String> result = new ArrayList<String>();
		Stack<Object[]> s = new Stack<Object[]>();
		s.push(new Object[] { rootRel, "" });
		while (!s.empty()) {
			Object[] args = s.pop();
			Relationship currRel = (Relationship) args[0];
			String currPrefix = (String) args[1];

			for (Relationship r : currRel.children) {
				String combinedPath = FilenameUtils.concat(currPrefix, r.target);
				if (combinedPath.startsWith(prefix))
					result.add(combinedPath);
				s.push(new Object[] { r,
						FilenameUtils.normalize(FilenameUtils.getFullPath(combinedPath)) });
			}
		}
		logger.debug("result : " + result);

		return (String[]) result.toArray(new String[0]);
	}

	public static void main(String[] args) {
		FileOutputStream os = null;
		FileOutputStream chartOs = null;
		try {
			OOXMLPackage pkg = new OOXMLPackage();
			OOXMLPackage chart = null;

			pkg.attach(new File(args[0]));
			chart = new OOXMLPackage();
			File xlsxExtracted = new File(args[0], "word/embeddings/Microsoft_Excel_____1.xlsx.extracted");
			if (xlsxExtracted.exists())
			{
				chartOs = new FileOutputStream(new File(args[0], "word/embeddings/Microsoft_Excel_____1.xlsx"));
				chart.attach(xlsxExtracted);
				chart.save(chartOs);
			}
			os = new FileOutputStream(new File(args[0] + "_mods.docx"));
			pkg.save(os);
			System.out.println(args[0] + "_mods.docx saved.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
			if (chartOs != null)
				try {
					chartOs.close();
				} catch (IOException e) {
				}
		}
	}
}
