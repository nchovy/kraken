/*
 * Copyright 2009 NCHOVY
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
package org.krakenapps.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class XmlUtil {
	private static Logger logger = LoggerFactory.getLogger(XmlUtil.class);

	public static Document read(InputStream inputStream) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(inputStream);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore.
				}
		}
		return null;
	}

	public static Document parse(String buffer) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(buffer.getBytes(Charset.forName("utf-8")));
			return builder.parse(is);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public static Document parse(byte[] buffer) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(buffer);
			return builder.parse(is);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		} catch (SAXException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public static Document newDocument() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	public static XPathExpression compileXPath(String expression) {
		try {
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();
			return xPath.compile(expression);
		} catch (XPathExpressionException e) {
			logger.error("Invalid XPath expression [" + expression + "]");
			return null;
		}
	}

	public static String getAttribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		if (attribute == null) {
			return null;
		}

		return attribute.getTextContent();
	}

	public static String toXmlString(Document document) {
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document.getLastChild()), result);
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
		} catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
		} catch (TransformerException e) {
			logger.error(e.getMessage());
		}
		return sw.toString();
	}

	private static byte[] encodeMap = new byte[64];
	private static byte[] decodeMap = new byte[128];
	static {
		int i = 0;
		byte b = 'A';
		for (; i < 26; i++) {
			encodeMap[i] = b;
			decodeMap[b++] = (byte) i;
		}
		b = 'a';
		for (; i < 52; i++) {
			encodeMap[i] = b;
			decodeMap[b++] = (byte) i;
		}
		b = '0';
		for (; i < 62; i++) {
			encodeMap[i] = b;
			decodeMap[b++] = (byte) i;
		}
		encodeMap[62] = '+';
		decodeMap['+'] = 62;
		encodeMap[63] = '/';
		decodeMap['/'] = 63;
	}

	public static byte[] encodeBase64(byte[] bytes) {
		byte[] result = new byte[(bytes.length + 2) / 3 * 4];
		for (int i = 0; i < (bytes.length + 2) / 3; i++) {
			byte a = bytes[i * 3];
			byte b = (i * 3 + 1 < bytes.length) ? bytes[i * 3 + 1] : 0;
			byte c = (i * 3 + 2 < bytes.length) ? bytes[i * 3 + 2] : 0;

			long l = (a & 0xFF) << 16 | (b & 0xFF) << 8 | (c & 0xFF);
			for (int j = 3; j >= 0; j--) {
				result[i * 4 + j] = encodeMap[(int) (l & 0x3F)];
				l >>= 6;
			}
		}
		if (bytes.length % 3 == 2)
			result[result.length - 1] = '=';
		else if (bytes.length % 3 == 1)
			result[result.length - 2] = result[result.length - 1] = '=';
		return result;
	}

	public static byte[] decodeBase64(byte[] bytes) {
		int len = bytes.length / 4 * 3;
		if (bytes[bytes.length - 1] == '=')
			len--;
		if (bytes[bytes.length - 2] == '=')
			len--;

		byte[] result = new byte[len];
		for (int i = 0; i < bytes.length / 4; i++) {
			byte a = decodeMap[bytes[i * 4]];
			byte b = decodeMap[bytes[i * 4 + 1]];
			byte c = decodeMap[bytes[i * 4 + 2]];
			byte d = decodeMap[bytes[i * 4 + 3]];

			long l = (a & 0x3F) << 18 | (b & 0x3F) << 12 | (c & 0x3F) << 6 | (d & 0x3F);
			for (int j = 2; j >= 0; j--) {
				if (i * 3 + j < result.length)
					result[i * 3 + j] = (byte) (l & 0xFF);
				l >>= 8;
			}
		}
		return result;
	}
}
