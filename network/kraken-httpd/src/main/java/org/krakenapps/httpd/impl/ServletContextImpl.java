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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.krakenapps.httpd.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletContextImpl implements ServletContext {
	private final Logger logger;
	private String name;
	private String serverInfo;
	private String contextPath;
	private ServletDispatcher dispatcher;

	private ConcurrentMap<String, String> initParams;
	private ConcurrentMap<String, Object> attrs;
	private SessionCookieConfig sessionCookieConfig;
	private Set<SessionTrackingMode> sessionTrackingModes;

	public ServletContextImpl(String name, String contextPath, String serverInfo) {
		this.logger = LoggerFactory.getLogger("javax.servlet.SerlvetContext." + name);
		this.name = name;
		this.serverInfo = serverInfo;
		this.contextPath = contextPath;
		this.dispatcher = new ServletDispatcher();
		this.initParams = new ConcurrentHashMap<String, String>();
		this.attrs = new ConcurrentHashMap<String, Object>();
		this.sessionCookieConfig = new SessionCookieConfigImpl();
		this.sessionTrackingModes = new HashSet<SessionTrackingMode>();
	}

	public ServletMatchResult matches(String path) {
		return dispatcher.matches(path);
	}

	@Override
	public String getServletContextName() {
		return name;
	}

	@Override
	public String getContextPath() {
		if ( contextPath.equals("/") )
			return "";
		else
			return contextPath;
	}

	@Override
	public ServletContext getContext(String uripath) {
		// do not allow access other servlet context
		return null;
	}

	@Override
	public int getMajorVersion() {
		return 3;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 3;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 0;
	}

	@Override
	public String getServerInfo() {
		return serverInfo;
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(initParams.keySet());
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		return initParams.putIfAbsent(name, value) == null;
	}

	@Override
	public Object getAttribute(String name) {
		return attrs.get(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(attrs.keySet());
	}

	@Override
	public void setAttribute(String name, Object object) {
		attrs.put(name, object);
	}

	@Override
	public void removeAttribute(String name) {
		attrs.remove(name);
	}

	@Override
	public Dynamic addServlet(String servletName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Dynamic addServlet(String servletName, Servlet servlet) {
		dispatcher.addServlet(servletName, servlet);
		return null;
	}

	@Override
	public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
		throw new UnsupportedOperationException();
	}

	public boolean removeServlet(String servletName) {
		return dispatcher.removeServlet(servletName);
	}

	@Override
	public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ServletRegistration getServletRegistration(String servletName) {
		return dispatcher.getServletRegistration(servletName);
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		return dispatcher.getServletRegistrations();
	}

	@Override
	public String getMimeType(String file) {
		return MimeTypes.instance().getByFile(file);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		// NOTE: use resource servlet instead
		return new HashSet<String>();
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		// NOTE: use resource servlet instead
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		// NOTE: use resource servlet instead
		return null;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Servlet getServlet(String name) throws ServletException {
		return dispatcher.getServlet(name);
	}

	@Override
	public Enumeration<Servlet> getServlets() {
		return Collections.enumeration(dispatcher.getServlets());
	}

	@Override
	public Enumeration<String> getServletNames() {
		return Collections.enumeration(dispatcher.getServletNames());
	}

	@Override
	public void log(String msg) {
		logger.info(msg);
	}

	@Override
	public void log(Exception exception, String msg) {
		logger.error(msg, exception);
	}

	@Override
	public void log(String message, Throwable throwable) {
		logger.info(message, throwable);
	}

	@Override
	public String getRealPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public FilterRegistration getFilterRegistration(String filterName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return sessionCookieConfig;
	}

	@Override
	public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
		this.sessionTrackingModes = sessionTrackingModes;
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return sessionTrackingModes;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(String className) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> void addListener(T t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addListener(Class<? extends EventListener> listenerClass) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareRoles(String... roleNames) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Servlet Context [");
		sb.append(name);
		sb.append("]\n* Context Path=");
		sb.append(contextPath);
		sb.append("\t");
		sb.append("\n* URL Mappings\n");
		sb.append(dispatcher);

		return sb.toString();
	}

}
