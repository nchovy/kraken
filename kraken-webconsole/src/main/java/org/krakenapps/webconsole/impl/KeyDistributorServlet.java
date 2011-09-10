package org.krakenapps.webconsole.impl;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.krakenapps.webconsole.ServletRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-keydist-servlet")
public class KeyDistributorServlet extends HttpServlet {
	private static final String PREFIX = "/keydist";
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(KeyDistributorServlet.class.getName());

	@Requires
	private ServletRegistry servletRegistry;

	@Requires
	private MessageBus msgbus;

	/**
	 * Register servlet to servlet registry of webconsole
	 */
	@Validate
	public void start() {
		servletRegistry.register(PREFIX, this);
	}

	/**
	 * Unregister servlet from servlet registry
	 */
	@Invalidate
	public void stop() {
		if (servletRegistry != null)
			servletRegistry.unregister(PREFIX);
	}

	@Override
	public void log(String message, Throwable t) {
		logger.warn("kraken webconsole: key dist error", t);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String key = UUID.randomUUID().toString();

		int id = Integer.valueOf(req.getParameter("session_id"));
		Session session = msgbus.getSession(id);
		if (session == null) {
			logger.warn("kraken webconsole: cannot issue enc key, unknown session {}", id);
			return;
		}

		logger.trace("kraken webconsole: session [{}] new enc key [{}]", session.getId(), key);

		session.setProperty("enc_key", key);
		resp.getWriter().write(key);
		resp.getWriter().close();
	}

	@Override
	public String toString() {
		return "enc key distributor, session: " + msgbus.getSessions().size();
	}
}
