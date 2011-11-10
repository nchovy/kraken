package org.krakenapps.ca;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
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
import org.krakenapps.webconsole.ServletRegistry;
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
	private ServletRegistry servletRegistry;

	@Requires
	private CertificateAuthorityService ca;

	@Validate
	public void start() {
		servletRegistry.register("/ca/crl", this);
	}

	@Invalidate
	public void stop() {
		servletRegistry.unregister("/ca/crl");
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

		List<RevokedCertificate> revokes = authority.getRevokedCertifcates();
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
