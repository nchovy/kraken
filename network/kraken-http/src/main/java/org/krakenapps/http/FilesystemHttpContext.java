/*
 * Copyright 2010 NCHOVY
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
package org.krakenapps.http;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krakenapps.http.internal.util.MimeTypes;
import org.mortbay.jetty.handler.ResourceHandler;
import org.osgi.service.http.HttpContext;

public class FilesystemHttpContext implements HttpContext {
	private ResourceHandler handler;
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

	public FilesystemHttpContext(String basePath) {
		handler = new ResourceHandler();
		handler.setResourceBase("file://" + basePath);
	}

	@Override
	public String getMimeType(String name) {
		int pos = name.lastIndexOf('.');
		if (pos < 0)
			return DEFAULT_MIME_TYPE;

		String extension = name.substring(pos + 1);
		String mimeType = MimeTypes.instance().getByExtension(extension);
		if (mimeType != null)
			return mimeType;

		return DEFAULT_MIME_TYPE;
	}

	@Override
	public URL getResource(String name) {
		try {
			return handler.getResource("/" + name).getURL();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean handleSecurity(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		return true;
	}

	public String getResourceBase() {
		return handler.getResourceBase();
	}

}
