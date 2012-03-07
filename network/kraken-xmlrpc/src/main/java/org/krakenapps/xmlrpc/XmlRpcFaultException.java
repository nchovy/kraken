/*
 * Copyright 2011 Future Systems
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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlRpcFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private Integer faultCode;
	private String faultString;

	public XmlRpcFaultException(Integer faultCode, String faultString) {
		super(faultString);
		this.faultCode = faultCode;
		this.faultString = faultString;
	}

	public XmlRpcFaultException(Document document) {
		XPathExpression expression = XmlUtil.compileXPath("//methodResponse");
		try {
			Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
			node = node.getFirstChild().getFirstChild().getFirstChild();
			if (node.getChildNodes().getLength() == 2) {
				set(this, node.getChildNodes().item(0));
				set(this, node.getChildNodes().item(1));
			}
		} catch (XPathExpressionException e) {
			throw new XmlRpcParseException();
		}
	}

	private static void set(XmlRpcFaultException e, Node node) {
		NodeList children = node.getChildNodes();
		Node name = null;
		Node value = null;
		if (children.item(0).getNodeName().equals("name")) {
			name = children.item(0);
			value = children.item(1);
		} else {
			name = children.item(1);
			value = children.item(0);
		}

		if (name.getTextContent().equals("faultCode"))
			e.faultCode = Integer.parseInt(value.getTextContent());
		else if (name.getTextContent().equals("faultString"))
			e.faultString = value.getTextContent();

	}

	public Integer getFaultCode() {
		return faultCode;
	}

	public String getFaultString() {
		return faultString;
	}

	@Override
	public String toString() {
		return "XmlRpcFaultException [faultCode=" + faultCode + ", faultString=" + faultString + "]";
	}
}
