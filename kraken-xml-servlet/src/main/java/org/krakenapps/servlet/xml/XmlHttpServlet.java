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
package org.krakenapps.servlet.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XmlHttpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final Logger logger = LoggerFactory.getLogger(XmlHttpServlet.class.getName());

	private XmlHttpServiceApi manager;

	public XmlHttpServlet(XmlHttpServiceApi manager) {
		this.manager = manager;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/xml;charset=UTF-8");
		try {
			logger.debug("xml servlet pathinfo: " + req.getPathInfo());
			String[] tokens = req.getPathInfo().split("/");
			if (tokens.length != 3)
				return;

			String filterId = tokens[1];
			String methodName = tokens[2];
			Map<String, Object> params = buildParameterMap(req);
			Document doc = (Document) manager.invokeXmlMethod(filterId, methodName, params);
			if (doc == null) {
				logger.warn("kraken-xml-servlet: null returned");
			}

			resp.getOutputStream().write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
			xmlToStream(doc, resp.getOutputStream());
			resp.getOutputStream().close();
		} catch (Exception e) {
			logger.error("kraken-xml-servlet: ", e);
		}

	}

	private void xmlToStream(Node node, OutputStream writer) {
		try {
			Source source = new DOMSource(node);
			Result result = new StreamResult(writer);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
		} catch (TransformerException e) {
			logger.error("kraken-xml-servlet: xml transform failed.", e);
		}
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> buildParameterMap(HttpServletRequest req) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Enumeration it = req.getParameterNames();
		while (it.hasMoreElements()) {
			String name = (String) it.nextElement();
			Object value = req.getParameter(name);
			paramMap.put(name, value);
		}
		return paramMap;
	}
}
