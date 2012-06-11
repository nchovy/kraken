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
package org.krakenapps.servlet.xmlrpc;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krakenapps.xmlrpc.XmlRpcMessage;
import org.krakenapps.xmlrpc.XmlRpcMethodCallParser;
import org.krakenapps.xmlrpc.XmlRpcMethodResponseBuilder;
import org.krakenapps.xmlrpc.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class XmlRpcServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(XmlRpcServlet.class.getName());

	private XmlRpcMethodRegistry registry;

	public XmlRpcServlet(XmlRpcMethodRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String xmlBody = null;
		try {
			xmlBody = readXmlBody(req);
			if (logger.isTraceEnabled())
				logger.trace("kraken xmlrpc servlet: request [{}]" + xmlBody);

			String response = processRpc(xmlBody);

			resp.setContentType("text/xml");
			resp.getOutputStream().write(response.getBytes("utf-8"));

			logger.trace("kraken xmlrpc servlet: response [{}]", response);
		} catch (Exception e) {
			logger.error("kraken xmlrpc servlet: cannot process xmlrpc => " + xmlBody, e);
		} finally {
			resp.getOutputStream().close();
		}
	}

	private String processRpc(String xmlBody) throws IOException {
		Document document = XmlUtil.parse(xmlBody);
		XmlRpcMessage methodCall = XmlRpcMethodCallParser.parse(document);
		String methodName = methodCall.getMethodName();
		Object[] parameters = methodCall.getParameters();

		Document methodResponse = null;
		try {
			Object value = registry.dispatch(methodName, parameters);
			methodResponse = XmlRpcMethodResponseBuilder.result(value);
		} catch (Exception e) {
			methodResponse = XmlRpcMethodResponseBuilder.fault(e);
		}

		return XmlUtil.toXmlString(methodResponse);
	}

	private String readXmlBody(HttpServletRequest req) throws IOException {
		ServletInputStream is = req.getInputStream();
		StringBuilder sb = new StringBuilder();
		char[] chars = new char[4096];
		InputStreamReader reader = new InputStreamReader(is);

		while (true) {
			int len = reader.read(chars, 0, chars.length);
			if (len < 0)
				break;

			sb.append(chars, 0, len);
		}

		return sb.toString();
	}
}
