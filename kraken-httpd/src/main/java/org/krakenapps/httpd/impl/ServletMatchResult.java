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
package org.krakenapps.httpd.impl;

import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;

public class ServletMatchResult {
	/**
	 * matched servlet path
	 */
	private String servletPath;

	/**
	 * path info
	 */
	private String pathInfo;

	/**
	 * matched servlet
	 */
	private Servlet servlet;

	/**
	 * matched servlet registration
	 */
	private ServletRegistration servletRegistration;

	ServletMatchResult(Servlet servlet, ServletRegistration reg, String servletPath, String pathInfo) {
		this.servlet = servlet;
		this.servletRegistration = reg;
		this.servletPath = servletPath;
		this.pathInfo = pathInfo;
	}

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public String getPathInfo() {
		return pathInfo;
	}

	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}

	public ServletRegistration getServletRegistration() {
		return servletRegistration;
	}

	public void setServletRegistration(ServletRegistration servletRegistration) {
		this.servletRegistration = servletRegistration;
	}

	@Override
	public String toString() {
		return "servlet=" + servlet + ", servletPath=" + servletPath + ", pathInfo=" + pathInfo;
	}

}
