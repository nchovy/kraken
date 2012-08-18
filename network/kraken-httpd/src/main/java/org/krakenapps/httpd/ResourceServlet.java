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
package org.krakenapps.httpd;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krakenapps.httpd.impl.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(ResourceServlet.class.getName());

	protected abstract InputStream getInputStream(HttpServletRequest req);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.trace("kraken httpd: httpd session [{}]", req.getSession().getId());
		// resp.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, "chunked");

		String pathInfo = req.getPathInfo();
		if (req instanceof Request) {
			if (pathInfo.endsWith("/"))
				((Request) req).setPathInfo(pathInfo + "index.html");
		}
		InputStream is = getInputStream(req);

		if (is == null || is.available() == 0) {
			if (req instanceof Request) {
				if (!pathInfo.endsWith("/")) {
					((Request) req).setPathInfo(pathInfo + "/index.html");
					is = getInputStream(req);
					if (is != null && is.available() > 0) {
						resp.sendRedirect(req.getServletPath() + pathInfo + "/");
						return;
					}
				}
			}

			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		try {
			resp.setContentType(getMimeType(req.getPathInfo()));

			byte[] b = new byte[4096];
			int len;
			while ((len = is.read(b)) != -1)
				resp.getOutputStream().write(b, 0, len);
		} catch (IOException e) {
			throw e;
		} finally {
			is.close();
			resp.getOutputStream().close();
		}
	}

	private String getMimeType(String path) {
		String mimeType = MimeTypes.instance().getByFile(path);

		if (mimeType == null)
			mimeType = "text/html";

		if (mimeType.startsWith("text/"))
			mimeType += "; charset=utf-8";

		return mimeType;
	}
}
