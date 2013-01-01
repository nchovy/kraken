/*
 * Copyright 2013 Future Systems
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
package org.krakenapps.logdb.httpinput.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.log.api.SimpleLog;
import org.krakenapps.logdb.httpinput.HttpInputLogger;
import org.krakenapps.logdb.httpinput.HttpInputService;
import org.krakenapps.logdb.httpinput.HttpInputServlet;

@Component(name = "logdb-httpinput-servlet")
@Provides(specifications = { HttpInputServlet.class })
public class HttpInputServletImpl extends HttpServlet implements HttpInputServlet {
	private static final long serialVersionUID = 1L;

	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(HttpInputServletImpl.class);

	@Requires
	private HttpService httpd;

	@Requires
	private HttpInputService httpInput;

	@Validate
	public void start() {
		HttpContext ctx = httpd.ensureContext("webconsole");
		ctx.addServlet("logdb-httpinput", this, "/logdb/inputs/*");
	}

	@Invalidate
	public void stop() {
		if (httpd != null) {
			HttpContext ctx = httpd.ensureContext("webconsole");
			ctx.removeServlet("logdb-httpinput");
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!req.getContentType().equalsIgnoreCase("text/json")) {
			slog.warn("kraken-logdb-httpinput: invalid post content type [{}] from remote [{}]", req.getContentType(),
					req.getRemoteAddr());
			resp.sendError(400);
			return;
		}

		// remove '/' prefix
		String token = req.getPathInfo().substring(1);

		HttpInputLogger logger = httpInput.findLogger(token);
		if (logger == null) {
			slog.warn("kraken-logdb-httpinput: input logger for token [{}] does not exist", token);
			resp.sendError(404);
			return;
		}

		JsonNode container = new ObjectMapper().readTree(req.getReader());

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
			for (int i = 0; i < container.size(); i++) {
				Map<String, Object> params = new ObjectMapper().readValue(container.get(i),
						new TypeReference<Map<String, Object>>() {
						});
				Date date = new Date();
				Object time = params.get("_time");
				if (time != null && time instanceof String) {
					try {
						date = dateFormat.parse((String) time);
					} catch (Throwable t) {
					}
				}

				logger.write(new SimpleLog(date, logger.getFullName(), params));
			}

			if (slog.isDebugEnabled())
				slog.debug("kraken-logdb-httpinput: received [{}] logs for [{}} from remote [{}}",
						new Object[] { container.size(), logger.getFullName(), req.getRemoteAddr() });
		} catch (ClassCastException e) {
			slog.error("kraken-logdb-httpinput: invalid json body format", e);
			resp.sendError(400);
			return;
		}
	}
}
