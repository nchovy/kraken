package org.krakenapps.webconsole;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ResourceContext extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected abstract InputStream getInputStream(HttpServletRequest req);

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		InputStream is = getInputStream(req);
		if (is != null) {
			byte[] b = new byte[4096];
			int len;

			while ((len = is.read(b)) != -1)
				resp.getOutputStream().write(b, 0, len);

			resp.getOutputStream().flush();
			resp.getOutputStream().close();
		}
	}
}
