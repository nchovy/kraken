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
package org.krakenapps.webconsole.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.msgbus.MessageBus;
import org.krakenapps.msgbus.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-keydist-servlet")
public class KeyDistributorServlet extends HttpServlet {
	private static final String PREFIX = "/keydist";
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(KeyDistributorServlet.class.getName());

	@Requires
	private HttpService httpd;

	@Requires
	private MessageBus msgbus;

	public void setHttpService(HttpService httpd) {
		this.httpd = httpd;
	}

	public void setMessageBus(MessageBus msgbus) {
		this.msgbus = msgbus;
	}

	/**
	 * Register servlet to servlet registry of webconsole
	 */
	@Validate
	public void start() {
		HttpContext ctx = httpd.ensureContext("webconsole");
		ctx.addServlet("keydist", this, PREFIX);
	}

	@Override
	public void log(String message, Throwable t) {
		logger.warn("kraken webconsole: key dist error", t);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String key = UUID.randomUUID().toString();

		String id = req.getParameter("session_id");
		Session session = msgbus.getSession(id);
		if (session == null) {
			logger.warn("kraken webconsole: cannot issue enc key, unknown session {}", id);
			return;
		}

		logger.trace("kraken webconsole: session [{}] new enc key [{}]", session.getGuid(), key);

		session.setProperty("enc_key", key);
		resp.getWriter().write(key);
		resp.getWriter().close();
	}

	@Override
	public String toString() {
		return "enc key distributor, session: " + msgbus.getSessions().size();
	}
}
