/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.http.internal.service;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public final class ResourceServlet extends HttpServlet {
	@Override
	public String toString() {
		return "ResourceServlet [name=" + path + "]";
	}

	private static final long serialVersionUID = 1L;
	private final String path;

	public ResourceServlet(String name) {
		this.path = name;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		String target = req.getPathInfo();
		if (target == null) {
			target = "";
		}

		if (target.equals(""))
			target = "/index.html";

		if (!target.startsWith("/")) {
			target = "/" + target;
		}

		String resName = this.path + target;

		URL url = getServletContext().getResource(resName);

		if (url == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			InputStream is = url.openStream();
			boolean exists = is != null;
			if (exists)
				is.close();

			if (!exists) {
				res.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			handle(req, res, url, resName);
		}
	}

	private void handle(HttpServletRequest req, HttpServletResponse res, URL url, String resName) throws IOException {
		String contentType = getServletContext().getMimeType(resName);
		if (contentType != null) {
			res.setContentType(contentType);
		}

		long lastModified = getLastModified(url);
		if (lastModified != 0) {
			res.setDateHeader("Last-Modified", lastModified);
		}

		if (!resourceModified(lastModified, req.getDateHeader("If-Modified-Since"))) {
			res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			copyResource(url, res);
		}
	}

	private long getLastModified(URL url) {
		long lastModified = 0;

		URLConnection conn = null;
		try {
			conn = url.openConnection();
			lastModified = conn.getLastModified();
		} catch (Exception e) {
			// Do nothing
		} finally {
			// if you do not close URL here, server will hold file exclusively
			// until GC'd. openConnection succeeded even though the file not
			// found.
			if (conn != null)
				try {
					conn.getInputStream().close();
				} catch (IOException e) {
					// ignore
				}
		}

		if (lastModified == 0) {
			String filepath = url.getPath();
			if (filepath != null) {
				File f = new File(filepath);
				if (f.exists()) {
					lastModified = f.lastModified();
				}
			}
		}

		return lastModified;
	}

	private boolean resourceModified(long resTimestamp, long modSince) {
		modSince /= 1000;
		resTimestamp /= 1000;

		return resTimestamp == 0 || modSince == -1 || resTimestamp > modSince;
	}

	private void copyResource(URL url, HttpServletResponse res) throws IOException {
		OutputStream os = null;
		InputStream is = null;

		try {
			os = res.getOutputStream();
			is = url.openStream();

			int len = 0;
			byte[] buf = new byte[1024];
			int n;

			while ((n = is.read(buf, 0, buf.length)) >= 0) {
				os.write(buf, 0, n);
				len += n;
			}

			res.setContentLength(len);
		} finally {
			if (is != null) {
				is.close();
			}

			if (os != null) {
				os.close();
			}
		}
	}
}
