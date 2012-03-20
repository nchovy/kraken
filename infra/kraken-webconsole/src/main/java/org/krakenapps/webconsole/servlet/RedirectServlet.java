package org.krakenapps.webconsole.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-redirect-servlet")
public class RedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(RedirectServlet.class);

	@Requires
	private HttpService httpd;

	@Validate
	public void start() {
		HttpContext ctx = httpd.ensureContext("redirect");
		ctx.addServlet("redirect", this, "/*");
	}

	@Invalidate
	public void stop() {
		if (httpd != null) {
			HttpContext ctx = httpd.ensureContext("redirect");
			ctx.removeServlet("redirect");
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("kraken webconsole: request uri [{}] is redirect to https", req.getRequestURI());
		logger.trace("kraken webconsole: local address is [{}]", req.getLocalAddr());
		String redirectURL = "https://";

		String host = req.getHeader(HttpHeaders.Names.HOST);
		if (host == null)
			redirectURL += req.getLocalAddr() + req.getRequestURI();
		else
			redirectURL += host + req.getRequestURI();

		if (req.getQueryString() != null && !req.getQueryString().equals(""))
			redirectURL += "?" + req.getQueryString();

		resp.sendRedirect(redirectURL);
	}
}
