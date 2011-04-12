package org.krakenapps.servlet.xmlrpc;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.krakenapps.xmlrpc.XmlRpcMessage;
import org.krakenapps.xmlrpc.XmlRpcMethodCallParser;
import org.krakenapps.xmlrpc.XmlRpcMethodResponseBuilder;
import org.krakenapps.xmlrpc.XmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@Component(name = "kraken-xmlrpc-servlet", immediate = true)
@Provides
public class XmlRpcServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(XmlRpcServlet.class.getName());

	@Requires
	private XmlRpcMethodRegistry registry;
	
	@Override
	public void init() throws ServletException {
		System.out.println("xmlrpc servlet init()");
		super.init();
	}

	@Override
	public void destroy() {
		System.out.println("xmlrpc servlet destroy()");
		super.destroy();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Document methodResponse = processRpc(req);
		String response = XmlUtil.toXmlString(methodResponse);
		resp.setContentType("text/xml");
		resp.getOutputStream().write(response.getBytes("utf-8"));
		resp.getOutputStream().close();
	}

	private Document processRpc(HttpServletRequest req) throws IOException {
		String xmlBody = readXmlBody(req);
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
		return methodResponse;
	}

	private String readXmlBody(HttpServletRequest req) throws IOException {
		StringBuilder sb = new StringBuilder(81920);
		char[] chars = new char[4096];
		InputStreamReader reader = new InputStreamReader(req.getInputStream());

		while (true) {
			int len = reader.read(chars, 0, chars.length);
			if (len < 0)
				break;

			sb.append(chars, 0, len);
		}

		String xmlBody = sb.toString();

		if (logger.isTraceEnabled())
			logger.trace("xmlrpc servlet input: " + xmlBody);
		return xmlBody;
	}
}
