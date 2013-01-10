/*
 * Copyright 2012 Future Systems, Inc.
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
package org.krakenapps.ca;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.ca.util.CrlBuilder;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet for CRL distribution
 * 
 * @author xeraph
 */
@Component(name = "ca-crl-servlet")
public class CrlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(CrlServlet.class.getName());

	@Requires
	private HttpService httpd;

	@Requires
	private CertificateAuthorityService ca;

	@Validate
	public void start() {
		HttpContext ctx = httpd.ensureContext("frodo");
		ctx.addServlet("crl", this, "/ca/crl/*");
	}

	@Invalidate
	public void stop() {
		if (httpd != null) {
			HttpContext ctx = httpd.ensureContext("frodo");
			ctx.removeServlet("crl");
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String authorityName = req.getPathInfo().substring(1); // remove slash

		if (logger.isDebugEnabled())
			logger.debug("kraken ca: crl [{}] request from [{}, {}]",
					new Object[] { authorityName, req.getRequestURI(), req.getRemoteAddr() });

		CertificateAuthority authority = ca.getAuthority(authorityName);
		if (authority == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "CRL does not exists for " + authorityName);
			return;
		}

		X509Certificate caCert = authority.getRootCertificate().getCertificate();
		RSAPrivateKey caKey = authority.getRootCertificate().getPrivateKey(authority.getRootKeyPassword());

		List<RevokedCertificate> revokes = null;
		String serial = req.getParameter("serial");
		if (serial != null) {
			RevokedCertificate rc = authority.getRevokedCertificate(serial);

			if (rc == null)
				revokes = new ArrayList<RevokedCertificate>();
			else
				revokes = Arrays.asList(rc);
		} else
			revokes = authority.getRevokedCertificates();
		
		try {
			byte[] b = CrlBuilder.getCrl(caCert, caKey, revokes);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setHeader("Content-Type", "application/pkix-crl");
			resp.setContentLength(b.length);
			resp.getOutputStream().write(b);

		} catch (Exception e) {
			logger.error("kraken ca: cannot generate crl for " + authorityName, e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot generate CRL for " + authorityName);
		} finally {
			resp.getOutputStream().close();
		}
	}
}
