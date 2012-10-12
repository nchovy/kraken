package org.krakenapps.docxcod.util;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLDocHelper {
	static private DocumentBuilderFactory dbFactory;
	static private XPathFactory xpathFactory;

	public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		if (dbFactory == null) {
			dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
		}
		return dbFactory.newDocumentBuilder();
	}

	public static XPath newXPath(Document doc) {
		if (xpathFactory == null) {
			xpathFactory = XPathFactory.newInstance();
		}
			
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(new DocumentNamespaceContext(doc));
		return xpath;
	}

	private static class DocumentNamespaceContext implements NamespaceContext {
		Document doc;
		String unnamedPrefix = null;
		String unnamedURI = null;

		public DocumentNamespaceContext(Document doc) {
			this.doc = doc;
			if (doc.lookupNamespaceURI(null) != null) {
				unnamedPrefix = "DEF";
				unnamedURI = doc.lookupNamespaceURI(null);
			}
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix.equals(unnamedPrefix))
				return unnamedURI;
			return doc.lookupNamespaceURI(prefix);
		}

		@Override
		public String getPrefix(String namespaceURI) {
			// no need to implement for using xpath
			return null;
		}

		@Override
		public Iterator<?> getPrefixes(String namespaceURI) {
			// no need to implement for using xpath
			return null;
		}
	}

	public static NodeList evaluateXPath(Document doc, String expression) throws XPathExpressionException {
		return (NodeList) newXPath(doc).evaluate(expression, doc, XPathConstants.NODESET);
	}
	public static NodeList evaluateXPath(XPath xpath, String expression, Node node) throws XPathExpressionException {
		return (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
	}
	public static NodeList evaluateXPathExpr(XPathExpression xpath, Node node) throws XPathExpressionException {
		return (NodeList) xpath.evaluate(node, XPathConstants.NODESET);
	}

	public static class NodeListWrapper implements Iterable<Node> {
		private final NodeList nl;

		public NodeListWrapper(NodeList nl) {
			this.nl = nl;
		}

		public class IteratorI implements Iterator<Node> {
			private int current;

			public IteratorI() {
				this.current = 0;
			}

			@Override
			public boolean hasNext() {
				if (nl == null)
					return false;
				else
					return current < nl.getLength();
			}

			@Override
			public Node next() {
				if (nl == null)
					return null;
				else
					return nl.item(current++);
			}

			@Override
			public void remove() {
			}
		}

		@Override
		public Iterator<Node> iterator() {
			return new IteratorI();
		}
	}
}
