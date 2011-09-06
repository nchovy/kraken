package org.krakenapps.webconsole.impl;

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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

public class SilverlightPolicyHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Document xml = getPolicy();
		String s = xmlToString(xml);

		HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		resp.setHeader("Content-Type", "text/xml");
		resp.setContent(ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8));

		ChannelFuture f = ctx.getChannel().write(resp);
		f.addListener(ChannelFutureListener.CLOSE);
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
