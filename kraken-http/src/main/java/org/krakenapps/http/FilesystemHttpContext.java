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
