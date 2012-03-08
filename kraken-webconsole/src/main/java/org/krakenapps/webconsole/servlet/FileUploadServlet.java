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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.httpd.HttpContext;
import org.krakenapps.httpd.HttpService;
import org.krakenapps.httpd.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-file-upload-servlet")
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class.getName());

	@Requires
	private HttpService httpd;

	@Requires
	private FileUploadApi upload;

	/**
	 * Register servlet to servlet registry of webconsole
	 */
	@Validate
	public void start() {
		HttpContext ctx = httpd.ensureContext("webconsole");
		ctx.addServlet("upload", this, "/upload");
	}

	@Override
	public void log(String message, Throwable t) {
		logger.warn("kraken webconsole: upload servlet error", t);
	}	

	public void setFileUploadApi(FileUploadApi upload) {
		this.upload = upload;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		UploadedFile f = null;
		FileInputStream is = null;
		ServletOutputStream os = null;
		String downloadToken = null;
		String fileGuid = null;

		logger.trace("kraken webconsole: jsession [{}]", req.getSession().getId());

		try {
			downloadToken = getDownloadToken(req);
			fileGuid = req.getParameter("resource");

			if (downloadToken == null) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}

			f = upload.getFileMetadataWithToken(downloadToken, fileGuid);
			is = new FileInputStream(f.getFile());
			os = resp.getOutputStream();
			logger.trace("kraken webconsole: open downstream for {}", f.getFile().getAbsolutePath());

			String mimeType = MimeTypes.instance().getByFile(f.getFileName());
			resp.setHeader("Content-Type", mimeType);

			String dispositionType = null;
			if (req.getParameter("force_download") != null)
				dispositionType = "attachment";
			else
				dispositionType = "inline";

			String encodedFilename = URLEncoder.encode(f.getFileName(), "UTF-8").replaceAll("\\+", "%20");
			resp.setHeader("Content-Disposition", dispositionType + "; filename*=UTF-8''" + encodedFilename);
			resp.setStatus(200);
			resp.setContentLength((int) f.getFileSize());

			byte[] b = new byte[8096];

			while (true) {
				int readBytes = is.read(b);
				if (readBytes <= 0)
					break;

				os.write(b, 0, readBytes);
			}
		} catch (Exception e) {
			resp.setStatus(500);
			logger.warn("kraken webconsole: cannot download id " + fileGuid, e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	private String getDownloadToken(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				logger.trace("kraken webconsole: checking all cookie for download, {} = {}", cookie.getName(),
						cookie.getValue());
				if (cookie.getName().equals("kraken_session"))
					return cookie.getValue();
			}
		}

		return req.getParameter("session");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		logger.debug("kraken webconsole: received post request from [{}]", req.getRemoteAddr());

		String token = req.getHeader("X-Upload-Token");
		if (token == null) {
			logger.warn("kraken webconsole: upload token header not found for [{}] stream", req.getRemoteAddr());
			return;
		}

		String range = req.getHeader("X-Upload-Range");
		String[] ranges = null;
		InputStream is = null;
		try {
			is = req.getInputStream();

			if (range == null) {
				upload.writeFile(token, is);
			} else {
				if (range != null)
					ranges = range.split(",");

				Long begin = Long.valueOf(ranges[0]);
				Long end = Long.valueOf(ranges[1]);

				if (begin >= end)
					throw new IllegalArgumentException("begin should be smaller than end range");

				logger.info("kraken webconsole: partial upload, token [{}], [{}~{}]",
						new Object[] { token, begin, end });

				// TODO: after complete dom porting
				// upload.writePartialFile(token, begin, end, is);
			}

			resp.setStatus(200);
		} catch (Exception e) {
			try {
				resp.sendError(500, e.toString());
			} catch (IOException e1) {
			}
			logger.warn("kraken webconsole: upload post failed", e);
		} finally {
			if (is == null)
				return;

			try {
				is.close();
			} catch (IOException e) {
			}

			try {
				resp.getOutputStream().close();
			} catch (IOException e) {
			}
		}
	}
}
