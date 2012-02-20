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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlRpcParameterParser {
	public static List<Object> parse(NodeList paramNodeList) {
		List<Object> params = new ArrayList<Object>();
		for (int i = 0; i < paramNodeList.getLength(); ++i) {
			Node paramNode = paramNodeList.item(i);
			if (paramNode.getNodeType() != 3)
				params.add(parse(paramNode));
		}
		return params;
	}

	public static Object parse(Node paramNode) {
		List<Node> childNodes = filtTextChildren(paramNode);

		if (childNodes.size() != 1)
			throw new XmlRpcParseException();

		Node valueNode = childNodes.get(0);
		return parseValueNode(valueNode);
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

	private static Object parseValueNode(Node valueNode) {
		List<Node> childNodes = filtTextChildren(valueNode);
		if (childNodes.size() == 0)
			return null;

		if (childNodes.size() != 1)
			throw new XmlRpcParseException();

		Node typeNode = childNodes.get(0);
		String nodeName = typeNode.getNodeName();

		if (nodeName.equals("i4"))
			return parseInteger(typeNode);
		else if (nodeName.equals("int"))
			return parseInteger(typeNode);
		else if (nodeName.equals("string"))
			return parseString(typeNode);
		else if (nodeName.equals("boolean"))
			return parseBoolean(typeNode);
		else if (nodeName.equals("double"))
			return parseDouble(typeNode);
		else if (nodeName.equals("dateTime.iso8601"))
			return parseDate(typeNode);
		else if (nodeName.equals("base64"))
			return parseBase64(typeNode);
		else if (nodeName.equals("struct"))
			return parseStruct(typeNode);
		else if (nodeName.equals("array"))
			return parseArray(typeNode);

		throw new XmlRpcParseException();
	}

	private static Object parseInteger(Node scalarNode) {
		return Integer.parseInt(scalarNode.getTextContent());
	}

	private static Object parseBoolean(Node scalarNode) {
		int value = Integer.parseInt(scalarNode.getTextContent());
		if (value == 1)
			return true;
		else if (value == 0)
			return false;
		else
			throw new XmlRpcParseException();
	}

	private static Object parseString(Node scalarNode) {
		return scalarNode.getTextContent();
	}

	private static Object parseDouble(Node scalarNode) {
		return Double.parseDouble(scalarNode.getTextContent());
	}

	private static Object parseDate(Node scalarNode) {
		try {
			DateFormat iso8601Format = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
			return iso8601Format.parse(scalarNode.getTextContent());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new XmlRpcParseException();
		}
	}

	private static Object parseBase64(Node scalarNode) {
		return XmlUtil.decodeBase64(scalarNode.getTextContent().getBytes());
	}

	private static Object parseStruct(Node structNode) {
		Map<String, Object> map = new HashMap<String, Object>();
		NodeList memberNodeList = structNode.getChildNodes();
		for (int i = 0; i < memberNodeList.getLength(); ++i) {
			Node memberNode = memberNodeList.item(i);
			if (memberNode.getNodeType() != 3)
				parseMember(map, memberNode);
		}
		return map;
	}

	private static void parseMember(Map<String, Object> map, Node memberNode) {
		List<Node> childNodes = filtTextChildren(memberNode);
		if (childNodes.size() != 2)
			throw new XmlRpcParseException();

		String name = null;
		Object parameter = null;
		if (childNodes.get(0).getNodeName().equals("name")) {
			name = childNodes.get(0).getTextContent();
			parameter = parseValueNode(childNodes.get(1));
		} else {
			name = childNodes.get(1).getTextContent();
			parameter = parseValueNode(childNodes.get(0));
		}

		map.put(name, parameter);
	}

	private static Object parseArray(Node arrayNode) {
		List<Node> childNodes = filtTextChildren(arrayNode);
		if (childNodes.size() != 1)
			throw new XmlRpcParseException();

		List<Object> objectList = new ArrayList<Object>();
		Node dataNode = childNodes.get(0);

		List<Node> dataChildren = filtTextChildren(dataNode);
		for (Node valueNode : dataChildren) {
			if (valueNode.getNodeName().equals("value") == false)
				throw new XmlRpcParseException();

			Object valueParameter = parseValueNode(valueNode);
			objectList.add(valueParameter);
		}
		return objectList.toArray();
	}
}
