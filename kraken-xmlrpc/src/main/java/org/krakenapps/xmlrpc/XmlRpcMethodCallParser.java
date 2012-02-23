/*
 * Copyright 2008 NCHOVY
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlRpcMethodCallParser {
	private XmlRpcMethodCallParser() {
	}

	public static XmlRpcMessage parse(Document document) {
		XmlRpcMessage message = new XmlRpcMessage(MessageType.MethodCall);
		XPathExpression expression = XmlUtil.compileXPath("//methodCall");
		try {
			Node node = (Node) expression.evaluate(document, XPathConstants.NODE);
			List<Node> childNodes = filtTextChildren(node);
			if (childNodes.size() != 2)
				throw new XmlRpcParseException();

			List<Object> parameters = null;
			if (childNodes.get(0).getNodeName().equals("methodName")) {
				message.setMethodName(childNodes.get(0).getTextContent());
				NodeList paramNodeList = childNodes.get(1).getChildNodes();
				parameters = XmlRpcParameterParser.parse(paramNodeList);
			} else {
				message.setMethodName(childNodes.get(1).getTextContent());
				NodeList paramNodeList = childNodes.get(0).getChildNodes();
				parameters = XmlRpcParameterParser.parse(paramNodeList);
			}

			message.setParameters(parameters);
			return message;
		} catch (XPathExpressionException e) {
			throw new XmlRpcParseException();
		}
	}

	private static List<Node> filtTextChildren(Node param) {
		List<Node> children = new ArrayList<Node>();
		int len = param.getChildNodes().getLength();
		for (int i = 0; i < len; i++) {
			Node child = param.getChildNodes().item(i);
			if (child.getNodeType() != 3)
				children.add(child);
		}

		return children;
	}

}
