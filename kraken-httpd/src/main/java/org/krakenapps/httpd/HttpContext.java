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
import javax.servlet.ServletException;
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

	public void handle(Request request, Response response) throws IOException {
		HttpServlet servlet = null;
		String pathInfo = null;

		logger.trace("kraken httpd: request [{} {}]", request.getMethod(), request.getRequestURI());

		try {
			ServletMatchResult r = servletContext.matches(request.getRequestURI());
			if (r != null) {
				servlet = (HttpServlet) r.getServlet();
				pathInfo = r.getPathInfo();
			} else if (webSocketManager.getPath().equals(request.getRequestURI())) {
				servlet = webSocketManager.getServlet();
				pathInfo = request.getRequestURI();
			} else {
				request.setHttpContext(this);
				request.setPathInfo(request.getRequestURI());
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			request.setHttpContext(this);
			request.setServletPath(r.getServletPath());
			request.setPathInfo(pathInfo);

			servlet.service(request, response);
		} catch (FileNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (ServletException e) {
			logger.error("kraken httpd: servlet service error.", e);
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
