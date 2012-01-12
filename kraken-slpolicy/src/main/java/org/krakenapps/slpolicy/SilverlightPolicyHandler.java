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
package org.krakenapps.slpolicy;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class SilverlightPolicyHandler extends SimpleChannelUpstreamHandler {
	private final Logger logger = LoggerFactory.getLogger(SilverlightPolicyHandler.class.getName());

	private static final String REQUEST = "<policy-file-request/>";

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer buf = (ChannelBuffer) e.getMessage();
		logger.debug("kraken webconsole: readable bytes [{}]", buf.readableBytes());
		
		if (buf.readableBytes() < REQUEST.length())
			return;

		logger.debug("kraken webconsole: silverlight policy request");

		Document xml = getPolicy();
		String s = xmlToString(xml);

		ChannelFuture f = ctx.getChannel().write(s);
		f.addListener(ChannelFutureListener.CLOSE);

		logger.debug("kraken webconsole: sent silverlight policy [{}]", s);
	}

	private Document getPolicy() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			Document d = builder.newDocument();

			Element accessPolicyElement = d.createElement("access-policy");
			Element crossDomainAccessElement = d.createElement("cross-domain-access");
			Element policyElement = d.createElement("policy");

			appendAllowFrom(d, policyElement);
			appendGrantTo(d, policyElement);

			crossDomainAccessElement.appendChild(policyElement);
			accessPolicyElement.appendChild(crossDomainAccessElement);
			d.appendChild(accessPolicyElement);

			return d;
		} catch (ParserConfigurationException e) {
			return null;
		}
	}

	public static String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void appendAllowFrom(Document d, Element policyElement) {
		Element allowFromElement = d.createElement("allow-from");
		Element domainElement = d.createElement("domain");
		domainElement.setAttribute("uri", "*");

		allowFromElement.appendChild(domainElement);
		policyElement.appendChild(allowFromElement);
	}

	private void appendGrantTo(Document d, Element policyElement) {
		Element grantToElement = d.createElement("grant-to");
		Element socketResourceElement = d.createElement("socket-resource");

		socketResourceElement.setAttribute("port", "4502");
		socketResourceElement.setAttribute("protocol", "tcp");

		grantToElement.appendChild(socketResourceElement);
		policyElement.appendChild(grantToElement);
	}

}
