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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XmlRpcClient {
	private static final int TIMEOUT = 15000; // 15sec

	private XmlRpcClient() {
	}

	public static Object call(URL url, String method, Object... args) throws XmlRpcFaultException, IOException {
		Document document = XmlUtil.newDocument();
		Element methodCall = document.createElement("methodCall");
		Element methodName = document.createElement("methodName");
		methodName.setTextContent(method);
		methodCall.appendChild(methodName);
		Element params = document.createElement("params");
		for (Object target : args) {
			Element param = document.createElement("param");
			param.appendChild(XmlRpcBuilderUtil.buildValueElement(document, target));
			params.appendChild(param);
		}
		methodCall.appendChild(params);
		document.appendChild(methodCall);

		OutputStreamWriter writer = null;
		Document doc = null;
		try {
			URLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			writer = new OutputStreamWriter(conn.getOutputStream());
			writer.write(XmlUtil.toXmlString(document));
			writer.flush();

			doc = XmlUtil.read(conn.getInputStream());
		} finally {
			if (writer != null)
				writer.close();
		}

		XPathExpression expression = XmlUtil.compileXPath("//methodResponse");
		try {
			Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
			node = node.getFirstChild();
			if (node.getNodeName().equals("params")) {
				List<Object> result = XmlRpcParameterParser.parse(node.getChildNodes());
				if (result.size() == 1)
					return result.get(0);
				else
					return result;
			} else
				throw new XmlRpcFaultException(doc);
		} catch (XPathExpressionException e) {
			throw new XmlRpcParseException();
		}
	}

	public static Object call(String url, String method, Object... args) throws XmlRpcFaultException,
			MalformedURLException, IOException {
		return call(new URL(url), method, args);
	}
}
