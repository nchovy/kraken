/*
 * Copyright 2012 Future Systems
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.krakenapps.httpd.impl.Request;
import org.krakenapps.httpd.impl.Response;
import org.krakenapps.httpd.impl.ServletContextImpl;
import org.krakenapps.httpd.impl.ServletMatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpContext {
	private final Logger logger = LoggerFactory.getLogger(HttpContext.class.getName());

	private String name;
	private ServletContextImpl servletContext;
	private WebSocketManager webSocketManager;
	private ConcurrentMap<String, HttpSession> httpSessions;

	public HttpContext(String name) {
		this.name = name;
		this.servletContext = new ServletContextImpl(name, "/", "");
		this.webSocketManager = new WebSocketManager();
		this.httpSessions = new ConcurrentHashMap<String, HttpSession>();
	}

	public HttpContext(String name, String contextPath) {
		this.name = name;
		this.servletContext = new ServletContextImpl(name, contextPath, "");
		this.webSocketManager = new WebSocketManager();
		this.httpSessions = new ConcurrentHashMap<String, HttpSession>();
	}

	public void handle(Request request, Response response) throws IOException {
		request.setHttpContext(this);
		HttpServlet servlet = null;
		String pathInfo = null;

		String uri = request.getRequestURI();
		logger.trace("kraken httpd: request [{} {}]", request.getMethod(), uri);

		try {
			String servletPath = null;
			ServletMatchResult r = servletContext.matches(uri);
			if (webSocketManager.getPath().equals(uri)) {
				servlet = webSocketManager.getServlet();
				servletPath = webSocketManager.getPath();
				pathInfo = uri.substring(uri.indexOf(servletPath) + servletPath.length());
			} else if (r != null) {
				servlet = (HttpServlet) r.getServlet();
				pathInfo = r.getPathInfo();
				servletPath = r.getServletPath();
			} else {
				String contextPath = request.getContextPath();
				if (!contextPath.equals("")) {
					request.setPathInfo(uri.substring(uri.indexOf(contextPath) + contextPath.length()));
				} else {
					request.setPathInfo(uri);
				}

				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			logger.trace("kraken httpd: servlet path is [{}]", servletPath);
			request.setServletPath(servletPath);
			request.setPathInfo(pathInfo);

			servlet.service(request, response);
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (Throwable t) {
			logger.error("kraken httpd: servlet error", t);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if (response != null && !request.isAsyncStarted())
				response.close();
		}
	}

	public String getName() {
		return name;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public WebSocketManager getWebSocketManager() {
		return webSocketManager;
	}

	public ConcurrentMap<String, HttpSession> getHttpSessions() {
		return httpSessions;
	}

	public void addServlet(String name, Servlet servlet, String... urlPatterns) {
		servletContext.addServlet(name, servlet);
		ServletRegistration reg = servletContext.getServletRegistration(name);
		reg.addMapping(urlPatterns);
	}

	public boolean removeServlet(String name) {
		return servletContext.removeServlet(name);
	}

	@Override
	public String toString() {
		return "HTTP Context [" + name + ", sessions=" + httpSessions.size() + "]\n>>\n" + servletContext + webSocketManager;
	}
}
