package org.krakenapps.webconsole;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.krakenapps.webconsole.impl.Request;

public abstract class ResourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected abstract InputStream getInputStream(HttpServletRequest req);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
					if (is != null && is.available() > 0)
						throw new PageNotFoundException(req.getServletPath() + pathInfo + "/");
				}
			}
			throw new PageNotFoundException();
		}

		try {
			resp.setContentType(getMimeType(req.getPathInfo()));

			byte[] b = new byte[4096];
			int len;
			while ((len = is.read(b)) != -1)
				resp.getOutputStream().write(b, 0, len);
			resp.getOutputStream().flush();
		} catch (IOException e) {
			throw e;
		} finally {
			is.close();
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
