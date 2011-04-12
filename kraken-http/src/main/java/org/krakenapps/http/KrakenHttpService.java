package org.krakenapps.http;

import java.util.Dictionary;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.krakenapps.http.internal.handler.ServletHandler;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

public interface KrakenHttpService extends HttpService {
	@SuppressWarnings("rawtypes")
	void registerFilter(Filter filter, String pattern, Dictionary initParams, int ranking, HttpContext context)
			throws ServletException;

	void unregisterFilter(Filter filter);

	void unregisterServlet(Servlet servlet);

	ServletHandler[] servlets();
}
